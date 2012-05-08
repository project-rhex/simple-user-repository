<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="org.mitre.openid.connect.repository.db.util.*,java.util.*" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %> 

<%
	String p = request.getParameter("page");
	if (p != null && p.trim().length() > 0) {
		request.setAttribute("page", new Integer(p));
	} else {
		request.setAttribute("page", 0);
	}
	String s = request.getParameter("sort_on");
	if (s != null && s.trim().length() > 0) {
		request.setAttribute("sort_on", s);
	} else {
		request.setAttribute("sort_on", "FIRST_NAME");
	}
	List<Breadcrumb> bc = new java.util.ArrayList<Breadcrumb>();
	bc.add(new Breadcrumb("Home", "/"));
	bc.add(new Breadcrumb("Manage Users"));
	request.setAttribute("bc", bc);
%>   
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<o:head title="User Management - User List Page" />
<body>
<script type="text/javascript" src="${base}/resources/js/manage_users.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	users.set_base("${base}");
	users.loader(${page}, "${sort_on}");
	users.paginator(${page}, "${sort_on}");
});
</script>

<o:header current_user="" current_role="" breadcrumbs="${bc}" />
<h1 class="span12">Manage Users</h1>
<a class="span12" href="/users/add">Add User</a>
<div id="users">
<table id="people" class="table-striped span12">
</table>
<div id="paginator" class="span12"></div>
</div>
</body>
</html>