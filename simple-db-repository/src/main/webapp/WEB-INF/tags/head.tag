<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>    
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>${title}</title>
<c:set var="min" value="false"></c:set>
<c:choose>
<c:when test="${min}">
<link href="stylesheets/bootstrap.min.css" rel="stylesheet">
<link href="stylesheets/bootstrap-responsive.min.css" rel="stylesheet">

<script type="text/javascript" src="javascript/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="javascript/bootstrap.min.js"></script> 
</c:when>
<c:otherwise>
<link href="stylesheets/bootstrap.css" rel="stylesheet">
<link href="stylesheets/bootstrap-responsive.css" rel="stylesheet">

<script type="text/javascript" src="javascript/jquery-1.7.2.dev.js"></script>
<script type="text/javascript" src="javascript/bootstrap.js"></script>
</c:otherwise>
</c:choose> 
</head>
