<%@attribute name="title" required="false" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<% 
String base = org.mitre.openid.connect.repository.db.util.ParseRequestContext.parseContext(request.getRequestURL().toString());
request.setAttribute("base", base);
%>    
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>${title}</title>
<c:set var="min" value="false"></c:set>
<c:choose>
<c:when test="${min}">
<link href="${base}/resources/bootstrap2/css/bootstrap.min.css" rel="stylesheet">
<link href="${base}/resources/bootstrap2/css/bootstrap-responsive.min.css" rel="stylesheet">

<script type="text/javascript" src="${base}/resources/js/jquery.min.js"></script>
<script type="text/javascript" src="${base}/resources/bootstrap2/js/bootstrap.min.js"></script> 
</c:when>
<c:otherwise>
<link href="${base}/resources/bootstrap2/css/bootstrap.css" rel="stylesheet">
<link href="${base}/resources/bootstrap2/css/bootstrap-responsive.css" rel="stylesheet">

<script type="text/javascript" src="${base}/resources/js/jquery.dev.js"></script>
<script type="text/javascript" src="${base}/resources/bootstrap2/js/bootstrap.js"></script>
</c:otherwise>
</c:choose>
<link rel="stylesheet/less" type="text/css" href="${base}/resources/stylesheets/users.less">
<script type="text/javascript" src="${base}/resources/js/backbone-min.js"></script>
<script type="text/javascript" src="${base}/resources/js/less-1.3.0.min.js"></script>
</head>
