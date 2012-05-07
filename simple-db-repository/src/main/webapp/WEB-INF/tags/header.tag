<%@attribute name="current_user" required="false" %>
<%@attribute name="current_role" required="false" %>
<%@attribute name="breadcrumbs" required="false" type="java.util.List" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="header">
	<img alt="logo" src="/images/logo.gif">
	<span class="user">${current_user}(${current_role})</span>
</div>
<div class="breadcrumbs">
	<c:forEach var="crumb" items="${breadcrumbs}" varStatus="row">
		<c:if test="${row.count > 1}" >></c:if>
		<c:choose>
			<c:when test="${empty crumb.link}">${crumb.title}</c:when>
			<c:otherwise><a href="${crumb.link}">${crumb.title}</a></c:otherwise> 
		</c:choose>
	</c:forEach>
</div>