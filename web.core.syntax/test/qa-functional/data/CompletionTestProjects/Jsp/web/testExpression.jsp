<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean class="java.util.Date" id="date"/>
<jsp:useBean class="java.util.Integer" id="integer"/>
<jsp:useBean class="java.util.Calendar" id="calendar"/>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

    <h1>JSP Page</h1>

<%--CC
${|
integer Integer
${integer
--%>

<%--CC
${d|
date Date
${date
--%>

<%--CC
${i|
initParam
${initParam[]
--%>

<%--CC
${p|
pageContext
${pageContext
--%>

<%--CC
${pageContext.|
request ServletRequest
${pageContext.request
--%>

<%--CC
#{|
sessionScope
#{sessionScope[]
--%>

<%--CC
#{c
calendar Calendar
#{calendar
--%>

<%--CC
#{calendar.
time Date
#{calendar.time
--%>

<%--CC
#{calendar.
toString 
#{calendar.toString
--%>

</body>
</html>
