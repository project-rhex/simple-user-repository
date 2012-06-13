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

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.SortBy;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mitre.openid.connect.repository.UserManager;
import org.mitre.openid.connect.repository.db.model.User;
import org.mitre.openid.connect.repository.db.model.UserAttribute;
import org.mitre.openid.connect.repository.db.util.ParseRequestContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
// @PreAuthorize("hasRole('ROLE_ADMIN')") 
@Controller
@RequestMapping("/users")
public class UserController {
		
	@Resource
	private UserInfoRepository userinfo;
	@Resource
	private UserManager userManager;
	private int count = 20;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public @ResponseBody String findRange() {
		Gson gson = new Gson();
		JsonArray userArray = new JsonArray();
		Collection<? extends UserInfo> allUsers = userinfo.getAll();
		for (Iterator iterator = allUsers.iterator(); iterator.hasNext();) {
            UserInfo userInfo = (UserInfo) iterator.next();
            userArray.add(gson.toJsonTree(userInfo));
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
			mav.addObject("label", "Edit");
			mav.addObject("userid", id.toString());
		}
		return mav;
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public HttpEntity<String> deleteUser(@PathVariable Long id) {
		userManager.delete(id);
		return new ResponseEntity<String>("{ success: true }", HttpStatus.OK);
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
	    return "{ success: true }";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public @ResponseBody String putUserData(@PathVariable Long id, @RequestBody String userJson) {
	    processUserData(userJson, id);
		return "{ success: true }";
	}
	
	private void processUserData(String userJson, Long userId) {
        Gson gson = new Gson();
        User postedUser = null;
        try {
            JsonParser parser = new JsonParser();
            JsonElement obj = parser.parse(userJson);
            postedUser = gson.fromJson(obj, User.class);
            if (obj.isJsonObject()) {
                String password = obj.getAsJsonObject().get("password").getAsString();
                postedUser.createPassword(password, userManager);
            }
            // Grab other attributes - the json is not really a User serialization
            for(Entry<String, JsonElement> entry : ((JsonObject) obj).entrySet()) {
            	String key = entry.getKey();
            	JsonElement value = entry.getValue();
            	if (key.startsWith("password") || "email".equals(key)) continue;
            	if (postedUser.getAttributes() == null) {
            		postedUser.setAttributes(new HashSet<UserAttribute>());
            	}
            	postedUser.getAttributes().add(new UserAttribute(key.toUpperCase(), value.getAsString()));
            }
            postedUser.setUsername(postedUser.getEmail());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read serialized user object", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't hash the password", e);
        }
        
        if (userId != null) { 
            postedUser.setId(userId);
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
