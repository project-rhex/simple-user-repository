package org.mitre.openid.connect.repository.db;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Test;
import org.mitre.openid.connect.repository.db.util.ParseRequestContext;

public class TestParseRequestContext {

	@Test
	public void test() throws MalformedURLException {
		assertNull(ParseRequestContext.parseContext("http://foo"));
		assertEquals("/simple", ParseRequestContext.parseContext("http://foo/simple"));
		assertEquals("/simple", ParseRequestContext.parseContext("http://foo/simple/bar"));
	}

}
