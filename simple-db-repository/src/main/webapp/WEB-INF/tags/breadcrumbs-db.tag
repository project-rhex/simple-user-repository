<%@attribute name="breadcrumbs" required="false" type="java.util.List" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="container">  
<div class="row">  
<div class="span6">  
<ul class="breadcrumb">
	<c:forEach var="crumb" items="${breadcrumbs}" varStatus="row">
		<li>
		<c:if test="${row.count > 1}" ><span class='divider'>></span></c:if>
		<c:choose>
			<c:when test="${empty crumb.link}">${crumb.title}</c:when>
			<c:otherwise><a href="${crumb.link}">${crumb.title}</a></c:otherwise> 
		</c:choose>
		</li>
	</c:forEach>
</ul>  
</div>  
</div>