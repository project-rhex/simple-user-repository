package org.mitre.openid.connect.repository.db.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

/**
 * Parse the request context from the request URL
 * 
 * @author DRAND
 */
public class ParseRequestContext {
	/**
	 * Parse out the context from a given request URL
	 * @param request a request url, never null or empty
	 * @return the first component after the host and port
	 * @throws MalformedURLException if the request isn't a proper URL
	 */
	public static String parseContext(String request) throws MalformedURLException {
		if (StringUtils.isBlank(request)) {
			throw new IllegalArgumentException("Request is required");
		}
		URL req = new URL(request);
		String pathparts[] = req.getPath().split("/");
		if (pathparts.length > 1) {
			return "/" + pathparts[1];
		} else {
			return null;
		}
	}
}
