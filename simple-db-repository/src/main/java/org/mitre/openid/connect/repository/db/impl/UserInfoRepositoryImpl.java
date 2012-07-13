package org.mitre.openid.connect.repository.db.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.StandardAttributes;
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
		addUserAttribute(user, StandardAttributes.LAST_NAME, userInfo.getFamilyName());
		addUserAttribute(user, StandardAttributes.FIRST_NAME, userInfo.getGivenName());
		addUserAttribute(user, StandardAttributes.LOCALE, userInfo.getLocale());
		addUserAttribute(user, StandardAttributes.MIDDLE_NAME, userInfo.getMiddleName());
		addUserAttribute(user, StandardAttributes.NAME, userInfo.getName());
		addUserAttribute(user, StandardAttributes.NICKNAME, userInfo.getNickname());
		addUserAttribute(user, StandardAttributes.PHONE_NUMBER, userInfo.getPhoneNumber());
		addUserAttribute(user, StandardAttributes.PICTURE, userInfo.getPicture());
		addUserAttribute(user, StandardAttributes.PROFILE, userInfo.getProfile());
		addUserAttribute(user, StandardAttributes.WEBSITE, userInfo.getWebsite());
		addUserAttribute(user, StandardAttributes.ZONEINFO, userInfo.getZoneinfo());
		addUserAttribute(user, StandardAttributes.GENDER, userInfo.getGender());
		if (userInfo.getAddress() != null) {
			Address addr = userInfo.getAddress();
			addUserAttribute(user, StandardAttributes.FORMATTED_ADDRESS, addr.getFormatted());
			addUserAttribute(user, StandardAttributes.STREET_ADDRESS, addr.getStreetAddress());
			addUserAttribute(user, StandardAttributes.LOCALITY, addr.getLocality());
			addUserAttribute(user, StandardAttributes.REGION, addr.getRegion());
			addUserAttribute(user, StandardAttributes.POSTAL_CODE, addr.getPostalCode());
			addUserAttribute(user, StandardAttributes.COUNTRY, addr.getCountry());
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
		Collection<UserAttribute> attrs = user.getAttributes();
		Map<String, String> amap = attributesToMap(attrs);
		PropertiedUserInfo info = new PropertiedUserInfo();
		info.setEmail(user.getEmail());
		info.setFamilyName(amap.get(StandardAttributes.LAST_NAME.name()));
		amap.remove(StandardAttributes.LAST_NAME.name());
		info.setGender(amap.get(StandardAttributes.GENDER.name()));
		amap.remove(StandardAttributes.GENDER.name());
		info.setGivenName(amap.get(StandardAttributes.FIRST_NAME.name()));
		amap.remove(StandardAttributes.FIRST_NAME.name());
		info.setLocale(amap.get(StandardAttributes.LOCALE.name()));
		amap.remove(StandardAttributes.LOCALE.name());
		info.setMiddleName(amap.get(StandardAttributes.MIDDLE_NAME.name()));
		amap.remove(StandardAttributes.MIDDLE_NAME.name());
		info.setName(amap.get(StandardAttributes.NAME.name()));
		amap.remove(StandardAttributes.NAME.name());
		info.setNickname(amap.get(StandardAttributes.NICKNAME.name()));
		amap.remove(StandardAttributes.NICKNAME.name());
		info.setPhoneNumber(amap.get(StandardAttributes.PHONE_NUMBER.name()));
		amap.remove(StandardAttributes.PHONE_NUMBER.name());
		info.setPicture(amap.get(StandardAttributes.PICTURE.name()));
		amap.remove(StandardAttributes.PICTURE.name());
		info.setProfile(amap.get(StandardAttributes.PROFILE.name()));
		amap.remove(StandardAttributes.PROFILE.name());
		info.setUpdatedTime(amap.get(StandardAttributes.UPDATED_TIME.name()));
		amap.remove(StandardAttributes.UPDATED_TIME.name());
		info.setUserId(user.getUsername());
		info.setEmailVerified(user.getEmailConfirmed());
		info.setWebsite(amap.get(StandardAttributes.WEBSITE.name()));
		amap.remove(StandardAttributes.WEBSITE.name());
		info.setZoneinfo(amap.get(StandardAttributes.ZONEINFO.name()));
		amap.remove(StandardAttributes.ZONEINFO.name());
		String street = amap.get(StandardAttributes.STREET_ADDRESS.name());
		amap.remove(StandardAttributes.STREET_ADDRESS.name());
		String faddr = amap.get(StandardAttributes.FORMATTED_ADDRESS.name());
		amap.remove(StandardAttributes.FORMATTED_ADDRESS.name());
		String locality = amap.get(StandardAttributes.LOCALITY.name());
		amap.remove(StandardAttributes.LOCALITY.name());
		String region = amap.get(StandardAttributes.REGION.name());
		amap.remove(StandardAttributes.REGION.name());
		String postal = amap.get(StandardAttributes.POSTAL_CODE.name());
		amap.remove(StandardAttributes.POSTAL_CODE.name());
		String country = amap.get(StandardAttributes.COUNTRY.name());
		amap.remove(StandardAttributes.COUNTRY.name());
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
	private void addUserAttribute(User user, StandardAttributes name, String value) {
		if (StringUtils.isNotBlank(value)) {
			if (user.getAttributes() == null) {
				user.setAttributes(new HashSet<UserAttribute>());
			}
			user.getAttributes().add(new UserAttribute(name, value));
		}
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
