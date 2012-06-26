<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="org.mitre.openid.connect.repository.db.util.*,java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %> 
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
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
	String titles[] = {"Dr", "Mr", "Ms", "Mrs", "Miss"};
	request.setAttribute("titles", titles);
%>
<o:header />
<o:breadcrumbs-db breadcrumbs="${bc}" />
<h2 class="span12">${label} User</h2>
<div class="container">
<div class="row">
<label class="span2" for="title_field">Title</label>
<select id="title_field" class="span2">
	<c:forEach var="title" items="${titles}">
		<option value="${title}" <c:if test="${title == title_field}">selected</c:if> >${title}</option>
	</c:forEach>
</select>
</div>
<div class="row">
<label class="span2" for="first_name_field">First Name</label>
<input id="first_name_field" class="span4" size="40" value="${first_name_field}">
</div>
<div class="row">
<label class="span2" for="last_name_field">Last Name</label>
<input id="last_name_field" class="span4" size="40" value="${last_name_field}">
</div>
<div class="row">
<label class="span2" for="email_field">Email</label>
<input id="email_field" class="span4" size="40" value="${email_field}">
<span id="email_errors" class="span3 errors"></span>
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
<label class="span2" for="role">Clinical role</label>
<select id="role_field" span="span3">
   <option value="PATIENT" <c:if test="${role_field == 'PATIENT'}">selected</c:if> >Patient</option>
   <option value="CLINICIAN" <c:if test="${role_field == 'CLINICIAN'}">selected</c:if> >Clinician</option>
</select>
</div>
<div class="row">
<span class="span1">&nbsp;</span>
<input id="add" class="span2" type='button' value="${applyLabel}" title="${applyLabel}">
<input id="cancel" class="span2" type='button' value="Cancel" title="Cancel">
</div>
<input type='hidden' id="user_id" value="${userid}" />
</div>
<o:copyright />
<o:footer-db include="add_user" />