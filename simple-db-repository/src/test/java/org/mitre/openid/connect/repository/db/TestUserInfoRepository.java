/****************************************************************************************
 *  TestUserInfoRepository.java
 *
 *  Created: Apr 17, 2012
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
package org.mitre.openid.connect.repository.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mitre.openid.connect.repository.UserManager;
import org.mitre.openid.connect.repository.db.model.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/mitre/openid/connect/repository/db/test.xml" })
public class TestUserInfoRepository {	
	@Resource
	private UserInfoRepository userinforepo;
	
	@Resource
	private UserManager usermanager;

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
	
	@Test
	public void testSaveAndGetByUserId() throws Exception {
		UserInfo userInfo = new DefaultUserInfo();
		Address addr = new Address();
		addr.setLocality("Chelmsford");
		addr.setRegion("Massachusetts");
		addr.setPostalCode("01824");
		addr.setStreetAddress("126 Penny Lane");
		addr.setFormatted("126 Penny Ln, Chelmsford, MA 01824");
		addr.setCountry("USA");
		
		userInfo.setUserId("msmith");
		userInfo.setAddress(addr);
		userInfo.setEmail("msmith@aol.com");
		userInfo.setGender("M");
		userInfo.setFamilyName("Smith");
		userInfo.setGivenName("Michael");
		userInfo.setLocale("en-US");
		userInfo.setMiddleName("Eugene");
		userInfo.setName("Mike Smith");
		userInfo.setNickname("Mike");
		userInfo.setPhoneNumber("978 256 1111");
		userInfo.setPicture("http://www.flicker.com/123456.png");
		userInfo.setProfile("http://www.facebook.com/mesmith");
		userInfo.setWebsite("http://www.linkedin.com/mesmith");
		userInfo.setZoneinfo("zone1");
		userinforepo.save((DefaultUserInfo) userInfo );
		
		// Retrieve and check
		UserInfo ui = userinforepo.getByUserId("msmith");
		assertNotNull(ui);
		Address addr2 = ui.getAddress();
		assertEquals("126 Penny Lane", addr2.getStreetAddress());
		assertEquals("126 Penny Ln, Chelmsford, MA 01824", addr2.getFormatted());
		assertEquals("Chelmsford", addr2.getLocality());
		assertEquals("Massachusetts", addr2.getRegion());
		assertEquals("01824", addr2.getPostalCode());
		assertEquals("USA", addr2.getCountry());
		
		assertEquals("msmith@aol.com", ui.getEmail());
		assertEquals("en-US", ui.getLocale());
		assertEquals("M", ui.getGender());
		assertEquals("Smith", ui.getFamilyName());
		assertEquals("Michael", ui.getGivenName());
		assertEquals("Eugene", ui.getMiddleName());
		assertEquals("Mike Smith", ui.getName());
		assertEquals("Mike", ui.getNickname());
		assertEquals("978 256 1111", ui.getPhoneNumber());
		assertEquals("http://www.flicker.com/123456.png", ui.getPicture());
		assertEquals("http://www.facebook.com/mesmith", ui.getProfile());
		assertEquals("http://www.linkedin.com/mesmith", ui.getWebsite());
		assertEquals("zone1", ui.getZoneinfo());
	}
	
	@Test
	public void testGetAll() throws Exception {
		Collection<? extends UserInfo> results = userinforepo.getAll();
		assertNotNull(results);
		assertTrue(results.size() > 1);
	}
	
	@Test
	public void testRemove() throws Exception {
		UserInfo userInfo = new DefaultUserInfo();
		Address addr = new Address();
		addr.setLocality("Chelmsford");
		addr.setRegion("Massachusetts");
		addr.setPostalCode("01824");
		addr.setStreetAddress("1 Penny Lane");
		
		userInfo.setUserId("jdoe");
		userInfo.setAddress(addr);
		userInfo.setEmail("jdoe@aol.com");

		userinforepo.save((DefaultUserInfo) userInfo);
		
		UserInfo jdoe = userinforepo.getByUserId("jdoe");
		assertNotNull(jdoe);
		
		userinforepo.remove(userInfo);
		jdoe = userinforepo.getByUserId("jdoe");
		assertNull(jdoe);
	}
	
	@Test
	public void testRemoveById() throws Exception {
		UserInfo userInfo = new DefaultUserInfo();
		Address addr = new Address();
		addr.setLocality("Chelmsford");
		addr.setRegion("Massachusetts");
		addr.setPostalCode("01824");
		addr.setStreetAddress("1 Penny Lane");
		
		userInfo.setUserId("jdoe");
		userInfo.setAddress(addr);
		userInfo.setEmail("jdoe@aol.com");

		userinforepo.save((DefaultUserInfo) userInfo);
		
		UserInfo jdoe = userinforepo.getByUserId("jdoe");
		assertNotNull(jdoe);
		
		userinforepo.removeByUserId("jdoe");
		jdoe = userinforepo.getByUserId("jdoe");
		assertNull(jdoe);
	}
}
