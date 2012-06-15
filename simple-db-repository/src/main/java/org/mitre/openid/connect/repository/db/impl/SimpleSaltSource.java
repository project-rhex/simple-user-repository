package org.mitre.openid.connect.repository.db.impl;

import org.mitre.openid.connect.repository.db.model.User;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service("simpleSaltSource")
public class SimpleSaltSource implements SaltSource {
	/**
	 * We know that this came from a User, so we can cast safely
	 */
	public Object getSalt(UserDetails user) {
		User originalUser = (User) user;
		return originalUser.getPasswordSalt();
	}
}
