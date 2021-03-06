<!--
  Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
    may be used to endorse or promote products derived from this software without
    specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
  THE POSSIBILITY OF SUCH DAMAGE.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:setBundle basename="LocalStrings"/>

<%@ page language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="javax.ejb.ObjectNotFoundException" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="enterprise.customer_cmp_ejb.persistence.Customer" %>
<%@ page import="enterprise.customer_cmp_ejb.ejb.session.*" %>
<%@ page import='java.util.*' %>
 
<html>

<head><title><fmt:message key="cmp_demo_title"/></title></head>
<body bgcolor="white">
<center>
<h2><fmt:message key="cmp_demo_title"/> </h2>

<fmt:message key="search_for_customer"/> :
<p>
    <form method="get" action="/customer/searchCustomer.jsp">
    Search by 
    <select name="searchCriteria">
      <option value="customerID" selected><fmt:message key="customer_id"/> 
      <option value="lastName"><fmt:message key="last_name"/> 
      <option value="firstName"><fmt:message key="first_name"/> 
    </select>
    <input type="text" name="searchText" size="25">
    <p>
    <input type="submit" value=<fmt:message key="search"/> >
    </form>

<%
String text = request.getParameter("searchText");
String criteria = request.getParameter("searchCriteria");

if (text != null && !"".equals(text)) {
    try {
        InitialContext ic = new InitialContext();
        Object o = ic.lookup("java:comp/env/CustomerSessionLocal");
        CustomerSessionLocal custSession = (CustomerSessionLocal) o;

        List customers=new ArrayList(); //creating empty list handles any errors gracefully.
        if ("customerID".equals(criteria)) {
            Customer customer = custSession.searchForCustomer(text);
            if(customer!=null){
                customers.add(customer);
            }
        }	  
        else if ("lastName".equals(criteria)) {
          customers = custSession.findCustomerByLastName(text);
        } else if ("firstName".equals(criteria)) {
          customers = custSession.findCustomerByFirstName(text);
        } else {
        }
  

%>
<fmt:message key="results"/> : <p>
<%
for (int i = 0; i < customers.size(); i++) {
  Customer c = (Customer)(customers).get(i);
  String cid = (String)c.getCustomerID();
%>
<a href="/customer/editCustomer.jsp?cid=<%=cid%>"> 
<%=c.getLastName()%>, <%=c.getFirstName()%></a> has
<%=c.getAddresses().size()%> addresses,
<%=c.getSubscriptions().size()%> subscriptions.
<p>
<%
}
%>
<%
    } catch(Exception e) {
        e.printStackTrace();
        out.println(e.toString());
    }
}
%>

<hr>
[<a href="/customer/index.html"><fmt:message key="home"/> </a>]
</center>
</body>
</html>
