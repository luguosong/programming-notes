<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

${param.username}
<br><%--等价于--%>
<%=request.getParameter("username")%>
<br>

${paramValues.hobby[0]}
${paramValues.hobby[1]}
${paramValues.hobby[2]}
<br><%--等价于--%>
<%=request.getParameterValues("hobby")%>

</body>
</html>
