<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Scriptlets Code Completion Page</title>
    </head>
    <body>

    <h1>JSP Scriptlets Code Completion Page</h1>
<%
    application.getClass();
%>
<%-- Java completion for HttpSession object methods --%>
<%--CC
<% request.getSession().|
void setAttribute (String arg0 , Object arg1 )
<% request.getSession().setAttribute(arg0, arg1);
--%>

<%-- Java completion for session object --%>
<%--CC
<% session.|
Object getAttribute (String arg0 )
<% session.getAttribute(arg0)
--%>

<%-- completion for methods and fields in scriptlet declaration --%>
<%--CC
<%! void f() { this.|
ServletConfig getServletConfig ()
<%! void f() { this.getServletConfig()
--%>

<%-- completion for methods and fields in scriptlet expression --%>
<%--CC
<%= request.|
String getContextPath ()
<%= request.getContextPath()
--%>

    </body>
</html>
