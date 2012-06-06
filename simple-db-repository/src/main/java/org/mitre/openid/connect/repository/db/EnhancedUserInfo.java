package org.mitre.openid.connect.repository.db;

import java.util.Set;

import org.mitre.openid.connect.model.UserInfo;

/**
 * A set of additional methods allowing the storage and manipulation of properties beyond those
 * defined in the core {@link UserInfo} interface.
 * 
 * @author DRAND
 */
public interface EnhancedUserInfo extends UserInfo {
	/**
	 * Add an additional property to the user information
	 * 
	 * @param name
	 *            the name of the property, must not be one of the existing user
	 *            info properties, never <code>null</code> or empty.
	 * @param value
	 *            a value to associate with the property. If the value is
	 *            <code>null</code> then any existing set property with the
	 *            given name will be removed.
	 */
	void setProperty(String name, String value);
	
	/**
	 * Retrieve the individual value of a given property
	 * 
	 * @param name the name of the property to be retrieve, never <code>null</code> or empty
	 * @return the value, may be <code>null</code> if the property is undefined.
	 */
	String getProperty(String name);
	
	/**
	 * @return a read-only collection of the keys in the extended properties on the given
	 * user information object.
	 */
	Set<String> keySet();
}
