<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
String c =
""+System.currentTimeMillis();
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<%--
  This file is an entry point:
     * for JavaServer
     * Faces application
--%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>JSP Page</title>
</head>
<body>
<body>
<!-- test
     comment -->
<%! int count = 1;%>
<%! int doubleCount() {
count = count * 2;
return count;
}%>
<h1><h:outputText value="JavaServer Faces"/></h1>

<jsp:useBean id="foo" class="javax.swing.JLabel">
Bean created!  Setting foo.bar...<br>
<jsp:setProperty name="foo" property="bar">
<jsp:attribute name="value">
<my:helloWorld/>
<%
String x =
""+System.currentTimeMillis();
%>
</jsp:attribute>
</jsp:setProperty>
</jsp:useBean>
Result: ${foo.bar}

</body>
</html>
