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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.naming.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.mitre.itflogin.IPasswordRule;
import org.mitre.itflogin.IUserValidity;
import org.mitre.itflogin.LockedUserException;
import org.mitre.itflogin.PasswordException;
import org.mitre.itflogin.UserException;
import org.mitre.itflogin.UserManager;
import org.mitre.itflogin.mail.MailHelper;
import org.mitre.itflogin.model.Role;
import org.mitre.itflogin.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean to manipulate user instances
 * 
 * @author DRAND
 */
@Transactional
public class UserManagerImpl extends HibernateDaoSupport implements UserManager {
	private static final Logger logger = LoggerFactory
			.getLogger(UserManagerImpl.class);

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
	private MailHelper mailer = null;
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
	@PostConstruct
	public void onCreation() {
		@SuppressWarnings("unchecked")
		List<Role> roles = getHibernateTemplate().find("from Role where name = 'ADMIN'");
		Role admin;
		if (roles.size() == 0) {
			admin = new Role();
			admin.setId(1L);
			admin.setName("ADMIN");
			getHibernateTemplate().save(admin);
		} else {
			admin = roles.get(0);
		}

		DetachedCriteria dc = DetachedCriteria.forClass(User.class);
		DetachedCriteria role = dc.createAlias("roles", "role");
		role.add(Restrictions.eq("role.name", "ADMIN"));
		@SuppressWarnings("unchecked")
		List<User> users = getHibernateTemplate().findByCriteria(dc);
		if (users.size() == 0) {
			if (StringUtils.isBlank(defaultAdminUserEmail) || 
					StringUtils.isBlank(defaultAdminUserName)) {
				logger.warn("Cannot create default user");
				return;
			}
			User defaultAdminUser = new User();
			defaultAdminUser.setUsername(defaultAdminUserName);
			defaultAdminUser.setEmail(defaultAdminUserEmail);
			String randomPassword = "p" + random.nextLong();
			int randomSalt = random.nextInt();
			try {
				String randomHash = salt(randomSalt, randomPassword);
				defaultAdminUser.setPasswordHash(randomHash);
				defaultAdminUser.setPasswordSalt(randomSalt);
				defaultAdminUser.getRoles().add(admin);
				getHibernateTemplate().save(defaultAdminUser);
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
		List<User> results = getHibernateTemplate().findByNamedParam(
				"from User where username = :username", "username", username);
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
		getHibernateTemplate().replicate(user, ReplicationMode.OVERWRITE);
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
		@SuppressWarnings("unchecked")
		List<User> results = getHibernateTemplate().findByNamedParam(
				"from User where username = :username", "username", username);
		if (results.size() > 0) {
			User u = results.get(0);
			getHibernateTemplate().delete(u);
		} else {
			logger.warn("User could not be found: " + username);
		}
	}	
	
	

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.UserManager#find(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> find(String likePattern) {
		if (likePattern == null || likePattern.trim().length() == 0) {
			throw new IllegalArgumentException(
					"likePattern should never be null or empty");
		}
		DetachedCriteria dc = DetachedCriteria.forClass(User.class);
		dc.add(Restrictions.ilike("username", likePattern));
		return getHibernateTemplate().findByCriteria(dc);
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.UserManager#find(java.lang.String)
	 */
	public Role findRole(String rolename) {
		if (rolename == null || rolename.trim().length() == 0) {
			throw new IllegalArgumentException(
					"rolename should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		List<Role> found = getHibernateTemplate().findByNamedParam(
				"from Role where name = :name", "name", rolename);
		if (found.size() == 0) {
			Role role = new Role();
			role.setName(rolename);
			getHibernateTemplate().persist(role);
			return findRole(rolename);
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
		List<User> found = getHibernateTemplate().findByNamedParam(
				"from User where username = :name", "name", username);
		if (found.size() > 0) {
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
			getHibernateTemplate().persist(newUser);
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
		List<User> found = getHibernateTemplate().findByNamedParam(
				"from User where username = :name", "name", username);
		if (found.size() == 0) {
			return false;
		}
		User user = found.get(0);
		int psalt = user.getPasswordSalt();
		try {
			String chash = salt(psalt, confirmation);
			boolean confirmed = chash.equals(user.getConfirmationHash());
			if (confirmed && user.getEmailConfirmed() == false) {
				user.setEmailConfirmed(true);
				getHibernateTemplate().persist(user);
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
		List<User> found = getHibernateTemplate().findByNamedParam(
				"from User where username = :name", "name", username);
		if (found.size() == 0) {
			throw new UserException("User " + username + " does not exist");
		}

		if (passwordRule != null)
			passwordRule.accept(newpassword);

		User user = found.get(0);
		int psalt = random.nextInt();
		try {
			String phash = salt(psalt, newpassword);
			user.setPasswordHash(phash);
			user.setPasswordSalt(psalt);
			user.setConfirmationHash(null); // Assume that the user may have done a reset
			getHibernateTemplate().persist(user);
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
		List<User> found = getHibernateTemplate().findByNamedParam(
				"from User where username = :name", "name", username);
		if (found.size() == 0) {
			throw new AuthenticationException();
		}
		User user = found.get(0);
		user.setFailedAttempts(0);
		getHibernateTemplate().persist(user);
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
		List<User> found = getHibernateTemplate().findByNamedParam(
				"from User where username = :name", "name", username);
		if (found.size() == 0) {
			throw new AuthenticationException();
		}
		User user = found.get(0);
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
				getHibernateTemplate().persist(user);
				throw new AuthenticationException();
			} else {
				user.setFailedAttempts(0);
				getHibernateTemplate().persist(user);
			}
		} catch (Exception e) {
			logger.error("Problem while authenticating user", e);
			throw new AuthenticationException();
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.UserManager#reset(java.lang.String)
	 */
	public String reset(String username) throws UserException {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException(
					"username should never be null or empty");
		}
		@SuppressWarnings("unchecked")
		List<User> found = getHibernateTemplate().findByNamedParam(
				"from User where username = :name", "name", username);
		if (found.size() == 0) {
			return null; // Quietly return
		}
		User user = found.get(0);

		// Setup a confirmation string, set the user to right state and send
		// the confirmation email
		String confirmationString = Long.toString(random.nextLong()) + Long.toString(random.nextLong());
		try {
			if (StringUtils.isNotBlank(user.getEmail())) {
				user.setConfirmationHash(salt(user.getPasswordSalt(),
						confirmationString));
				getHibernateTemplate().persist(user);
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
	public MailHelper getMailer() {
		return mailer;
	}

	/**
	 * @param mailer
	 *            the mailer to set
	 */
	public void setMailer(MailHelper mailer) {
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
