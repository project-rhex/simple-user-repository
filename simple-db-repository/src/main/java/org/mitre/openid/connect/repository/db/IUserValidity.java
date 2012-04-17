/****************************************************************************************
 *  IUserValidity.java
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
package org.mitre.openid.connect.repository.db;


public interface IUserValidity {

	/**
	 * Check that the passed username passes user validity rules. 
	 * @param username
	 * @throws UserException
	 */
	public abstract void valid(String username) throws UserException;

}