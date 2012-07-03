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

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.repository.SortBy;
import org.mitre.openid.connect.repository.UserManager;
import org.mitre.openid.connect.repository.db.IPasswordRule;
import org.mitre.openid.connect.repository.db.IUserValidity;
import org.mitre.openid.connect.repository.db.LockedUserException;
import org.mitre.openid.connect.repository.db.PasswordException;
import org.mitre.openid.connect.repository.db.UserException;
import org.mitre.openid.connect.repository.db.model.Role;
import org.mitre.openid.connect.repository.db.model.User;
import org.mitre.openid.connect.repository.db.model.UserAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean to manipulate user instances
 * 
 * @author DRAND
 */
@Transactional
@Repository
public class UserManagerImpl implements UserManager {
	private static final Logger logger = LoggerFactory
			.getLogger(UserManagerImpl.class);

	@Autowired
	private PasswordEncoder simplePasswordEncoder;
	
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
	 * Default password for the admin user
	 */
	private String defaultAdminUserPassword = "PassWord";
	/**
	 * Rule that decides if a password is acceptable to the system. 
	 */
	private IPasswordRule passwordRule = null;
	/**
	 * Rule that decides if a user is acceptable to the system
	 */
	private IUserValidity userValidity = new SimpleUserValidity();
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
	
	/*
	 * (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#count()
	 */
	public int count() {
		TypedQuery<Number> uq = (TypedQuery<Number>) em.createNamedQuery("users.count");
		List<Number> results = uq.getResultList();
		return results.size() > 0 ? results.get(0).intValue() : 0;
	}

	
	
	@Override
	public User findById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id should never by null");
		}
		return em.find(User.class, id);
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
		TypedQuery<User> uq = (TypedQuery<User>) em.createNamedQuery("users.by_username");
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
		user.setUpdated(new Date(System.currentTimeMillis()));
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
	
	public void delete(Long userid) {
		if (userid == null) {
			throw new IllegalArgumentException("userid is a required argument");
		}
		User existing = findById(userid);
		if (existing != null) {
			em.remove(existing);
		} else {
			logger.warn("Userid could not be found: " + userid);
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
		TypedQuery<User> uq = (TypedQuery<User>) em.createNamedQuery("users.like_name");
		List<User> results = uq.setParameter("pattern", likePattern.toLowerCase()).getResultList();
		return results;
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.db.UserManager#findInRange(int, int, org.mitre.openid.connect.repository.db.UserManager.SortBy)
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> findInRange(int first, int count, SortBy sortBy) {
		List<User> users;
		if (sortBy.equals(SortBy.USERNAME) || sortBy.equals(SortBy.EMAIL)) {
			TypedQuery<User> uq = (TypedQuery<User>) em.createQuery(
					"select u from User u order by u." + sortBy.name().toLowerCase());
			users = uq.setFirstResult(first)
					.setMaxResults(count)
					.getResultList();
		} else {
			TypedQuery<User> uq = (TypedQuery<User>) em.createNamedQuery("users.by_user_attribute_name");
			users = uq.setParameter("attr", sortBy.name())
					.setFirstResult(first)
					.setMaxResults(count)
					.getResultList();
		}
		List<Map<String,String>> rval = new ArrayList<Map<String,String>>();
		for(User u : users) {
			Map<String, String> data = new HashMap<String, String>();
			Query q = em.createNamedQuery("user_attributes.by_user_id");
			List<UserAttribute> attrs = q.setParameter("id", u.getId()).getResultList();
			for(UserAttribute attr : attrs) {
				if (attr.getType() != UserAttribute.NORMAL_TYPE) continue;
				data.put(attr.getName(), attr.getValue());
			}
			data.put("USERNAME", u.getUsername());
			data.put("EMAIL", u.getEmail());
			data.put("ID", u.getId().toString());
			rval.add(data);
		}
		return rval;
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
		TypedQuery<Role> rq = (TypedQuery<Role>) em.createNamedQuery("roles.by_name");
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
        //System.out.println("GG5");
        newUser.setJamesPasswordHash(newUser.encodeJamesPasswordHash(password));

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

		setUserPassword(username, newpassword, user);
	}

	/**
	 * Set the password for the given user
	 * @param username
	 * @param newpassword
	 * @param user
	 * @throws UserException
	 */
	private void setUserPassword(String username, String newpassword, User user)
			throws UserException {
		int psalt = random.nextInt();
		try {
			String phash = salt(psalt, newpassword);
			user.setPasswordHash(phash);
            //System.out.println("GG3");
            user.setJamesPasswordHash(user.encodeJamesPasswordHash(newpassword));
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
	
	/**
	 * Bridge to password encoder
	 * @return
	 */
	private String salt(Integer saltValue, String password) {
		return simplePasswordEncoder.encodePassword(password, saltValue);
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

	public String getDefaultAdminUserPassword() {
		return defaultAdminUserPassword;
	}

	public void setDefaultAdminUserPassword(String defaultAdminUserPassword) {
		this.defaultAdminUserPassword = defaultAdminUserPassword;
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
