/****************************************************************************************
 *  UserManagerImpl.java
 *
 *  Created: Jul 8, 2010
 *
 *  @author DRAND
 *
 *  (C) Copyright MITRE Corporation 2010
 *
 *  The program is provided "as is" without any warranty express or implied, including
 *  the warranty of non-infringement and the implied warranties of merchantibility and
 *  fitness for a particular purpose.  The Copyright owner will not be liable for any
 *  damages suffered by you as a result of using the Program.  In no event will the
 *  Copyright owner be liable for any special, indirect or consequential damages or
 *  lost profits even if the Copyright owner has been advised of the possibility of
 *  their occurrence.
 *
 ***************************************************************************************/
package org.mitre.openid.connect.repository.db.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.naming.AuthenticationException;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.repository.db.IPasswordRule;
import org.mitre.openid.connect.repository.db.IUserValidity;
import org.mitre.openid.connect.repository.db.LockedUserException;
import org.mitre.openid.connect.repository.db.PasswordException;
import org.mitre.openid.connect.repository.db.UserException;
import org.mitre.openid.connect.repository.db.UserManager;
import org.mitre.openid.connect.repository.db.model.Role;
import org.mitre.openid.connect.repository.db.model.User;
import org.mitre.openid.connect.repository.db.model.UserAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean to manipulate user instances
 * 
 * @author DRAND
 */
@Transactional
public class UserManagerImpl implements UserManager {
	private static final Logger logger = LoggerFactory
			.getLogger(UserManagerImpl.class);

	@PersistenceContext
	private EntityManager em;
	
	/**
	 * The default administrative user to create *if* there are no defined 
	 * admin users in the system on startup.
	 */
	private String defaultAdminUserName = null;
	/**
	 * Email for the default administrative user
	 */
	private String defaultAdminUserEmail = null;
	/**
	 * Rule that decides if a password is acceptable to the system. 
	 */
	private IPasswordRule passwordRule = null;
	/**
	 * Rule that decides if a user is acceptable to the system
	 */
	private IUserValidity userValidity = null;
	/**
	 * Secure random number generator. Do not replace with the regular random
	 * number generator. A pseudo random number generator will tend to produce
	 * evenly distributed but predictable patterns.
	 */
	private SecureRandom random = new SecureRandom();
	/**
	 * A separate component that isolates JavaMail.
	 */
	private MailSender mailer = null;
	/**
	 * The number of tries the user gets before we lock the user out of the system
	 * and require admin intervention.
	 */
	private int attemptLimit = 3;
	/**
	 * Base url that the web application will be placed at. There will be a set
	 * of predictable web components off of this base that handle password
	 * reset, login, and other tasks. This base url is used to create mail 
	 * messages that contain links to these pages.
	 */
	private URL base = null; 
	
	/**
	 * If the repository has no users that are administrators, create one based
	 * on the configuration. Also, create the admin role if it doesn't exist.
	 */
	public void testAndInitialize() {
		@SuppressWarnings("unchecked")
		Role admin = findRole("ADMIN");
		// Find admin users only
		TypedQuery<User> uq = (TypedQuery<User>) 
				em.createQuery("select u from User u inner join u.roles r where r.name = 'ADMIN'");
		List<User> users = uq.getResultList();
		if (users.size() == 0) {
			if (StringUtils.isBlank(defaultAdminUserName)) {
				logger.warn("Cannot create default user");
				return;
			}
			User defaultAdminUser = new User();
			defaultAdminUser.setUsername(defaultAdminUserName);
			defaultAdminUser.setEmail(defaultAdminUserEmail);
			String initialpw = "PassWord";
			int randomSalt = random.nextInt();
			try {
				String randomHash = salt(randomSalt, initialpw);
				defaultAdminUser.setPasswordHash(randomHash);
				defaultAdminUser.setPasswordSalt(randomSalt);
				defaultAdminUser.getRoles().add(admin);
				em.persist(defaultAdminUser);
			} catch (Exception e) {
				logger.error("Something's wrong that shouldn't be wrong", e);
			}
		}
		
		if (userValidity == null) {
			userValidity = new SimpleUserValidity();
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#get(java.lang.String)
	 */
	public User get(String username) {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		TypedQuery<User> uq = (TypedQuery<User>) em.createQuery(
				"select u from User u where u.username = :username");
		List<User> results = uq.setParameter("username", username).getResultList();
		return results.size() > 0 ? results.get(0) : null;
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.UserManager#save(org.mitre.itflogin.model.User)
	 */
	public void save(User user) {
		if (user == null) {
			throw new IllegalArgumentException(
					"user should never be null");
		}
		if (user.getId() == null)
			em.persist(user);
		else
			em.merge(user);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mitre.itflogin.UserManager#delete(java.lang.String)
	 */
	public void delete(String username) {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		User existing = get(username);
		if (existing != null) {
			em.remove(existing);
		} else {
			logger.warn("User could not be found: " + username);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#deleteRole(java.lang.String)
	 */
	public void deleteRole(String rolename) {
		if (rolename == null || rolename.trim().length() == 0) {
			throw new IllegalArgumentException(
					"rolename should never be null or empty");
		}
		Role existing = findRole(rolename);
		if (existing != null) {
			em.remove(existing);
		} else {
			logger.warn("Role could not be found: " + rolename);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.UserManager#find(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<User> find(String likePattern) {
		if (likePattern == null || likePattern.trim().length() == 0) {
			throw new IllegalArgumentException(
					"likePattern should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		TypedQuery<User> uq = (TypedQuery<User>) em.createQuery(
				"select u from User u where lower(u.username) like :pattern");
		List<User> results = uq.setParameter("pattern", likePattern.toLowerCase()).getResultList();
		return results;
	}

	private final static String ROLE_Q_BY_NAME = "select r from Role r where r.name = :name";
	
	/* (non-Javadoc)
	 * @see org.mitre.itflogin.UserManager#find(java.lang.String)
	 */
	public Role findRole(String rolename) {
		if (rolename == null || rolename.trim().length() == 0) {
			throw new IllegalArgumentException(
					"rolename should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		TypedQuery<Role> rq = (TypedQuery<Role>) em.createQuery(ROLE_Q_BY_NAME);
		List<Role> found = rq.setParameter("name", rolename).getResultList();
		if (found.size() == 0) {
			Role role = new Role();
			role.setName(rolename);
			em.persist(role);
			return role;
		} else {
			return found.get(0);
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#add(java.lang.String, java.lang.String)
	 */
	public void add(String username, String password) throws PasswordException,
			UserException {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		User existing = get(username);
		if (existing != null) {
			throw new UserException("User " + username + " already exists");
		}

		userValidity.valid(username);
		
		if (passwordRule != null)
			passwordRule.accept(password);

		User newUser = new User();
		newUser.setUsername(username);
		int psalt = random.nextInt();
		try {
			String phash = salt(psalt, password);
			newUser.setPasswordHash(phash);
			newUser.setPasswordSalt(psalt);
			em.persist(newUser);
		} catch (Exception e) {
			logger.error("Problem while storing user", e);
			throw new UserException(
					"User "
							+ username
							+ " was not stored due to system problem. Contact a developer to look at the logs.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#checkConfirmation(java.lang.String, java.lang.String)
	 */
	public boolean checkConfirmation(String username, String confirmation) {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		if (confirmation == null || confirmation.trim().length() == 0) {
			throw new IllegalArgumentException(
					"confirmation should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		User user = get(username);
		if (user != null) {
			return false;
		}
		int psalt = user.getPasswordSalt();
		try {
			String chash = salt(psalt, confirmation);
			boolean confirmed = chash.equals(user.getConfirmationHash());
			if (confirmed && user.getEmailConfirmed() == false) {
				user.setEmailConfirmed(true);
				em.persist(user);
			}
			return confirmed;
		} catch (Exception e) {
			logger.error("Problem while confirming confirmation string", e);
			return false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#modifyPassword(java.lang.String, java.lang.String)
	 */
	public void modifyPassword(String username, String newpassword) throws UserException, PasswordException {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		User user = get(username);
		if (user == null) {
			throw new UserException("User " + username + " does not exist");
		}

		if (passwordRule != null)
			passwordRule.accept(newpassword);

		int psalt = random.nextInt();
		try {
			String phash = salt(psalt, newpassword);
			user.setPasswordHash(phash);
			user.setPasswordSalt(psalt);
			user.setConfirmationHash(null); // Assume that the user may have done a reset
			em.persist(user);
		} catch (Exception e) {
			logger.error("Problem while storing user", e);
			throw new UserException(
					"User "
							+ username
							+ " was not stored due to system problem. Contact a developer to look at the logs.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#unlock(java.lang.String)
	 */
	public void unlock(String username) throws AuthenticationException {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		User user = get(username);
		if (user == null) {
			throw new AuthenticationException();
		}
		user.setFailedAttempts(0);
		em.persist(user);
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#authenticate(java.lang.String, java.lang.String)
	 */
	public void authenticate(String username, String password)
			throws AuthenticationException, LockedUserException {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		if (password == null) {
			password = "";
		}
		@SuppressWarnings("unchecked")
		User user = get(username);
		if (user == null) {
			throw new AuthenticationException();
		}
		if (user.getFailedAttempts() != null
				&& user.getFailedAttempts() >= attemptLimit) {
			throw new LockedUserException();
		}
		int psalt = user.getPasswordSalt();
		try {
			String phash = salt(psalt, password);
			if (!phash.equals(user.getPasswordHash())) {
				int attemps = user.getFailedAttempts() != null ? user
						.getFailedAttempts() : 0;
				user.setFailedAttempts(attemps + 1);
				em.persist(user);
				throw new AuthenticationException();
			} else {
				user.setFailedAttempts(0);
				em.persist(user);
			}
		} catch (Exception e) {
			logger.error("Problem while authenticating user", e);
			throw new AuthenticationException();
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#reset(java.lang.String)
	 */
	public String reset(String username) throws UserException, AuthenticationException {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		User user = get(username);
		if (user == null) {
			throw new AuthenticationException();
		}
		// Setup a confirmation string, set the user to right state and send
		// the confirmation email
		String confirmationString = Long.toString(random.nextLong()) + Long.toString(random.nextLong());
		try {
			if (StringUtils.isNotBlank(user.getEmail())) {
				user.setConfirmationHash(salt(user.getPasswordSalt(),
						confirmationString));
				em.persist(user);
			} else {
				throw new UserException("No available email, see administrator");
			}
		} catch (Exception e) {
			// Failed log it
			logger.error("Failed to reset password, problem was", e);
			throw new UserException(
					"Failed to reset user password, see administrator");
		}
		
		return confirmationString;
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#salt(int, java.lang.String)
	 */
	public String salt(int saltValue, String password)
			throws NoSuchAlgorithmException, IOException {
		if (password == null || password.trim().length() == 0) {
			throw new IllegalArgumentException(
					"password should never be null or empty");
		}
		byte[] pdata = password.getBytes("UTF8");
		byte[] cdata = new byte[4];
		cdata[0] = (byte) (saltValue & 0x000000FF);
		cdata[1] = (byte) ((saltValue & 0x0000FF00) >> 8);
		cdata[2] = (byte) ((saltValue & 0x00FF0000) >> 16);
		cdata[3] = (byte) ((saltValue & 0xFF000000) >> 24);
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(cdata);
		byte thedigest[] = digest.digest(pdata);
		StringBuilder rval = new StringBuilder(thedigest.length * 2);
		for (int i = 0; i < thedigest.length; i++) {
			String part = String.format("%02x", thedigest[i]);
			rval.append(part);
		}
		return rval.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#getAttributes(org.mitre.openid.connect.repository.db.model.User)
	 */
	public Collection<UserAttribute> getAttributes(User user) {
		TypedQuery<UserAttribute> uaq = (TypedQuery<UserAttribute>)
				em.createQuery("select ua from UserAttribute ua where ua.user_id = :id");
		return uaq.setParameter("id", user.getId()).getResultList();
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#loadAttribute(java.lang.Long)
	 */
	public UserAttribute loadAttribute(Long id) {
		return em.find(UserAttribute.class, id);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#saveAttribute(org.mitre.openid.connect.repository.db.model.UserAttribute)
	 */
	public void saveAttribute(UserAttribute attribute) {
		if (attribute.getId() != null)
			em.merge(attribute);
		else
			em.persist(attribute);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#removeAttribute(org.mitre.openid.connect.repository.db.model.UserAttribute)
	 */
	public void removeAttribute(UserAttribute attribute) {
		Query q = em.createQuery("delete from UserAttribute where id = :id");
		q.setParameter("id", attribute.getId()).executeUpdate();
	}

	/**
	 * @return the passwordRule
	 */
	public IPasswordRule getPasswordRule() {
		return passwordRule;
	}

	/**
	 * @param passwordRule
	 *            the passwordRule to set
	 */
	public void setPasswordRule(IPasswordRule passwordRule) {
		this.passwordRule = passwordRule;
	}

	/**
	 * @return the mailer
	 */
	public MailSender getMailer() {
		return mailer;
	}

	/**
	 * @param mailer
	 *            the mailer to set
	 */
	public void setMailer(MailSender mailer) {
		this.mailer = mailer;
	}

	/**
	 * @return the attemptLimit
	 */
	public int getAttemptLimit() {
		return attemptLimit;
	}

	/**
	 * @param attemptLimit
	 *            the attemptLimit to set
	 */
	public void setAttemptLimit(int attemptLimit) {
		this.attemptLimit = attemptLimit;
	}

	public URL getBaseURL() {
		return base;
	}
	
	/**
	 * @return the base
	 */
	public String getBase() {
		return base.toExternalForm();
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(String base) {
		try {
			this.base = new URL(base);
		} catch (MalformedURLException e) {
			logger.error("Problem parsing base url", e);
		}
	}

	/**
	 * @return the defaultAdminUserName
	 */
	public String getDefaultAdminUserName() {
		return defaultAdminUserName;
	}

	/**
	 * @param defaultAdminUserName the defaultAdminUserName to set
	 */
	public void setDefaultAdminUserName(String defaultAdminUserName) {
		this.defaultAdminUserName = defaultAdminUserName;
	}

	/**
	 * @return the defaultAdminUserEmail
	 */
	public String getDefaultAdminUserEmail() {
		return defaultAdminUserEmail;
	}

	/**
	 * @param defaultAdminUserEmail the defaultAdminUserEmail to set
	 */
	public void setDefaultAdminUserEmail(String defaultAdminUserEmail) {
		this.defaultAdminUserEmail = defaultAdminUserEmail;
	}

	/**
	 * @return the userValidity
	 */
	public IUserValidity getUserValidity() {
		return userValidity;
	}

	/**
	 * @param userValidity the userValidity to set
	 */
	public void setUserValidity(IUserValidity userValidity) {
		if (userValidity == null) {
			throw new IllegalArgumentException(
					"userValidity should never be null");
		}
		this.userValidity = userValidity;
	}
}
