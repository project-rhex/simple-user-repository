package org.mitre.openid.connect.repository.db.impl;

import org.mitre.openid.connect.repository.UserManager;
import org.mitre.openid.connect.repository.db.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("simpleUserDetailsService")
public class SimpleUserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserManager userManager;
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		User user = userManager.get(username);
		if (user == null) {
			throw new UsernameNotFoundException("Didn't find " + username);
		}
		
		return user;
	}

}
