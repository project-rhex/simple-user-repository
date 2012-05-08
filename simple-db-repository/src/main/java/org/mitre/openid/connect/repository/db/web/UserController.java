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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.repository.db.UserManager;
import org.mitre.openid.connect.repository.db.UserManager.SortBy;
import org.mitre.openid.connect.repository.db.util.ParseRequestContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Handle user requests
 * @author DRAND
 *
 */
@Controller
@RequestMapping("/users")
public class UserController {
	static class Results {
		int page;
		int count;
		int total;
		List<Map<String,String>> results;
	}
		
	@Resource
	private UserManager userManager;
	private int count = 20;
	
	@RequestMapping("/index")
	public @ResponseBody String findRange(@RequestParam("page") Integer page_number, @RequestParam("sort_on") String sortOn) {
		int page = page_number != null ? page_number : 0;
		int first = page * count;
		SortBy sortBy = SortBy.valueOf(StringUtils.isNotBlank(sortOn) ? sortOn : "FIRST_NAME");
		
		List<Map<String, String>> values = userManager.findInRange(first, count, sortBy);
		
		Gson gson = new Gson();
		Results h = new Results();
		h.page = page;
		h.count = count;
		h.total = userManager.count();
		h.results = values;
		return gson.toJson(h);
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
