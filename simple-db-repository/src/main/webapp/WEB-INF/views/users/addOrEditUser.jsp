<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" import="org.mitre.openid.connect.repository.db.util.*,java.util.*,org.apache.commons.lang.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<o:header title="User Management - ${label} User Page" />
<o:topbar />
<o:includes />
<%
	String base = (String) request.getAttribute("base"); // Calculated in o:head
	String label = (String) request.getAttribute("label");
	List<Breadcrumb> bc = new java.util.ArrayList<Breadcrumb>();
	bc.add(new Breadcrumb("Home", base + "/"));
	bc.add(new Breadcrumb("Manage Users", base + "/users/manageUsers"));
	if (label == null) {
		label = "New";
		pageContext.setAttribute("applyLabel", "Add User");
	} else {
		pageContext.setAttribute("applyLabel", "Save");
	}
	bc.add(new Breadcrumb(label + " User"));
	request.setAttribute("bc", bc);
%>
<o:header />
<o:breadcrumbs-db breadcrumbs="${bc}" />
<h2 class="span12">${label} User</h2>
<div class="container">
<div class="row">
<label class="span2" for="title_field">Title</label>
<select id="title_field" class="span2">
	<option value="Dr" <c:if test="${title_field == 'Dr'}">selected</c:if> >Dr</option>
	<option value="Mr" <c:if test="${title_field == 'Mr'}">selected</c:if> >Mr</option>
	<option value="Ms" <c:if test="${title_field == 'Ms'}">selected</c:if> >Ms</option>
	<option value="Mrs" <c:if test="${title_field == 'Mrs'}">selected</c:if> >Mrs</option>
	<option value="Miss" <c:if test="${title_field == 'Miss'}">selected</c:if> >Miss</option>
</select>
</div>
<div class="row">
<label class="span2" for="firstname_field">First Name</label>
<input id="firstname_field" class="span4" size="48" value="${firstname_field}">
</div>
<div class="row">
<label class="span2" for="middlename_field">Middle Name</label>
<input id="middlename_field" class="span4" size="48" value="${middlename_field}">
</div>
<div class="row">
<label class="span2" for="lastname_field">Last Name</label>
<input id="lastname_field" class="span4" size="48" value="${lastname_field}">
</div>
<div class="row">
<label class="span2" for="nickname_field">Nickname</label>
<input id="nickname_field" class="span4" size="48" value="${nickname_field}">
</div>
<div class="row">
<label class="span2" for="gender_field">Gender</label>
<select id="gender_field" span="span3">
   <option value="M" <c:if test="${gender_field == 'M'}">selected</c:if> >Male</option>
   <option value="F" <c:if test="${gender_field == 'F'}">selected</c:if> >Female</option>
</select>
</div>
<div class="row">
<label class="span2" for="email_field">Email</label>
<input id="email_field" class="span4" size="64" value="${email_field}">
<span id="email_errors" class="span3 errors"></span>
</div>
<div class="row">
<label class="span2" for="street_field">Street</label>
<input id="street_field" class="span4" size="64" value="${street_field}">
</div>
<div class="row">
<label class="span2" for="locality_field">Locality</label>
<input id="locality_field" class="span4" size="32" value="${locality_field}">
</div>
<div class="row">
<label class="span2" for="region_field">Region</label>
<input id="region_field" class="span4" size="32" value="${region_field}">
</div>
<div class="row">
<label class="span2" for="postalCode_field">Postal Code</label>
<input id="postalCode_field" class="span4" size="32" value="${postalCode_field}">
</div>
<div class="row">
<label class="span2" for="password_field">Password</label>
<input type='password' id="password_field" class="span4" size="32">
<span id="password_errors" class="span3 errors"></span>
</div>
<div class="row">
<label class="span2" for="password_repeat_field">Re-Type Password</label>
<input type='password' id="password_repeat_field" class="span4">
</div>
<div class="row">
<label class="span2" for="phone_field">Phone number</label>
<input id="phone_field" class="span4" size="32" value="${phone_field}">
</div>
<div class="row">
<label class="span2" for="profile_field">Profile</label>
<input id="profile_field" class="span4" size="80" value="${profile_field}">
</div>
<div class="row">
<label class="span2" for="website_field">Website</label>
<input id="website_field" class="span4" size="80" value="${website_field}">
</div>
<div class="row">
<label class="span2" for="picture_field">Picture</label>
<input id="picture_field" class="span4" size="80" value="${picture_field}">
</div>
<div class="row">
<label class="span2" for="role">Clinical role</label>
<select id="role_field" span="span3">
   <option value="PATIENT" <c:if test="${role_field == 'PATIENT'}">selected</c:if> >Patient</option>
   <option value="CLINICIAN" <c:if test="${role_field == 'CLINICIAN'}">selected</c:if> >Clinician</option>
</select>
</div>
<div class="row">
<label class="span2" for="admin_role_field">Is Admin</label>
<input type="checkbox" id="admin_role_field" <c:if test="${is_admin}">checked</c:if> >
</div>
<div class="row">
<span class="span2">&nbsp;</span>
<input id="addProperty" class="span2" type="button" value="Add Property" title="Add Property">
</div>
<div id="property_marker" class="row">
</div>
<c:forEach var="prop" items="${properties}">
<c:set var="pval" value="${propertymap[prop]}"/>
<div class="row">
<% 
String prop = (String) pageContext.getAttribute("prop"); 
prop = prop.substring(0,prop.length() - 6);
%>
<label class="span2" for="${prop}_field"><%= StringUtils.capitalize(prop.replace("_"," ")) %></label>
<input id="${prop}_field" class="span4" size="40" value="${pval}">
</div>
</c:forEach>
<script type="text/javascript">
$(document).ready(function() {
<c:forEach var="prop" items="${properties}">
	usr.properties[usr.properties.length] = '${prop}';
</c:forEach>
</script>
<div class="row">
<span class="span1">&nbsp;</span>
<input id="add" class="span2" type='button' value="${applyLabel}" title="${applyLabel}">
<input id="cancel" class="span2" type='button' value="Cancel" title="Cancel">
</div>
<input type='hidden' id="user_id" value="${userid}" />
</div>
<o:copyright />
<o:footer-db include="add_user" />