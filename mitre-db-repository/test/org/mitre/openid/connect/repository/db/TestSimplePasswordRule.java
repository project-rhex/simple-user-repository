/****************************************************************************************
 *  TestSimplePasswordRule.java
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
package org.mitre.openid.connect.repository.db;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mitre.itflogin.IPasswordRule;
import org.mitre.itflogin.PasswordException;
import org.mitre.itflogin.impl.SimplePasswordRule;


public class TestSimplePasswordRule {
	public static final IPasswordRule rule = new SimplePasswordRule();
	
	@Test
	public void testSimpleSuccess1() throws Exception {
		rule.accept("aAbB12345");
	}
	
	@Test
	public void testSimpleSuccess2() throws Exception {
		rule.accept("aAbBcCd!");
	}
	
	@Test
	public void testSimpleSuccess3() throws Exception {
		rule.accept("aaaa1234$");
	}
	
	@Test
	public void testSimpleSuccess4() throws Exception {
		rule.accept("BBBB122%");
	}
	
	@Test
	public void testSimpleTooFewCharacters() throws Exception {
		try {
			rule.accept("aAbB123");
			fail("Should have thrown");
		} catch(PasswordException pe) {
			// OK
		}
	
	}

	@Test
	public void testSimpleTooFewClasses1() throws Exception {
		try {
			rule.accept("aaaa5678");
			fail("Should have thrown");
		} catch(PasswordException pe) {
			// OK
		}
	}

	@Test
	public void testSimpleTooFewClasses2() throws Exception {
		try {
			rule.accept("1234BBBB");
			fail("Should have thrown");
		} catch(PasswordException pe) {
			// OK
		}
	}
	
	@Test
	public void testSimpleTooFewClasses3() throws Exception {
		try {
			rule.accept("12345678");
			fail("Should have thrown");
		} catch(PasswordException pe) {
			// OK
		}
	}
}
