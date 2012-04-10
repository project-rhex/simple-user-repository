/****************************************************************************************
 *  SimplePasswordRule.java
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

import java.util.regex.Pattern;

import org.mitre.itflogin.IPasswordRule;
import org.mitre.itflogin.PasswordException;

/**
 * The simple password rule requires three of the four following character
 * classes and a minimum length password:
 * <ul>
 * <li>Upper case characters
 * <li>Lower case characters
 * <li>Numbers
 * <li>Special characters such as !@#$%^&*()_-+=;':"[]{},./<>?
 * </ul>
 *  
 * @author DRAND
 *
 */
public class SimplePasswordRule implements IPasswordRule {
	private static final String MESSAGE = "The password must contain at least three of the following four kinds" +
			" of characters: upper case characters, lower case characters, numbers and special characters. It must" +
			" also be at least ";
	
	private int minlength = 8;
	private Pattern lower = Pattern.compile("\\p{Lower}{1}");
	private Pattern upper = Pattern.compile("\\p{Upper}{1}");
	private Pattern digit = Pattern.compile("\\p{Digit}{1}");
	private Pattern special = Pattern.compile("\\p{Punct}{1}");
	
	@Override
	public void accept(String password) throws PasswordException {
		int count = 0;
		if (password != null && password.length() >= minlength) {
			if (lower.matcher(password).find()) {
				count++;
			}
			if (upper.matcher(password).find()) {
				count++;
			}
			if (digit.matcher(password).find()) {
				count++;
			}
			if (special.matcher(password).find()) {
				count++;
			}
			if (count > 2) { 
				return;
			}
		}
		throw new PasswordException(MESSAGE + minlength + " characters long");
	}

	/**
	 * @return the minlength
	 */
	public int getMinlength() {
		return minlength;
	}

	/**
	 * @param minlength the minlength to set
	 */
	public void setMinlength(int minlength) {
		this.minlength = minlength;
	}	
}
