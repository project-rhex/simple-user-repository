/****************************************************************************************
 *  UserManager.java
 *
 *  Created: Jul 12, 2010
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
package org.mitre.openid.connect.repository;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mitre.openid.connect.repository.db.LockedUserException;
import org.mitre.openid.connect.repository.db.PasswordException;
import org.mitre.openid.connect.repository.db.UserException;
import org.mitre.openid.connect.repository.db.model.Role;
import org.mitre.openid.connect.repository.db.model.User;

/**
 * The user manager performs all internal tasks related to CRUD operations,
 * and managing users.
 * 
 * @author DRAND
 *
 */
public interface UserManager {
	/**
	 * Check the database and create any required users and roles.
	 */
	void testAndInitialize();
	
	/**
	 * @return get the total count of users in the database
	 */
	int count();
	
	/**
	 * Lookup user by name
	 * 
	 * @param username
	 *            the given user name, never <code>null</code> or empty
	 * @return return user description or <code>null</code> if not found
	 */
	User get(String username);
	
	/**
	 * Lookup user by id
	 * 
	 * @param id user id
	 * @return return user or <code>null</code> if not found
	 */
	User findById(Long id);

	/**
	 * Save user
	 * 
	 * @param user
	 *            , never <code>null</code>
	 */
	void save(User user);
	
	/**
	 * Remove user
	 * 
	 * @param username never <code>null</code> or empty
	 */
	void delete(String username);
	
	/**
	 * Remove user by id
	 * 
	 * @param id
	 */
	void delete(Long id);
	
	/**
	 * Delete role
	 * 
	 * @param rolename never <code>null</code> or empty
	 */
	void deleteRole(String rolename);

	/**
	 * Find or create role by name. May be used to find roles in order to add
	 * roles to a user.
	 * 
	 * @param rolename
	 *            the role, never <code>null</code> or empty
	 * @return the role, never <code>null</code>
	 */
	Role findRole(String rolename);

	/**
	 * Add the user to the database with the given password
	 * 
	 * @param username
	 * @param password
	 * @throws UserException
	 * @throws PasswordException
	 */
	void add(String username, String password) throws PasswordException,
			UserException;

	/**
	 * Check confirmation string against saved hash. If confirmed set the email
	 * confirmed state if not already set.
	 * 
	 * @param username
	 *            the username
	 * @param confirmation
	 *            the confirmation string
	 * @return <code>true</code> if the user is found and the confirmation
	 *         string checks out
	 */
	boolean checkConfirmation(String username, String confirmation);

	/**
	 * Modify an existing user with the new password
	 * 
	 * @param username
	 * @param newpassword
	 * @throws UserException
	 * @throws PasswordException
	 */
	void modifyPassword(String username, String newpassword)
			throws UserException, PasswordException;

	/**
	 * Unlock the given user
	 * 
	 * @param username
	 * @throws AuthenticationException
	 */
	void unlock(String username) throws AuthenticationException;

	/**
	 * Authenticate the given username + password pair
	 * 
	 * @param username
	 * @param password
	 * @throws AuthenticationException
	 * @throws LockedUserException
	 */
	void authenticate(String username, String password)
			throws AuthenticationException, LockedUserException;

	/**
	 * Reset the password state on a given username and setup for a confirmation
	 * email.
	 * 
	 * @param username
	 *            the user to setup, never <code>null</code> or empty
	 * @return confirmation string to allow an email to be sent. The email is
	 *         used to allow the user to reset their password or it can be used
	 *         to just confirm their email. The difference is the endpoint url
	 *         of the link sent to the user. Once used the confirmation becomes
	 *         useless.
	 * @throws AuthenticationException 
	 */
	String reset(String username) throws UserException, AuthenticationException;

	/**
	 * Salt the given value in preparation for saving in the database. The salt
	 * value and the "password", which is any plain text to be hashed, is placed
	 * in a byte array and then run through a hashing algorithm (SHA1). While
	 * SHA1 is not
	 * 
	 * @param saltValue
	 * @param password
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	String salt(int saltValue, String password)
			throws NoSuchAlgorithmException, IOException;

	/**
	 * @return the base url for the login system that was set as part of the
	 *         user manager initialization. This allows relative links to be
	 *         calculated, never <code>null</code>.
	 */
	URL getBaseURL();
	
	/**
	 * Find a subset of users
	 * @param likePattern a pattern to pass to the like clause
	 * @return a list of matching users
	 */
	List<User> find(String likePattern);
	
	/**
	 * Retrieve a set of information sorted by the given attribute
	 * 
	 * @param first initial offset into the user collection given the sorting
	 * @param count the number of results to return (maximum)
	 * @param sortBy the attribute to sort by
	 * @return user objects in a list, possibly an empty set
	 */
	List<Map<String, String>> findInRange(int first, int count, SortBy sortBy);
}