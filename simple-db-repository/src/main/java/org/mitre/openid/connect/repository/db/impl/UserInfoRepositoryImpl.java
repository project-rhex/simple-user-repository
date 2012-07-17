package org.mitre.openid.connect.repository.db.impl;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mitre.openid.connect.repository.UserManager;
import org.mitre.openid.connect.repository.db.data.PropertiedUserInfo;
import org.mitre.openid.connect.repository.db.model.User;
import org.mitre.openid.connect.repository.db.model.UserAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
@Primary
/**
 * Note that user id in the userinfo is the user name in the database, not the database primary key or id.
 * Be careful!
 * 
 * @author DRAND
 *
 */
public class UserInfoRepositoryImpl implements UserInfoRepository {
	private static final Logger logger = LoggerFactory
			.getLogger(UserInfoRepositoryImpl.class);
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PasswordEncoder simplePasswordEncoder;
	private SecureRandom random = new SecureRandom();
	@PersistenceContext
	private EntityManager em;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.UserInfoRepository#getByUserId(java.lang.String)
	 */
	public PropertiedUserInfo getByUserId(String userId) {
		if (userId == null || userId.trim().length() == 0) {
			throw new IllegalArgumentException(
					"userId should never be null or empty");
		}
		User user = userManager.get(userId);
		if (user != null) {
			return userToUserInfo(user);
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.UserInfoRepository#save(org.mitre.openid.connect.model.UserInfo)
	 */
	public UserInfo save(UserInfo userInfo) {
		if (userInfo == null) {
			throw new IllegalArgumentException(
					"userInfo should never be null");
		}
		String userId = userInfo.getUserId();
		if (userId == null || userId.trim().length() == 0) {
			throw new IllegalArgumentException(
					"userId should never be null or empty");
		}
		User user = userManager.get(userId);
		if (user == null) {
			user = new User();
			user.setUsername(userId);
			Long password = RandomUtils.nextLong();
			Integer salt = random.nextInt();
			user.setPasswordHash(simplePasswordEncoder.encodePassword(password.toString(), salt));
            //System.out.println("GG2");
            user.setJamesPasswordHash(user.encodeJamesPasswordHash(password.toString()));
			user.setPasswordSalt(salt);
		}
		/**
		 * Set user information from userInfo
		 */
		if (StringUtils.isNotBlank(userInfo.getEmail()))
			user.setEmail(userInfo.getEmail());
			
		user.setLastname(userInfo.getFamilyName());
		if (StringUtils.isNotBlank(userInfo.getGivenName())) {
			user.setFirstname(userInfo.getGivenName());
		} else if (StringUtils.isNotBlank(userInfo.getName())) {
			user.setFirstname(userInfo.getName());	
		}
		user.setLocale(userInfo.getLocale());
		user.setMiddlename(userInfo.getMiddleName());
		user.setNickname(userInfo.getNickname());
		user.setPhone(userInfo.getPhoneNumber());
		user.setPicture(userInfo.getPicture());
		user.setProfile(userInfo.getProfile());
		user.setWebsite(userInfo.getWebsite());
		user.setZoneinfo(userInfo.getZoneinfo());
		user.setGender(userInfo.getGender());
		if (userInfo.getAddress() != null) {
			Address addr = userInfo.getAddress();
			user.setFormattedAddress(addr.getFormatted());
			user.setStreet(addr.getStreetAddress());
			user.setLocality(addr.getLocality());
			user.setRegion(addr.getRegion());
			user.setPostalCode(addr.getPostalCode());
			user.setCountry(addr.getCountry());
		}
		user.setEmailConfirmed(userInfo.getEmailVerified());
		if (userInfo instanceof PropertiedUserInfo) {
			PropertiedUserInfo pui = (PropertiedUserInfo) userInfo;
			for(String key : pui.keySet()) {
				String value = pui.getProperty(key);
				// Skip any extended property that starts with _, indicates something internal like _USER_ID
				if (key.charAt(0) == '_') continue;
				addUserAttribute(user, key, value);
			}
		}
		userManager.save(user);
		
		return userInfo;
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.UserInfoRepository#remove(org.mitre.openid.connect.model.UserInfo)
	 */
	public void remove(UserInfo userInfo) {
		removeByUserId(userInfo.getUserId());
	}
	

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.UserInfoRepository#removeByUserId(java.lang.String)
	 */
	public void removeByUserId(String userId) {
		if (userId == null || userId.trim().length() == 0) {
			throw new IllegalArgumentException(
					"userId should never be null or empty");
		}
		userManager.delete(userId); // Insures that all cascading, etc. is done
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.UserInfoRepository#getAll()
	 */
	public Collection<UserInfo> getAll() {
		TypedQuery<User> uq = (TypedQuery<User>) em.createNamedQuery("users.all");
		List<User> users = uq.getResultList();
		List<UserInfo> userInfos = new ArrayList<UserInfo>();
		for(User u : users) {
			userInfos.add(userToUserInfo(u));
		}
		return userInfos;
	}

	/**
	 * Convert a user object to a userInfo object. We remove attributes from the map as we 
	 * go so only extended properties are left at the end.
	 * 
	 * @param user
	 * @return
	 */
	private PropertiedUserInfo userToUserInfo(User user) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:MM:ssZ");
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		PropertiedUserInfo info = new PropertiedUserInfo();
		info.setEmail(user.getEmail());
		info.setFamilyName(user.getLastname());
		info.setGender(user.getGender());
		info.setGivenName(user.getFirstname());
		info.setLocale(user.getLocale());
		info.setMiddleName(user.getMiddlename());
		info.setName(user.getFirstname() + " " + user.getLastname());
		info.setNickname(user.getNickname());
		info.setPhoneNumber(user.getPhone());
		info.setPicture(user.getPicture());
		info.setProfile(user.getProfile());
		info.setUpdatedTime(fmt.format(user.getUpdated()));
		info.setUserId(user.getUsername());
		info.setEmailVerified(user.getEmailConfirmed());
		info.setWebsite(user.getWebsite());
		info.setZoneinfo(user.getZoneinfo());
		String street = user.getStreet();
		String faddr = user.getFormattedAddress();
		String locality = user.getLocality();
		String region = user.getRegion();
		String postal = user.getPostalCode();
		String country = user.getCountry();
		if (StringUtils.isNotBlank(street) || StringUtils.isNotBlank(faddr)
				|| StringUtils.isNotBlank(locality)
				|| StringUtils.isNotBlank(region)
				|| StringUtils.isNotBlank(country)
				|| StringUtils.isNotBlank(postal)) {
			Address addr = new Address();
			addr.setFormatted(faddr);
			addr.setStreetAddress(street);
			addr.setLocality(locality);
			addr.setRegion(region);
			addr.setCountry(country);
			addr.setPostalCode(postal);
			info.setAddress(addr);
		}
		Collection<UserAttribute> attrs = user.getAttributes();
		Map<String, String> amap = attributesToMap(attrs);
		// Handle the extended properties
		for(String key : amap.keySet()) {
			String value = amap.get(key);
			info.setProperty(key, value);
		}
		info.setProperty("_USER_ID", user.getId().toString());
		
		return info;
	}

	/**
	 * Convert attributes into a hash map
	 * @param attrs
	 * @return
	 */
	private Map<String, String> attributesToMap(Collection<UserAttribute> attrs) {
		Map<String,String> rval = new HashMap<String, String>();
		for(UserAttribute attr : attrs) {
			if (attr.getType() != UserAttribute.NORMAL_TYPE) continue;
			rval.put(attr.getName(), attr.getValue());
		}
		return rval;
	}
	
	/**
	 * Add the named attribute to the user if not blank/null
	 * @param user
	 * @param attr
	 * @param value
	 */
	private void addUserAttribute(User user, String name, String value) {
		if (StringUtils.isNotBlank(value)) {
			if (user.getAttributes() == null) {
				user.setAttributes(new HashSet<UserAttribute>());
			}
			user.getAttributes().add(new UserAttribute(name, value));
		}
	}
}
