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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.AuthenticationException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.repository.db.UserManager.SortBy;
import org.mitre.openid.connect.repository.db.model.Role;
import org.mitre.openid.connect.repository.db.model.User;
import org.mitre.openid.connect.repository.db.model.UserAttribute;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * To run this test put the webapp directory on the unit test classpath or
 * persistence.xml won't be found by jpa.
 * 
 * @author DRAND
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/mitre/openid/connect/repository/db/test.xml" })
public class TestUserImpl {
	@Resource UserManager usermanager;
	
	public static boolean setup = false;
	
	@Before
	public void testSetup() throws Exception {
		if (setup) return;
		setup = true;
		List<User> users = usermanager.find("%");
		for(User user : users) {
			usermanager.delete(user.getUsername());
		}
		
		usermanager.deleteRole("GUEST");
		usermanager.deleteRole("ADMIN");
		usermanager.testAndInitialize();
	}
	
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
		usermanager.add("zooey", "xzCB15%#");
		usermanager.add("zaaney", "xzCB15%#");
		List<User> users = usermanager.find("Z%");
		assertNotNull(users);
		assertTrue(users.size() > 0);
	}
	
	@Test public void testRoleMembership() throws Exception {
		usermanager.add("charlie", "xaBC95(#");
		User c = usermanager.get("charlie");
		Role g = usermanager.findRole("GUEST");
		c.getRoles().add(g);
		usermanager.save(c);
		
		// Check persistence
		c = usermanager.get("charlie");
		assertTrue(c.getRoles().size() > 0);
		assertEquals(g, c.getRoles().iterator().next());
		
		// Add another role, remove original role
		Role t = usermanager.findRole("TEST");
		c.getRoles().add(usermanager.findRole("TEST"));
		c.getRoles().remove(g);
		usermanager.save(c);
		
		// Check again
		c = usermanager.get("charlie");
		assertTrue(c.getRoles().size() > 0);
		assertEquals(t, c.getRoles().iterator().next());
		
		// Remove the test role
		c.getRoles().remove(t);
		usermanager.save(c);
		
		c = usermanager.get("charlie");
		assertTrue(c.getRoles().isEmpty());
	}
	
	@Test public void testUserAttributes() throws Exception {
		usermanager.add("meghan", "aAbBcC124%#$");
		User meghan = usermanager.get("meghan");
		
		UserAttribute a1 = new UserAttribute("a", "foo");
		UserAttribute a2 = new UserAttribute("b", "bar");
		meghan.getAttributes().add(a1);
		meghan.getAttributes().add(a2);
		usermanager.save(meghan);
		
		// Grab attributes for meghan and test
		meghan = usermanager.get("meghan");
		Collection<UserAttribute> attrs = meghan.getAttributes();
		assertEquals(2, attrs.size());
		
		// Remove an attribute
		UserAttribute found = null;
		for(UserAttribute attr : meghan.getAttributes()) {
			if ("b".equals(attr.getName())) {
				found = attr;
			}
		}
		if (found != null) {
			meghan.getAttributes().remove(found);
			usermanager.save(meghan);
		}
		meghan = usermanager.get("meghan");
		attrs = meghan.getAttributes();
		assertEquals(1, attrs.size());
		
		// Load just one attribute
		UserAttribute attr = attrs.iterator().next();
		assertNotNull(attr);
		assertEquals("a", attr.getName());
		assertEquals("foo", attr.getValue());
	}
	
	@Test public void testRangeAndSortFinder() throws Exception {
		for(int i = 0; i < 100; i++) {
			try {
				createUser();
			} catch(Exception e) {
				// It's ok, we're bound to clobber a user or two
			}
		}
		
		List<Map<String, String>> results = usermanager.findInRange(0, 10, SortBy.FIRST_NAME);
		assertNotNull(results);
		assertEquals(10, results.size());
		testOrdering(UserManager.SortBy.FIRST_NAME, results);
		
		results = usermanager.findInRange(30, 10, SortBy.FIRST_NAME);
		assertNotNull(results);
		assertEquals(10, results.size());
		testOrdering(UserManager.SortBy.FIRST_NAME, results);
		
		results = usermanager.findInRange(50, 10, SortBy.LAST_NAME);
		assertNotNull(results);
		assertEquals(10, results.size());
		testOrdering(UserManager.SortBy.LAST_NAME, results);
		
		results = usermanager.findInRange(5, 10, SortBy.EMAIL);
		assertNotNull(results);
		assertEquals(10, results.size());
		testOrdering(UserManager.SortBy.EMAIL, results);
		
		results = usermanager.findInRange(55, 10, SortBy.EMAIL);
		assertNotNull(results);
		assertEquals(10, results.size());
		testOrdering(UserManager.SortBy.EMAIL, results);
		
		results = usermanager.findInRange(0, 20, SortBy.USERNAME);
		assertNotNull(results);
		assertEquals(20, results.size());
		testOrdering(UserManager.SortBy.USERNAME, results);
		
		results = usermanager.findInRange(75, 20, SortBy.USERNAME);
		assertNotNull(results);
		assertEquals(20, results.size());
		testOrdering(UserManager.SortBy.USERNAME, results);		
	}
	
	private void testOrdering(SortBy key,
			List<Map<String, String>> results) {
		String lookup = key.name();
		for(int i = 0; i < results.size() - 1; i++) {
			Map<String, String> r0 = results.get(i);
			Map<String, String> r1 = results.get(i + 1);
			String v0 = r0.get(lookup);
			String v1 = r1.get(lookup);
			assertTrue(! (v0.compareTo(v1) > 0));
		}
	}

	public static final String[] names = new String[] {
		"Alice", "Barbara", "Chris", "David", "Eric", "Fred", "George",
		"Howard", "Irene", "Jane", "Kelly", "Laura", "Ira"
	};

	private String getRandomName() {
		return names[RandomUtils.nextInt(names.length)];
	}
	
	public static final String[] lastnames = new String[] {
		"Bach", "Chopin", "Newton", "Verne", "Brahms", "Gershwin", "Rogers", 
		"Paxton", "Bok"
	};
	
	public static final String[] companies = new String[] {
		"ibm.com", "mitre.org", "comcast.net", "verizon.com"
	};
	
	private void createUser() throws PasswordException, UserException {
		String username = getRandomName() + Integer.toString(RandomUtils.nextInt(100));
		String firstname = getRandomName();
		String lastname = lastnames[RandomUtils.nextInt(lastnames.length)];
		String email = username + "@" + companies[RandomUtils.nextInt(companies.length)];
		String password = "aAbBcCdD1234@!$%";
		
		usermanager.add(username, password);
		User u = usermanager.get(username);
		u.setEmail(email);
		u.getAttributes().add(new UserAttribute(UserManager.StandardAttributes.FIRST_NAME, firstname, u));
		u.getAttributes().add(new UserAttribute(UserManager.StandardAttributes.LAST_NAME, lastname, u));
		usermanager.save(u);
	
		
	}
}

