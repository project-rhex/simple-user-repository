<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<% 
String base = org.mitre.openid.connect.repository.db.util.ParseRequestContext.parseContext(request.getRequestURL().toString());
request.setAttribute("base", base);
%>
<link rel="stylesheet/less" type="text/css" href="${base}/resources/stylesheets/users.less">
<script type="text/javascript" src="${base}/resources/js/less-1.3.0.min.js"></script>
