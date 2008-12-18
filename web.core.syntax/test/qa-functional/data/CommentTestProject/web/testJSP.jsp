<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page import="java.util.logging.Logger" %>
<c:set var="path" value='/ Analytics'/>


<div class="f-page-cell bg-sky" >
    <h2>NetBeans Analytics Community</h2>
    <p>
        This is home of the NetBeans Analytics Community that collects UI gestures from
        users when they work within any application based on the <a href="http://platform.netbeans.org">NetBeans
        Platform</a>. UI gestures are stored and uploaded to a netbeans.org server for
        archiving and analysis.
    </p>
    <p>
        To collect information from a NetBeans-based application, you need to install
        a special module that does the collecting and logging. Read more about this from
        the <a href="http://bits.netbeans.org/dev/javadoc/org-netbeans-modules-uihandler/overview-summary.html">infrastructure description</a>
        of the <em>UI Gestures module</em>.
    </p>
</div>
<%--
  Let's get back the URL as a String so we can use it to
  demonstrate "c:import"
--%>
<% pageContext.setAttribute("filepath",
     application.
       getResource(request.getParameter("filename")).toExternalForm()); %>
<% pageContext.setAttribute("filename", request.getParameter("filename")); %>
<div class="b-bottom-dashed b-top f-page-cell">
    <img alt="" src="http://www.netbeans.org/images/v5/wp.png" class="float-left" style="margin-right:10px;"/>
    <h1 class="font-light normal">News</h1>
    <table>
        <tr><td>4&nbsp;Nov&nbsp;2008</td>
            <td>
                Are you interested in statistics of different NetBeans versions?
                Just look at some of them and select your favorit NetBeans version from the list.
            </td>
        </tr>
        <tr><td>08&nbsp;Aug&nbsp;2008</td>
            <td>
                What is the size of projects? How many files are in them? New statistics
                describing this metrics can be found at
                <a href="graph/projectsize.jsp">Project Size</a>.
                <c:import var="cnn" url="http://www.cnn.com/cnn.rss"/>
            </td>
        </tr>
        <tr><td>20&nbsp;Jul&nbsp;2008</td>
            <td>
                What is the screen resolution of NetBeans users and how many monitors are
                they using? See <a href="graph/screensize.jsp">Screen Size</a> statistics
                to get the response.

<h3>Absolute URL</h3>

<h4>CNN's RSS XML feed:</h4>
  <c:import url="http://www.cnn.com/cnn.rss"/>
            </td>
        </tr>
        <tr><td>23&nbsp;Jun&nbsp;2008</td>
            <td>
                NetBeans statistics graphs are migrated to google graph. See
                <a href="graph/memory.jsp">Memory</a> or
                <a href="graph/projectjavaeeservers.jsp">Java EE Servers Usage</a> and
                many others.
            </td>
        </tr>
<%!
    Logger log = Logger.getLogger("JSPWiki");

	String findParam( PageContext ctx, String key )
	{
	    ServletRequest req = ctx.getRequest();

	    String val = req.getParameter( key );

	    if( val == null )
	    {
	        val = (String)ctx.findAttribute( key );
	    }

	    return val;
	}
    boolean b = false;
%>

<%
java.util.Map<String,String> map = new java.util.HashMap<String,String>();
map.put("1", "one");
map.put("2", "two");
request.setAttribute("test", b);
%>

<% for(String cn : map.keySet()) { %>

    <p style="<%= (map.get(cn)!=null && !map.get(cn).equals("two") ? "color: red" : "") %>">

    <%=  map.get(cn) %></p>

<% } %>
 <tr>
  <td>"base", param=ABC</td>
  <td><c:url value="base"><c:param name="param" value="ABC"/></c:url></td>
 </tr>
 <tr>
  <td>"base", param=123</td>
  <td><c:url value="base"><c:param name="param" value="123"/></c:url></td>
 </tr>
 <tr>
  <td>"base", param=&</td>
  <td><c:url value="base"><c:param name="param" value="&"/></c:url></td>
 </tr>

        <tr><td>10&nbsp;Dec&nbsp;2007</td>
            <td>A new statistic that displays exception reports distribution was added. See <a href="graph/exceptionreports.jsp">Exception reports</a>.
            </td>
        </tr>
  <tr>
    <td>${s1}</td>
    <td>23</td>
    <td>999</td>
    <td>${fn:substring(s1, 23, 999)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>-1</td>
    <td>-1</td>
    <td>${fn:substring(s1, -1, -1)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>99</td>
    <td>12</td>
    <td>&nbsp;${fn:substring(s1, 99, 12)}</td>
  </tr>
        <tr><td>9&nbsp;Jun&nbsp;2007</td>
            <td>Guys from the Czech Technical university provided us a new statistic -
                the usage of <a href="graph/codecompletion.jsp">code completion</a>.
                Thank you very much Jakub and Jan!
            </td>
        </tr>
        <tr><td>29&nbsp;Mar&nbsp;2007</td>
            <td>We are back after a week outage and we bring you new statistic.
                Choose the Help graph in the in the right column to see how much you
                use the integrated help.
            </td>
        </tr>
        <tr><td>13&nbsp;Mar&nbsp;2007</td>
            <td>If you perform an upload, we are able to generate a <b>Tip of the Day</b>
                for you based on the projects that you have been working with.
            </td>
        </tr>
        <tr><td>07&nbsp;Feb&nbsp;2007</td>
            <td>We have our second graph! See the <em>Graphs</em> section in the right column
                of this page.
            </td>
        </tr>
        <tr><td>03&nbsp;Jan&nbsp;2007</td>
            <td>This site got a new look and feel - similar to those used on the
                <a href="http://www.netbeans.org">netbeans.org</a>
                <a href="http://www.netbeans.org/community/index.html">Community Portal</a>.
            </td>
        </tr>

    </table>
    <%--
    <br/>
    <div class="align-right">
        <img src="http://www.netbeans.org/images/v4/redcross.gif" alt="" border="0" height="7" width="10"> <a href="http://www.netbeans.org/servlets/ProjectNewsAdd"><b>add news</b></a>&nbsp;&nbsp;&nbsp;<a href="http://www.netbeans.org/rss-091.xml" title="RSS Newsfeed"><img src="http://www.netbeans.org/images/v4/btn_xml.gif" alt="RSS Newsfeed" align="bottom" border="0" height="10" width="27"></a>
        &nbsp;&nbsp;&nbsp;<img src="http://www.netbeans.org/images/v4/star_r.gif" alt="" border="0" height="7" width="10"> <a href="news/index.html"><b>archive</b></a>

    </div>
    --%>
</div>
<font size=4>
<ul>
<li>	Day of month: is  <jsp:getProperty name="clock" property="dayOfMonth"/>
<li>	Year: is  <jsp:getProperty name="clock" property="year"/>
<li>	Month: is  <jsp:getProperty name="clock" property="month"/>
<li>	Time: is  <jsp:getProperty name="clock" property="time"/>
<li>	Date: is  <jsp:getProperty name="clock" property="date"/>
<li>	Day: is  <jsp:getProperty name="clock" property="day"/>
<li>	Day Of Year: is  <jsp:getProperty name="clock" property="dayOfYear"/>
<li>	Week Of Year: is  <jsp:getProperty name="clock" property="weekOfYear"/>
<li>	era: is  <jsp:getProperty name="clock" property="era"/>
<li>	DST Offset: is  <jsp:getProperty name="clock" property="DSTOffset"/>
<li>	Zone Offset: is  <jsp:getProperty name="clock" property="zoneOffset"/>
</ul>
</font>
<%!
    class Table{
        public java.util.Map getEntries(){
                return java.util.Collections.emptyMap();
            }
        }
%>
<%! Table table;%>
<%
	for(int i=0; i<table.getEntries().entrySet().size(); i++) {
	   Object entr = table.getEntries().get(i);
%>
	<TR>
	<TD>
	</TD>
	</TR>
<%
	}
%>
<font color="red">

<%= System.currentTimeMillis() %>

</font>
<%--

<div class="b-bottom-dashed f-page-cell">
    <img alt="" src="http://www.netbeans.org/images/v5/flame.png" class="float-left" style="margin-right:10px;"/>
    <h1 class="font-light normal">Hot Threads</h1>

  
</div>
--%>

<!-- mailing lilsts start -->

<%--
<div class="b-bottom-dashed f-page-cell">
<img alt="" src="http://www.netbeans.org/images/v5/kmail.png" class="float-left" style="margin-right:10px;"/>
<h1 class="font-light normal">Mailing Lists</h1>
<div style="margin-left:60px;">
<table>
      <tr>


--%>
<!-- mailing lists ens -->
<div class="f-page-cell b-bottom">
    <img alt="" src="http://www.netbeans.org/images/v5/kpackage.png" class="float-left" style="margin-right:10px;"/>
    <h1 class="font-light normal">Useful Links</h1>
    <div style="margin-left:35px;">
        <table>
            <tr>
                <td>
                    <ul>
                        <li><a href="http://wiki.netbeans.org">NetBeans Wiki</a> to share ideas</li>
                        <li><a href="http://www.planetnetbeans.org/">Planet NetBeans -- Blogs by community members</a></li>
                    </ul>
                </td>
            </tr>
        </table>
    </div>
</div>


<!-- End Content Area -->
