/****************************************************************************************
 *  UserController.java
 *
 *  Created: Apr 13, 2012
 *
 *  @author DRAND
 *
 *  (C) Copyright MITRE Corporation 2012
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
package org.mitre.openid.connect.repository.db.web;

import org.mitre.openid.connect.repository.db.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handle user requests
 * @author DRAND
 *
 */
@Controller
public class UserController {
	@Autowired
	private UserManager um;
	
	@RequestMapping(value="/users", method=RequestMethod.GET, produces="application/json")
	public String findRange(Model model) {
		
		return null;
	}
	
	@RequestMapping(value="/index", method=RequestMethod.GET, produces="text/html")
	public String index(Model model) {
		return null;
	}

	/**
	 * @return the um
	 */
	public UserManager getUm() {
		return um;
	}

	/**
	 * @param um the um to set
	 */
	public void setUm(UserManager um) {
		this.um = um;
	}
}
