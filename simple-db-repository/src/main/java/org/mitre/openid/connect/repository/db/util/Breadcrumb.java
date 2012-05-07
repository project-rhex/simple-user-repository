package org.mitre.openid.connect.repository.db.util;

import org.apache.commons.lang.StringUtils;

/**
 * Represent a single breadcrumb
 * @author DRAND
 *
 */
public class Breadcrumb {
	private String title;
	private String link;
	
	public Breadcrumb(String title) {
		this(title, null);
	}
	
	public Breadcrumb(String title, String link) {
		if (StringUtils.isBlank(title)) {
			throw new IllegalArgumentException("Title may not be blank or null");
		}
		this.title = title;
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Breadcrumb other = (Breadcrumb) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
