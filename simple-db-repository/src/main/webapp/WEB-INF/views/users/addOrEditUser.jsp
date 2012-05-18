<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="org.mitre.openid.connect.repository.db.util.*,java.util.*" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %> 
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%
	String label = (String) pageContext.getAttribute("label");
	List<Breadcrumb> bc = new java.util.ArrayList<Breadcrumb>();
	bc.add(new Breadcrumb("Home", "${base}/"));
	bc.add(new Breadcrumb("Manage Users", "${base}/manageUsers"));
	bc.add(new Breadcrumb(label + " User"));
	request.setAttribute("bc", bc);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<o:head title="User Management - ${label} User Page" />
<body>
<o:header current_user="" current_role="" breadcrumbs="${bc}" />
<h2 class="span12">${label} User</h2>
<form action="${base}/users/editUser" method="POST">
<label class="span4" for="title_field">Title</label>
<select id="title_field" class="span8">
	<option value="Dr"/>
	<option value="Mr"/>
	<option value="Ms"/>
	<option value="Mrs"/>
	<option value="Miss"/>
</select>
<label class="span4" for="first_name_field">First Name</label>
<input id="first_name_field" class="span8" size="40" />
<label class="span4" for="last_name_field">Last Name</label>
<input id="last_name_field" class="span8" size="40" />
<label class="span4" for="email_field">Email</label>
<input id="email_field" class="span8" size="40" />
<label class="span4" for="password_field">Password</label>
<input type='password' id="password_field" class="span8" size="32" />
<label class="span4" for="pw_field2">Re-Type Password</label>
<input type='password' id="pw_field2" class="span8" />
<label class="span4" for="role">Clinical role</label>
<select id="role" span="span8">
	<option value="patient"/>
	<option value="clinician"/>
</select>
<input type='hidden' id="user_id"/>
</form>
</body>
</html>