<!--
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the License).  You may not use this file except in
 compliance with the License.

 You can obtain a copy of the license at
 https://glassfish.dev.java.net/public/CDDLv1.0.html or
 glassfish/bootstrap/legal/CDDLv1.0.txt.
 See the License for the specific language governing
 permissions and limitations under the License.

 When distributing Covered Code, include this CDDL
 Header Notice in each file and include the License file
 at glassfish/bootstrap/legal/CDDLv1.0.txt.
 If applicable, add the following below the CDDL Header,
 with the fields enclosed by brackets [] replaced by
 you own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 Copyright 2006 Sun Microsystems, Inc. All rights reserved.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:setBundle basename="LocalStrings"/>

<%
  String lotteryName = (String) request.getAttribute("lottery_name"); 
  String lotteryNumber = (String) request.getAttribute("lottery_number"); 
  String lotteryDate = (String) request.getAttribute("lottery_date"); 
%>

<html> 
  <body> 
    <center>

      <h2><fmt:message key="california_lottery"/></h2> 

      <fmt:message key="quick_pick_results">
        <fmt:param>
          <%= lotteryName%>
        </fmt:param> 
        <fmt:param>
          <%= lotteryDate%>
        </fmt:param> 
      </fmt:message>
      <br><br>
      <b><%= lotteryNumber%></b><br><br>

      <fmt:message key="play_again"/> &nbsp<a href="index.html"><fmt:message key="here"/></a>.

    </center>
  </body>
</html>
