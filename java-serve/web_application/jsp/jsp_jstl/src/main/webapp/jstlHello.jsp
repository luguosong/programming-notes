
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--引入核心标签库--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<%
    String[] fruits = {"Apple", "Banana", "Orange", "Grapes"};
    request.setAttribute("fruits",fruits);
%>

<c:forEach var="fruit" items="${fruits}">
    <div>${fruit}</div>
</c:forEach>

</body>
</html>
