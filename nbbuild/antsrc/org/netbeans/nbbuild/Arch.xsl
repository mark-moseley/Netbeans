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
    <xsl:key match="//api" name="apiGroups" use="@group" />
    
    <xsl:param name="arch.stylesheet"/>
    <xsl:param name="arch.overviewlink"/>
    <xsl:param name="arch.footer"/>
    <xsl:param name="arch.answers.date"/>
    <xsl:param name="arch.when"/>

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="api-answers/@module" /> - NetBeans Architecture Questions</title>
                <xsl:if test="$arch.stylesheet">
                    <link rel="stylesheet" type="text/css" href="{$arch.stylesheet}"/>
                </xsl:if>
            </head>
            <body>
            
                <xsl:if test="$arch.overviewlink">
                    <p class="overviewlink"><a href="{$arch.overviewlink}">Overview</a></p>
                </xsl:if>
            
                <h1>NetBeans Architecture Answers for <xsl:value-of select="api-answers/@module" /><xsl:text> module</xsl:text></h1>
                
                <xsl:variable name="qver" select="api-answers/api-questions/@version"/>
                <xsl:variable name="afor" select="api-answers/@question-version" />
                
                <ul>
                <li><b>Author:</b><xsl:text> </xsl:text><xsl:value-of select="api-answers/@author" /></li>
                <li><b>Answers as of:</b><xsl:text> </xsl:text><xsl:value-of select="$arch.answers.date"/></li>
                <li><b>Answers for questions version:</b><xsl:text> </xsl:text><xsl:value-of select="$afor" /></li>
                <li><b>Latest available version of questions:</b><xsl:text> </xsl:text><xsl:value-of select="$qver" /></li>
                </ul>
                
                <xsl:if test="not($qver=$afor)">
                    <strong>
                        WARNING: answering questions version <xsl:value-of select="$afor"/>
                        rather than the current <xsl:value-of select="$qver"/>.
                    </strong>
                </xsl:if>
            
                <xsl:apply-templates />    
                
                <hr/>

                <h2>Interfaces table</h2>

                <xsl:call-template name="for-each-group">
                    <xsl:with-param name="target" >api-group</xsl:with-param>
                </xsl:call-template>
                
                
                <xsl:variable name="all_interfaces" select="//api" />
                <xsl:if test="not($all_interfaces)" >
                    <b> WARNING: No imported or exported interfaces! </b>
                </xsl:if>

                <hr/>
                                
                <xsl:if test="$arch.footer">
                    <p><xsl:value-of select="$arch.footer"/></p>
                </xsl:if>
                
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="category">
        <hr/>
        <h2>
            <xsl:value-of select="@name" />
        </h2>
        <ul>
            <xsl:for-each select="question">
                <xsl:call-template name="answer" />
            </xsl:for-each>
        </ul>
    </xsl:template>
    

    <xsl:template name="answer">
        <xsl:variable name="value" select="@id" />
    
        <p/>
        <font color="gray" >
        <b><a name="answer-{@id}">Question (<xsl:value-of select="@id"/>)</a>:</b> <em><xsl:apply-templates select="./node()" /></em>
        </font>
        <p/>
        
        <xsl:choose>
            <xsl:when test="count(//answer[@id=$value])" >
                <b>Answer:</b> <!-- <xsl:value-of select="//answer[@id=$value]" /> -->
                <xsl:apply-templates select="//answer[@id=$value]/node()" />
            </xsl:when>
            <xsl:when test="string-length($arch.when)=0 or contains($arch.when,@when)" >
                <b>WARNING:</b>
                <xsl:text> Question with id="</xsl:text>
                <i> 
                <xsl:value-of select="@id" />
                </i>
                <xsl:text>" has not been answered!</xsl:text>
             </xsl:when>
             <xsl:otherwise>
                 <i>Needs to be yet answered in <xsl:value-of select="@when" /> phase.</i>
              </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="api">
        <!-- generates link to given API -->
        <xsl:variable name="name" select="@name" />
        
        <a>
            <xsl:attribute name="href" >
                <xsl:text>#api-</xsl:text><xsl:value-of select="$name" />
            </xsl:attribute>
            <xsl:value-of select="$name" />
        </a>
        <!-- put "- and description" there only if there are some child nodes -->
        <xsl:if test="child::node()" >
            - <xsl:apply-templates />
        </xsl:if>
    </xsl:template>

    <xsl:template name="api">
        <xsl:call-template name="api-line" >
            <xsl:with-param name="name" select="@name" />
            <xsl:with-param name="type" select="@type" />
            <xsl:with-param name="group">api</xsl:with-param>
            <xsl:with-param name="category" select="@category" />
            <xsl:with-param name="describe.url" select="@url" />
            <xsl:with-param name="describe.node" select="./node()" />
        </xsl:call-template>
    </xsl:template>
    
    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
  
  
    <xsl:template match="answer">
        <!-- ignore direct answers -->
    </xsl:template>
    <xsl:template match="hint">
        <!-- ignore direct answers -->
    </xsl:template>
    
    <!-- enumerates all groups of APIs and calls given template 
      on each of them
    -->
    <xsl:template name="for-each-group" >
        <xsl:param name="target" />
    
        <xsl:for-each select="//api[generate-id() = generate-id(key('apiGroups', @group))]">
            <xsl:call-template name="jump-to-target">
                <xsl:with-param name="group" select="@group" />
                <xsl:with-param name="target" select="$target" />
            </xsl:call-template>
        </xsl:for-each>

    </xsl:template>    
    <xsl:template name="jump-to-target" >
        <xsl:param name="target" />
        <xsl:param name="group" />
        
        <xsl:choose>
            <xsl:when test="$target='api-group'" >
                <xsl:call-template name="api-group">
                    <xsl:with-param name="group" select="$group" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>
                    WRONG TARGET: <xsl:value-of select="$target"/>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    

    <!-- displays group of APIs -->
    
    <xsl:template name="api-group" >
        <xsl:param name="group" />
        
    
        <a>
            <xsl:attribute name="name" >
                <xsl:text>group-</xsl:text><xsl:value-of select="$group" />
            </xsl:attribute>
            <h5>Group of <xsl:value-of select="$group"/> interfaces</h5>
        </a>
        
        <xsl:variable name="all_interfaces" select="//api[@group=$group]" />
        <table cellpadding="1" cellspacing="0" border="0" class="tablebg" width="100%"><tr><td>
          <table border="0" cellpadding="3" cellspacing="1" width="100%">   
            <tr class="tablersh">
                <td align="CENTER" width="25%"><span class="titlectable">Interface Name</span></td>
                <td align="CENTER" width="10%"><span class="titlectable">In/Out</span></td>
                <td align="CENTER" width="10%"><span class="titlectable">Stability</span></td>
                <td align="CENTER" ><span class="titlectable">Specified in What Document?</span></td>
            </tr>

            <xsl:for-each select="$all_interfaces">
                <xsl:call-template name="api" />
            </xsl:for-each>
          </table>
        </td></tr></table>
        <p/>
    </xsl:template>    
    
    <!-- the template to convert an instances of API into an HTML line in a table 
      describing the API -->

    <xsl:template name="api-line" >
       <xsl:param name="name" />
       <xsl:param name="group" />
       <xsl:param name="category" />
       <xsl:param name="type" /> <!-- can be left empty -->
       
       <xsl:param name="describe.url" />
       <xsl:param name="describe.node" />

        <tr class="tabler">
            <td>
                <a>
                    <xsl:attribute name="name" >
                        <xsl:value-of select="$group" /><xsl:text>-</xsl:text><xsl:value-of select="$name" />
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <xsl:if test="$type" > 
                <td> <!-- imported/exported -->
                    <xsl:choose>
                        <xsl:when test="$type='import'">Imported</xsl:when>
                        <xsl:when test="$type='export'">Exported</xsl:when>
                        <xsl:otherwise>
                            <xsl:message>
                                WARNING: <xsl:value-of select="$type"/>
                            </xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:if>
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
            
            <td> <!-- description -->
                <xsl:call-template name="describe">
                  <xsl:with-param name="describe.url" select="$describe.url" />
                  <xsl:with-param name="describe.node" select="$describe.node" />
                </xsl:call-template>
            </td>
        </tr>
    </xsl:template>  
    <xsl:template name="describe">
       <xsl:param name="describe.url" />
       <xsl:param name="describe.node" />
       
       
       <xsl:if test="$describe.url" >
            <a>
                <xsl:attribute name="href" >
                    <xsl:value-of select="$describe.url" />
                </xsl:attribute>
                <xsl:value-of select="$describe.url" />
            </a>
           
           <xsl:if test="$describe.node" >
               <p/>
           </xsl:if>
       </xsl:if>
       
       <xsl:if test="$describe.node" >
           <xsl:apply-templates select="$describe.node" />
       </xsl:if>
    </xsl:template>
        
</xsl:stylesheet> 
