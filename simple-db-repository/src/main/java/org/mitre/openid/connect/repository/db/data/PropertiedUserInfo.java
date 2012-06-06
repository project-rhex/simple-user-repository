package org.mitre.openid.connect.repository.db.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.repository.db.EnhancedUserInfo;

public class PropertiedUserInfo extends DefaultUserInfo implements EnhancedUserInfo {
	/**
	 * Storage for the extended properties
	 */
	private Map<String,String> extendedProperties = new HashMap<String, String>();
	
	@Override
	public void setProperty(String name, String value) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Name must be supplied");
		}
		if (value == null) {
			extendedProperties.remove(name);
		} else {
			extendedProperties.put(name, value);
		}
		
	}

	@Override
	public String getProperty(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Name must be supplied");
		}
		return extendedProperties.get(name);
	}

	@Override
	public Set<String> keySet() {
		return Collections.unmodifiableSet(extendedProperties.keySet());
	}
}
