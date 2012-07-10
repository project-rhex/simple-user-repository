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

import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.SortBy;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mitre.openid.connect.repository.UserManager;
import org.mitre.openid.connect.repository.db.model.Role;
import org.mitre.openid.connect.repository.db.model.User;
import org.mitre.openid.connect.repository.db.model.UserAttribute;
import org.mitre.openid.connect.repository.db.util.ParseRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handle user requests
 * @author DRAND
 *
 */
@PreAuthorize("hasRole('ROLE_ADMIN')") 
@Controller
@RequestMapping("/users")
public class UserController {
		
	private static final String SUCCESS_TRUE = "{ \"success\": true }";
	@Autowired
	private UserInfoRepository userinfo;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PasswordEncoder simplePasswordEncoder;
	private SecureRandom random = new SecureRandom();
	private int count = 20;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public @ResponseBody String findRange() {
		Gson gson = new Gson();
		JsonArray userArray = new JsonArray();
		Collection<? extends UserInfo> allUsers = userinfo.getAll();
		for (Iterator iterator = allUsers.iterator(); iterator.hasNext();) {
            UserInfo userInfo = (UserInfo) iterator.next();
            JsonObject el = (JsonObject) gson.toJsonTree(userInfo);
            userArray.add(el);
        }
		
		return userArray.toString();
	}
	
	@RequestMapping("/paginator")
	public @ResponseBody String paginator(@RequestParam("page") Integer page_number, @RequestParam("sort_on") String sortOn, HttpServletRequest request) {
		String base = "";
		try {
			base = ParseRequestContext.parseContext(request.getRequestURL().toString());
		} catch (MalformedURLException e) {
			//
		}
		StringBuilder sb = new StringBuilder(160);
		int page = page_number != null ? page_number : 0;
		int total = userManager.count();
		int page_count = total / count;
		if (total % count != 0) {
			page_count++; // Need an extra page if there are remainders
		}
		page_count = Math.max(1, page_count);
		sb.append("<span class='paginator'>");
		for(int p = 0; p < page_count; p++) {
			String label;
			if (p == 0) {
				label = "First";
			} else if ((page_count - p) == 1) {
				label = "Last";
			} else {
				label = Integer.toString(p);
			}
			if (p == page) {
				sb.append("<span class='page'>" + label + "</span>");
			} else {
				sb.append("<span class='page_link'><a href='");
				sb.append(base);
				sb.append("/users/manageUsers?page=" + p + "&sort_on=" + sortOn + "'>" + label + "</a></span>");
			}
		}
		sb.append("</span>");
		return sb.toString();
	}
	
	@RequestMapping("/manageUsers")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ModelAndView manageUsers(@RequestParam(value="page", defaultValue="0") Integer page, 
			@RequestParam(value="sort_on", defaultValue="FIRST_NAME") String sortOn) {
		int first = page * count;
		SortBy sortBy = SortBy.valueOf(sortOn);
		
		ModelAndView mav = new ModelAndView("users/manageUsers");
		if (page != null) mav.addObject("page", page);
		if (StringUtils.isNotBlank(sortOn)) mav.addObject("sortOn", SortBy.valueOf(sortOn));
		return mav;
	}
	
	@RequestMapping("/addUser")
	public ModelAndView addUser() {
		return editUser(null);
	}
	
	@RequestMapping(value = "/editUser/{id}", method = RequestMethod.GET) 
	public ModelAndView editUser(@PathVariable Long id) {
		ModelAndView mav = new ModelAndView("users/addOrEditUser");
		if (id == null) {
			mav.addObject("label", "Add");
			mav.addObject("userid", "-1");
		} else {
			User user = userManager.findById(id);
			UserInfo info = userinfo.getByUserId(user.getUsername());
			mav.addObject("label", "Edit");
			mav.addObject("userid", id.toString());
			mav.addObject("first_name_field", info.getGivenName());
			mav.addObject("last_name_field", info.getFamilyName());
			mav.addObject("email_field", info.getEmail());
			boolean clinician = false;
			boolean admin = false;
			for(Role r : user.getRoles()) {
				if ("clinician".equalsIgnoreCase(r.getName())) {
					clinician = true; 
				} else if ("admin".equalsIgnoreCase(r.getName())) {
					admin = true;
				}
			}
			mav.addObject("role_field", clinician ? "CLINICIAN" : "PATIENT");
			mav.addObject("is_admin", admin);
			// Copy other user attributes as _field values
			Map<String,String> attrs = attrsToMap(user);
			Set<String> keys = attrs.keySet();
			keys.remove("title_field");
			keys.remove("user-id_field");
			keys.remove("first_name_field");
			keys.remove("last_name_field");
			mav.addObject("properties", keys);
			mav.addObject("propertymap", attrs);
		}
		return mav;
	}
	
	/**
	 * Convert the user attributes in the user object into an attribute - value map
	 * of the keys and value pairs.
	 * @param user the user object, assumed not <code>null</code>
	 * @return the map of keys and values, e.g. title_field: foo. Note that the keys 
	 * will have the string "_field" appended unless they already end in "_field" 
	 */
	private Map<String, String> attrsToMap(User user) {
		Map<String, String> rval = new HashMap<String, String>();
		for(UserAttribute attr : user.getAttributes()) {
			if (attr.getType() == UserAttribute.REMOTE_TYPE) continue;
			String key = attr.getName();
			if (! key.endsWith("_field")) {
				key = key.toLowerCase() + "_field";
			}
			rval.put(key, attr.getValue());
		}
		return rval;
	}

	@RequestMapping(value = "/{userid}", method = RequestMethod.DELETE)
	public HttpEntity<String> deleteUser(@PathVariable Long userid) {
		userManager.delete(userid);
		return new ResponseEntity<String>(SUCCESS_TRUE, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public @ResponseBody String getUserData(@PathVariable Long id) {
		User user = userManager.findById(id);
		Gson gson = new Gson();
		return gson.toJson(user);
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST)
	public @ResponseBody String postUserData(@RequestBody String userJson) {
	    processUserData(userJson, null);
	    return SUCCESS_TRUE;
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public @ResponseBody String putUserData(@PathVariable Long id, @RequestBody String userJson) {
	    processUserData(userJson, id);
		return SUCCESS_TRUE;
	}
	
	private void processUserData(String userJson, Long userId) {
        Gson gson = new Gson();
        User postedUser = null;
        JsonParser parser = new JsonParser();
        JsonElement obj = parser.parse(userJson);
        postedUser = gson.fromJson(obj, User.class);
        if (obj.isJsonObject()) {
            String password = obj.getAsJsonObject().get("password").getAsString();
            if (StringUtils.isNotBlank(password)) {
	            Integer salt = random.nextInt();
	            postedUser.setPasswordHash(simplePasswordEncoder.encodePassword(password, salt));
	            postedUser.setJamesPasswordHash(postedUser.encodeJamesPasswordHash(password));
	            postedUser.setPasswordSalt(salt);
            } else {
            	User original = userManager.findById(userId);
            	if (original == null) {
            		throw new RuntimeException("Couldn't find original user to retrieve password information from");
            	}
            	postedUser.setPasswordHash(original.getPasswordHash());
            	postedUser.setJamesPasswordHash(original.getJamesPasswordHash());
            	postedUser.setPasswordSalt(original.getPasswordSalt());
            }
        }
        // Grab other attributes - the json is not really a User serialization
        boolean patient = false, clinician = false;
        JsonObject data = (JsonObject) obj;
        for(Entry<String, JsonElement> entry : data.entrySet()) {
        	String key = entry.getKey();
        	JsonElement value = entry.getValue();
        	if (key.startsWith("password") || "email".equals(key)) continue;
        	if (key.equals("patient")) {
        		patient = true;
        		continue;
        	}
        	if (key.equals("clinician")) {
        		clinician = true;
        		continue;
        	}
        	if (postedUser.getAttributes() == null) {
        		postedUser.setAttributes(new HashSet<UserAttribute>());
        	}
        	if (key.contains("role")) continue; // Skip roles
        	if (StringUtils.isBlank(value.getAsString())) continue;
        	postedUser.getAttributes().add(new UserAttribute(key.toUpperCase(), value.getAsString()));
        }
        postedUser.setUsername(postedUser.getEmail());
        
        if (userId != null) { 
            postedUser.setId(userId);
        }
        
        // Clear and add new roles
        postedUser.getRoles().clear();
        if (clinician = true) {
        	postedUser.getRoles().add(userManager.findOrCreateRole("CLINICIAN"));
        } else {
        	postedUser.getRoles().add(userManager.findOrCreateRole("PATIENT"));
        }
        JsonElement admin_role = data.get("admin_role");
        if (admin_role != null) {
        	postedUser.getRoles().add(userManager.findRole("ADMIN"));
        }
        
        userManager.save(postedUser);
	}
	
	/**
	 * @return the um
	 */
	public UserManager getUm() {
		return userManager;
	}

	/**
	 * @param um the um to set
	 */
	public void setUm(UserManager um) {
		this.userManager = um;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
