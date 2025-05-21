<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<%--获取Session--%>
<%=request.getSession().getAttribute("s")%><br>
<%--Session存活时间查询--%>
<%="Session存活时间："+request.getSession().getMaxInactiveInterval()+"秒"%><br>
<%--上次访问时间间隔--%>
<%="上次访问时间间隔："+(System.currentTimeMillis()-request.getSession().getLastAccessedTime())/1000+"秒"%><br>

<a href="addSession">添加Session</a>
<a href="deleteSession">删除Session</a>
</body>
</html>
