<!--
  Copyright 2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<html>
  <head>
    <title>Tag Plugin Examples: if</title>
  </head>
  <body>
    <h1>Tag Plugin Examples - &lt;c:if></h1>

    <hr>
    <br>
    <a href="notes.html">Plugin Introductory Notes</a>
    <br/>
    <a href="howto.html">Brief Instructions for Writing Plugins</a>
    <br/> <br/>
    <hr>

    <font color="#000000"/>
    <br>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <h3>Set the test result to a variable</h3>
    <c:if test="${1==1}" var="theTruth" scope="session"/>
    The result of testing for (1==1) is: ${theTruth}

    <h3>Conditionally execute the body</h3>
    <c:if test="${2>0}">
	It's true that (2>0)!
    </c:if>
  </body>
</html> 
