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

    <!-- unique key over all groups of apis -->
    <xsl:key match="//api[@type='export']" name="apiGroups" use="@group" />
    
    <xsl:template match="/apis" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>NetBeans API List</title>
            <link rel="StyleSheet" href="http://www.netbeans.org/netbeans.css" type="text/css" title="NetBeans OpenSource Style" />
        <!--    <link rel="StyleSheet" href="docs.css" type="text/css"> -->

          <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
          <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />

        </head>

        <body style="margin-left: 20px; margin-right: 20px; margin-top: 0px;" bgcolor="#FFFFFF" >

        <center><h1>NetBeans API List</h1></center>

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
                <li>
                <xsl:choose>
                    <xsl:when test="api" >
                        <a href="#def-api-{@name}"><xsl:value-of select="@name"/></a> -
                            <xsl:value-of select="substring-before(description, '.')" disable-output-escaping="yes"/>.
                        
                    </xsl:when>
                    <xsl:otherwise>
                            <xsl:variable name="where" select="substring-before(@target, '/')"/>
                            <b><a href="{$where}/index.html"><xsl:value-of select="$where"/></a></b>
                            - no API description provided
                            (see <a href="http://openide.netbeans.org/tutorial/api.html">how to do it</a>)
                    </xsl:otherwise>
                </xsl:choose>
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="module">
            <xsl:variable name="interfaces" select="api[@type='export']" />
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

                <table border="3" cellpadding="6" width="90%">
                    <thead>
                        <th valign="bottom" width="30%"><b>Interface Name</b></th>
                        <th valign="bottom" width="15%"><b>Stability Classification</b></th>
                        <th valign="bottom" width="45%"><b>Specified in What Document?</b></th>
                    </thead>

                    <xsl:for-each select="$interfaces">
                        <tr/>
                        <xsl:if test="@group='java'" >
                            <xsl:call-template name="api" />
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="//api[generate-id() = generate-id(key('apiGroups', @group))]">
                        <xsl:variable name="grp" select="@group" />
                        <xsl:if test="$grp!='java'" >
                            <xsl:variable name="apis" select="/apis" />
                            <xsl:variable name="module" select="$apis/module[@name=$module.name]" />

                            <xsl:variable name="allOfTheGroup" select="$module/api[@group=$grp]" />
                            <xsl:if test="$allOfTheGroup">
                                <tr/>
                                <td>Set of <xsl:value-of select="$grp"/> APIs</td>
                                <td>Individual</td>
                                <td>
                                    <a href="{$arch.target}#group-{$grp}">table with definitions</a>
                                </td>
                            </xsl:if>
                        </xsl:if>
                    </xsl:for-each>

                </table>
            </xsl:if>


            <P/>

    </xsl:template>

    <xsl:template name="api">
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />

        <tbody>
            <td>
                <a name="api-{$name}"><xsl:value-of select="$name"/></a>
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
                <xsl:choose>
                    <xsl:when test="$category='official'">Official</xsl:when>
                    <xsl:when test="$category='stable'">Stable</xsl:when>
                    <xsl:when test="$category='devel'">Under Development</xsl:when>
                    <xsl:when test="$category='third'">Third party</xsl:when>
                    <xsl:when test="$category='standard'">Standard</xsl:when>
                    <xsl:when test="$category='friend'">Friend</xsl:when>
                    <xsl:when test="$category='private'">Private</xsl:when>
                    <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$category" /></xsl:otherwise>
                </xsl:choose>
            </td>

            <td> <!-- url -->
                <a href="{$url}"><xsl:value-of select="$url"/></a>
            </td>
        </tbody>

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

</xsl:stylesheet>


