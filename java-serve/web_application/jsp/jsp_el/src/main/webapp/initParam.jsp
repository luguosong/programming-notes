<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>获取应用初始化参数</title>
</head>
<body>

${initParam.username}
<br><%--等价于--%>
<%=application.getInitParameter("username")%>

</body>
</html>
