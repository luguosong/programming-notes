<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%--在service()方法之外编写代码--%>
<%--❗存在线程安全问题，一般不使用--%>
<%!
    static {
        System.out.println("在service方法外部执行");
    }
%>
</body>
</html>
