<?xml version="1.0" encoding="UTF-8" ?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:param name="date" />

    <!-- unique key over all groups of apis -->
    <xsl:key match="//api[@type='export']" name="apiGroups" use="@group" />
    <!-- unique key over all names of apis -->
    <xsl:key match="//api" name="apiNames" use="@name" />
    
    <xsl:template match="/apis" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>NetBeans API List</title>
            <link rel="stylesheet" href="OpenAPIs/netbeans.css" type="text/css"/>

          <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
          <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />

        </head>

        <body>

        <center>
            <h1>NetBeans API List</h1>
            <h3>Version 4.0</h3>
            <xsl:if test="$date" >
                <xsl:value-of select="$date" />
                <p/>
            </xsl:if>
        </center>

        This document provides a list of <em>NetBeans APIs</em> with a short description
        of what they are used for, and a table describing different types of interfaces
        (see <a href="http://openide.netbeans.org/tutorial/api-design.html#api">What is
        an API?</a> to understand why we list DTDs, file formats, etc.) and with
        a stability category (see <a
        href="http://openide.netbeans.org/tutorial/api-design.html#life">API
        life-cycle</a> for a list of possible categories and their descriptions).
        The aim is to provide as detailed a definition of NetBeans module 
        external interfaces as possible and give other developers a chance to decide
        whether they want to depend on a particular API or not.
        <p/>
        To get the API of your module listed here, see the documentation for the 
        Javadoc building
        <a href="http://openide.netbeans.org/tutorial/api.html">infrastructure</a>.
        
        <h4>Additional Sources of Information</h4>
        
        <ul>
            <li><a href="allclasses.html">Index of all NetBeans API classes</a></li>
        </ul>

        <hr/>
        <xsl:call-template name="list-modules" />
        <hr/>
        <xsl:apply-templates />
        
        </body>
        </html>
       
    </xsl:template>
    
    <xsl:template name="list-modules">
        <h2>Content</h2>
        <ul>
            <xsl:for-each select="/apis/module" >
            	<xsl:sort select="@name" />
                <xsl:choose>
                    <xsl:when test="api" >
                       <li>
                           <a href="#def-api-{@name}"><xsl:value-of select="@name"/></a> -
                            <!-- XXX the following is crap; e.g. messes up descs of Dialogs API, I/O API, ... -->
                            <!-- Should use e.g.:
                            <answer id="arch-what">
                                <span class="summary">This API does such-and-such.</span>
                                It also does some other less important stuff.
                            </answer>
                            -->
                          <xsl:comment>Begin of first sentenece</xsl:comment>
                          <xsl:apply-templates mode="first-sentence" select="description" />
                          <xsl:comment>End of first sentenece</xsl:comment>.
                        </li>
                    </xsl:when>
                    <xsl:otherwise>
                            <!-- will be covered later -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="/apis/module" >
                <xsl:sort select="api" order="descending" />
            	<xsl:sort select="@name" />
                <xsl:choose>
                    <xsl:when test="api" >
                            <!-- covered before -->
                    </xsl:when>
                    <xsl:otherwise>
                        <li>
                            <xsl:variable name="where" select="substring-before(@target, '/')"/>
                            <b><a href="{$where}/index.html"><xsl:value-of select="$where"/></a></b>
                            - no API description provided
                            (see <a href="http://openide.netbeans.org/tutorial/api.html">how to do it</a>)
                        </li>
                    </xsl:otherwise>
                </xsl:choose>
             </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="module">
            <xsl:variable name="interfaces" select="api[@type='export' and generate-id() = generate-id(key('apiNames', @name))]" />
            <xsl:variable name="module.name" select="@name" />
            <xsl:variable name="arch.stylesheet" select="@stylesheet" />
            <xsl:variable name="arch.overviewlink" select="@overviewlink" />
            <xsl:variable name="arch.footer" select="@footer" />
            <xsl:variable name="arch.target" select="@target" />

            <xsl:if test="$interfaces">
                <h3>

                    <a name="def-api-{$module.name}"><xsl:value-of select="$module.name"/></a>
                    
                    (<a>
                        <xsl:attribute name="href">
                            <xsl:call-template name="filedirapi" >
                                <xsl:with-param name="arch.target" select="$arch.target" />
                            </xsl:call-template>
                            <xsl:text>/index.html</xsl:text>
                        </xsl:attribute>
                        <xsl:text>javadoc</xsl:text>
                    </a>,
                    <a>
                        <xsl:attribute name="href">
                            <xsl:call-template name="filedirapi" >
                                <xsl:with-param name="arch.target" select="$arch.target" />
                            </xsl:call-template>
                            <xsl:text>.zip</xsl:text>
                        </xsl:attribute>
                        <xsl:text>download</xsl:text>
                    </a>)
                </h3>

                <div><xsl:apply-templates select="description"/></div>

                <xsl:if test="deploy-dependencies">
                    <div>
                       <p><b>Usage:</b></p>
                       <xsl:apply-templates select="deploy-dependencies"/>
                    </div>
                </xsl:if>

                <p/><table cellpadding="1" cellspacing="0" border="0" class="tablebg" width="100%"><tr><td>
                  <table border="0" cellpadding="3" cellspacing="1" width="100%">
<!--                    <tr><td COLSPAN="5" class="tablecbg" ALIGN="CENTER"><font CLASS="titlectable">Do not duplicate any files</font></td></tr> -->
                    <tr class="tablersh">
                      <td align="CENTER" width="30%"><span class="titlectable">Interface Name</span></td>
                      <td align="CENTER" width="15%"><span class="titlectable">Stability Classification</span></td>
                      <td align="CENTER" ><span class="titlectable">Specified in What Document?</span></td>
                    </tr>

                    <xsl:for-each select="$interfaces">
                        <xsl:if test="@group='java'" >
                            <xsl:call-template name="api" >
                                <xsl:with-param name="arch.target" select="$arch.target" />
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="//api[generate-id() = generate-id(key('apiGroups', @group))]">
                        <xsl:variable name="grp" select="@group" />
                        <xsl:if test="$grp!='java'" >
                            <xsl:variable name="apis" select="/apis" />
                            <xsl:variable name="module" select="$apis/module[@name=$module.name]" />

                            <xsl:variable name="allOfTheGroup" select="$module/api[@group=$grp]" />
                            <xsl:if test="$allOfTheGroup">
                              <tr class="tabler">
                                <td>Set of <xsl:value-of select="$grp"/> APIs</td>
                                <td>Individual</td>
                                <td>
                                    <a href="{$arch.target}#group-{$grp}">table with definitions</a>
                                </td>
                              </tr>
                            </xsl:if>
                        </xsl:if>
                    </xsl:for-each>

                  </table>
                </td></tr></table>
            </xsl:if>


            <P/>

    </xsl:template>

    <xsl:template name="api">
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />
        <xsl:param name="arch.target" />

        <tr class="tabler">
            <td>
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="$arch.target" />
                        <xsl:text>#java-</xsl:text>
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <!--
            <td>
                <xsl:choose>
                    <xsl:when test="$type='import'">Imported</xsl:when>
                    <xsl:when test="$type='export'">Exported</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$type" /></xsl:otherwise>
                </xsl:choose>
            </td> -->
            <td> <!-- stability category -->
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>http://openide.netbeans.org/tutorial/api-design.html#category-</xsl:text>
                        <xsl:value-of select="$category" />
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$category='official'">Official</xsl:when>
                        <xsl:when test="$category='stable'">Stable</xsl:when>
                        <xsl:when test="$category='devel'">Under Development</xsl:when>
                        <xsl:when test="$category='third'">Third party</xsl:when>
                        <xsl:when test="$category='standard'">Standard</xsl:when>
                        <xsl:when test="$category='friend'">Friend</xsl:when>
                        <xsl:when test="$category='private'">Private</xsl:when>
                        <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                        <xsl:otherwise>
                            <xsl:message>
                                WARNING: <xsl:value-of select="$category"/>
                            </xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>  
            </td>

            <td> <!-- url -->
                <a href="{$url}"><xsl:value-of select="$url"/></a>
            </td>
        </tr>

    </xsl:template>

    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>

    <!-- extracts first part before slash from LoadersAPI/bleble.html or
     and prints it or prints OpenAPIs as a fallback -->

    <xsl:template name="filedirapi" >
        <xsl:param name="arch.target" />
    
        <xsl:if test="substring-before($arch.target,'/')">
            <xsl:value-of select="substring-before($arch.target,'/')" />
        </xsl:if>
        <xsl:if test="not (substring-before($arch.target,'/'))">
            <xsl:text>OpenAPIs</xsl:text>
        </xsl:if>
    </xsl:template>


    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Gets the first sentence with HTML tags -->
    
    <xsl:template mode="first-sentence" match="api-ref">
        <b><xsl:value-of select="@name" /></b><xsl:text> </xsl:text>
    </xsl:template>

    <xsl:template mode="first-sentence" match="node()">
        <xsl:choose>
            <xsl:when test="count(child::*) = 0" >
                <xsl:variable name="first-sentence" select="substring-before(normalize-space(), '. ')" />
                <xsl:variable name="first-dot" select="substring-before(normalize-space(), '.')" />
                <xsl:choose>
                    <xsl:when test="$first-sentence" >
                        <xsl:value-of select="$first-sentence" />
                        <!-- this trick starts comment which disables output produces after 
                           Which means comments out everything after the .
                           -->
                        <xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
                    </xsl:when>
                    <xsl:when test="$first-dot" >
                        <xsl:value-of select="$first-dot" />
                        <!-- this trick starts comment which disables output produces after 
                           Which means comments out everything after the .
                           -->
                        <xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates mode="first-sentence" select="child::*"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="first-sentence" select="node()"/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
    
</xsl:stylesheet>


