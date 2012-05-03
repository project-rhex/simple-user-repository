<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<o:head title="User Management - User List Page" />
<body>
<script type="text/javascript" src="manage_users.js"></script>
<script type="text/javascript">
$(document).ready({
	
});
</script>

<o:header current_user="" current_role="" breadcrumbs="" />
<h1>Manage Users</h1>
<a href="/users/add">Add User</a>
<div id="#users"></div>
</body>
</html>