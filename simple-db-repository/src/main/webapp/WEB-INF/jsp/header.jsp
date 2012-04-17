<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="header">
	<img alt="logo" src="/images/logo.gif">
	<span class="user">${current_user}(${current_role})</span>
</div>
<div class="breadcrumbs">
	<c:forEach var="crumb" items="${breadcrumbs}" varStatus="row">
		<c:if test="${row.count > 1}" >></c:if>
		<c:choose>
			<c:when test="${crumb.link is empty}">${crumb.title}</c:when>
			<c:otherwise><a href="${crumb.link}">${crumb.title}</a></c:otherwise> 
		</c:choose>
	</c:forEach>
</div>