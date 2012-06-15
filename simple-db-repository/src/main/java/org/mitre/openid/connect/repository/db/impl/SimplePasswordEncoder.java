package org.mitre.openid.connect.repository.db.impl;

import java.security.MessageDigest;

import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("simplePasswordEncoder")
public class SimplePasswordEncoder implements PasswordEncoder {

	@Override
	public String encodePassword(String password, Object salt) {
		try {
			Integer saltValue = (Integer) salt;
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
		} catch (Exception e) {
			throw new RuntimeException("Problem encoding password", e);
		}
	}

	@Override
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		String check = encodePassword(rawPass, salt);
		return check.equals(encPass);
	}
}
