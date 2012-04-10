/****************************************************************************************
 *  SimpleUserValidity.java
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
package org.mitre.openid.connect.repository.db.impl;

import org.mitre.openid.connect.repository.db.IUserValidity;
import org.mitre.openid.connect.repository.db.UserException;

/**
 * Check user validity. For this simple method the user should start with an 
 * alphabetic character and contain alphabetic, numeric or only the punctuation
 * characters _ or -. The username must have at least four characters.
 * 
 * @author DRAND
 */
public class SimpleUserValidity implements IUserValidity {
	/* (non-Javadoc)
	 * @see org.mitre.itflogin.impl.IUserValidity#valid(java.lang.String)
	 */
	public void valid(String username) throws UserException {
		if (username == null || username.trim().length() == 0) {
			throw new UserException(
					"username should never be null or empty");
		}
		username = username.toLowerCase();
		if (username.length() < 4) {
			throw new UserException("The username must have at least 4 characters");
		}
		if (! Character.isLetter(username.charAt(0))) {
			throw new UserException("A username must start with a letter");
		}
		for(int i = 1; i < username.length(); i++) {
			int ch = username.charAt(i);
			if (Character.isLetter(ch)) {
				// 
			} else if (Character.isDigit(ch)) {
				//
			} else if (ch == '_' || ch == '-') {
				//
			} else {
				throw new UserException("The username contained the invalid character " + (char) ch);
			} 
		}
 	}
}
