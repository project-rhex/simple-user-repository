/****************************************************************************************
 *  TestSimpleUserValidity.java
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

import org.junit.Test;
import static org.junit.Assert.*;
import org.mitre.itflogin.UserException;
import org.mitre.itflogin.impl.SimpleUserValidity;

public class TestSimpleUserValidity {
	SimpleUserValidity v = new SimpleUserValidity();

	@Test
	public void testGoodUser() throws Exception {
		try {
			v.valid("joey");
		} catch (UserException e) {
			fail("Should not have thrown");
		}
	}

	@Test
	public void testShortUser() throws Exception {
		try {
			v.valid("joe");
			fail("Should have thrown");
		} catch (UserException e) {
			// OK
		}
	}

	@Test
	public void testBadCharacter() throws Exception {
		try {
			v.valid("rons*");
			fail("Should have thrown");
		} catch (UserException e) {
			// OK
		}
	}

	@Test
	public void testBadFirstCharacter() throws Exception {
		try {
			v.valid("1joe");
			fail("Should have thrown");
		} catch (UserException e) {
			// OK
		}
	}

	@Test
	public void testAllGoodCharacters() throws Exception {
		try {
			v.valid("AllGood1234Characters_-");
		} catch (UserException e) {
			fail("Should not have thrown");
		}
	}
}
