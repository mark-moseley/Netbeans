<?xml version='1.0' encoding='ISO-8859-1' ?>
<!--
*     Copyright � 2003 Sun Microsystems, Inc. All rights reserved.
*     Use is subject to license terms.
-->
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<helpset version="1.0">

 <title>Core IDE Help</title>

  <maps>
     <homeID>ide.welcome</homeID>
     <mapref location="Map.jhm" />
  </maps>
  <view>
    <name>TOC</name>
    <label>Table Of Contents</label>
    <type>javax.help.TOCView</type>
    <data>ide-toc.xml</data>
  </view>
  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>ide-idx.xml</data>
  </view>
  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch2
    </data>
  </view>

</helpset>
