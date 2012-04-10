/****************************************************************************************
 *  TestUserImpl.java
 *
 *  Created: Jul 9, 2010
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

import java.util.List;

import javax.annotation.Resource;
import javax.naming.AuthenticationException;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import org.mitre.itflogin.LockedUserException;
import org.mitre.itflogin.PasswordException;
import org.mitre.itflogin.UserManager;
import org.mitre.itflogin.impl.UserManagerImpl;
import org.mitre.itflogin.model.Role;
import org.mitre.itflogin.model.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/mitre/itflogin/test/test.xml" })
public class TestUserImpl {
	@Resource UserManager usermanager;
	
	@Test public void testSalting() throws Exception {
		String val1 = usermanager.salt(0x10020110, "Fido1234$");
		String val2 = usermanager.salt(0x1A345510, "Fido1234$");
		String val3 = usermanager.salt(0x10020110, "Fido1234$");
		String val4 = usermanager.salt(0x10020110, "badpassword");
		assertNotNull(val1);
		assertNotNull(val2);
		assertTrue(val1.length() > 20);
		assertNotSame(val1, val2);
		assertEquals(val1, val3);
		assertNotSame(val3, val4);
	}
	
	@Test public void testAdminUserIsCreated() throws Exception {
		User u = usermanager.get("drand");
		assertNotNull(u);
		assertEquals("drand", u.getUsername());
		assertNotNull(u.getRoles());
		assertTrue(u.getRoles().size() > 0);
		assertEquals("ADMIN", u.getRoles().iterator().next().getName());
	}
	
	@Test public void testCreateAndAuthenticate() throws Exception {
		String pw = "Fido1234$";
		usermanager.add("john", pw);
		try {
			usermanager.authenticate("john", "badpassword");
			fail("Should have thrown an exception");
		} catch(AuthenticationException e) {
			// OK, expected
		}
		
		try {
			usermanager.authenticate("john", pw);
		} catch(AuthenticationException e) {
			fail("Should not have thrown exception");
		}
	}
	
	@Test public void testLocking() throws Exception {
		String pw = "PsPw55123124$";
		usermanager.add("chris", pw);
		try {
			usermanager.authenticate("chris", "badpassword1");
			fail("Should have thrown an exception");
		} catch(AuthenticationException e) {
			// OK, expected
		} catch(LockedUserException le) {
			fail("Wrong exception");
		}
		
		try {
			usermanager.authenticate("chris", "badpassword2");
			fail("Should have thrown an exception");
		} catch(AuthenticationException e) {
			// OK, expected
		} catch(LockedUserException le) {
			fail("Wrong exception");
		}
		
		try {
			usermanager.authenticate("chris", "badpassword3");
			fail("Should have thrown an exception");
		} catch(AuthenticationException e) {
			// OK, expected
		} catch(LockedUserException le) {
			fail("Wrong exception");
		}
		
		try {
			usermanager.authenticate("chris", pw);
			fail("Should have thrown an exception");
		} catch(AuthenticationException e) {
			fail("Wrong exception");
		} catch(LockedUserException le) {
			// OK, correct exception
		}
		
		usermanager.unlock("chris");
		
		try {
			usermanager.authenticate("chris", "badpassword1");
			fail("Should have thrown an exception");
		} catch(AuthenticationException e) {
			// OK, expected
		} catch(LockedUserException le) {
			fail("Wrong exception");
		}
		
		try {
			usermanager.authenticate("chris", pw);
		} catch(AuthenticationException e) {
			fail("Exception not expected");
		} catch(LockedUserException le) {
			fail("Exception not expected");
		}
	}
	
	@Test public void testResetAndConfirmation() throws Exception {
		usermanager.add("joex", "xyZZ12##");
		User j = usermanager.get("joex");
		Role g = usermanager.findRole("GUEST");
		j.getRoles().add(g);
		j.setEmail("drand@mitre.org");
		j.setEmailConfirmed(true);
		usermanager.save(j);
		
		String cf = usermanager.reset("joex");
		usermanager.checkConfirmation("joex", cf);
		
		j = usermanager.get("joex");
		assertTrue(j.getRoles().contains(g));
	}
	
	@Test public void testDelete() throws Exception {
		usermanager.add("jane", "xyZZ12##");
		User jane = usermanager.get("jane");
		assertNotNull(jane);
		usermanager.delete("jane");
		jane = usermanager.get("jane");
		assertNull(jane);
	}
	
	@Test public void testFind() throws Exception {
		List<User> users = usermanager.find("J%");
		assertNotNull(users);
		assertTrue(users.size() > 0);
	}
}
