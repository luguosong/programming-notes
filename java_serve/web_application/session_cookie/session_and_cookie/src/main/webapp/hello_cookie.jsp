<%@ page import="java.io.PrintWriter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<div>
    <%
        PrintWriter writer = response.getWriter();
        writer.println("当前Cookie：");
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            writer.println(cookie.getName());
            writer.println(cookie.getValue() + ";  ");
        }
    %>
</div>
<a href="addCookie">添加Cookie</a>
<a href="deleteCookie">清除Cookie</a>

</body>
</html>
