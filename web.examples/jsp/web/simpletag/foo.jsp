<html>
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
<body>
<%@ taglib uri="http://jakarta.apache.org/tomcat/examples-taglib" prefix="eg"%>

Radio stations that rock:

<ul>
<eg:foo att1="98.5" att2="92.3" att3="107.7">
<li><%= member %></li>
</eg:foo>
</ul>

<eg:log>
Did you see me on the stderr window?
</eg:log>

<eg:log toBrowser="true">
Did you see me on the browser window as well?
</eg:log>

</body>
</html>
