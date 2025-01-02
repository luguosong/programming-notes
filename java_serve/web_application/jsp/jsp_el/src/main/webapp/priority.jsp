<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>EL表达式的优先级</title>
</head>
<body>
<%
    System.out.println(111);
    pageContext.setAttribute("key","pageContext");
    request.setAttribute("key","request");
    session.setAttribute("key","session");
    application.setAttribute("key","ServletContext");
%>

<%--pageContext,默认情况下，从最小的返回读取--%>
${key}

<%--指定对应域获取--%>
<%--pageContext--%>
${pageScope.key}
<%--request--%>
${requestScope.key}
<%--session--%>
${sessionScope.key}
<%--ServletContext--%>
${applicationScope.key}
</body>
</html>
