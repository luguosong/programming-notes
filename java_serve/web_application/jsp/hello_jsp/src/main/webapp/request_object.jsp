<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>request对象</title>
</head>
<body>
<%--使用request对象获取应用的根路径--%>
<a href="<%=request.getContextPath()%>/java_in_jsp1.jsp">页面跳转</a>

</body>
</html>
