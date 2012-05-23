<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="org.mitre.openid.connect.repository.db.util.*,java.util.*" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %> 
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<o:head title="User Management - ${label} User Page" />
<%
	String base = (String) request.getAttribute("base"); // Calculated in o:head
	String label = (String) pageContext.getAttribute("label");
	List<Breadcrumb> bc = new java.util.ArrayList<Breadcrumb>();
	bc.add(new Breadcrumb("Home", base + "/"));
	bc.add(new Breadcrumb("Manage Users", base + "/users/manageUsers"));
	if (label == null) {
		label = "New";
	}
	bc.add(new Breadcrumb(label + " User"));
	request.setAttribute("bc", bc);
%>
<body>
<script type="text/javascript" src="${base}/resources/js/add_user.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	usr.populate();
});
</script>
<o:header current_user="" current_role="" breadcrumbs="${bc}" />
<h2 class="span12">${label} User</h2>
<form action="${base}/users/editUser" method="POST">
<label class="span2" for="title_field">Title</label>
<select id="title_field" class="span2>
	<option value="Dr">Dr</option>
	<option value="Mr">Mr</option>
	<option value="Ms">Ms</option>
	<option value="Mrs">Mrs</option>
	<option value="Miss">Miss</option>
</select>
<br>
<label class="span2" for="first_name_field">First Name</label>
<input id="first_name_field" class="span4" size="40" />
<br>
<label class="span2" for="last_name_field">Last Name</label>
<input id="last_name_field" class="span4" size="40" />
<br>
<label class="span2" for="email_field">Email</label>
<input id="email_field" class="span4" size="40" />
<form:errors path="email" />
<br>
<label class="span2" for="password_field">Password</label>
<input type='password' id="password_field" class="span4" size="32" />
<form:errors path="password" />
<br>
<label class="span2" for="password_repeat_field">Re-Type Password</label>
<input type='password' id="password_repeat_field" class="span4" />
<br>
<label class="span2" for="role">Clinical role</label>
<select id="role" span="span3">
	<option value="PATIENT">Patient</option>
	<option value="CLINICIAN">Clinician</option>
</select>
<br>
<input class="span1" type='submit' name="Add User' value='Add'>
<input class="offset1 span1" type='submit' name="Cancel' value='Cancel'>
<input type='hidden' id="user_id" />
<input type='hidden' id="user" value="${user}" />
</form>
</body>
</html>