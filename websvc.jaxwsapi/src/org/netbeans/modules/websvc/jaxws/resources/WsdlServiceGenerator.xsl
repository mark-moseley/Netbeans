<?xml version="1.0" encoding="UTF-8" ?>
<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.

You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.

When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.
-->

<!--
    Document   : WsdlServiceGenerator.xsl
    Created on : June 15, 2006, 6:29 PM
    Author     : mkuchtiak
    Description: stylesheet used to generate missing <wsdl:service> element
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
     xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" 
     xmlns:mime="http://schemas.xmlsoap.org/wsdl/mime/"
     xmlns:http="http://schemas.xmlsoap.org/wsdl/http/" 
     version="1.0">
    
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="tns_prefix">tns</xsl:param>
    <xsl:param name="wsdl_location">WSDL LOCATION</xsl:param>

    <xsl:template match="wsdl:definitions">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            
            <!-- generate wsdl import -->
            <xsl:element name="import" namespace="http://schemas.xmlsoap.org/wsdl/">
                <xsl:attribute name="namespace">
                    <xsl:value-of select="@targetNamespace"/>
                </xsl:attribute>
                <xsl:attribute name="location">
                    <xsl:value-of select="$wsdl_location"/>
                </xsl:attribute>
            </xsl:element>
            
            <!-- select the first binding -->
            <xsl:variable name = "bindingNode" select="wsdl:binding[1]"/>
            
            <!-- if binding exists - generate service element only -->
            <xsl:if test="boolean($bindingNode)">
                <xsl:variable name = "bindingName" select="$bindingNode/@name"/>
                <xsl:variable name = "bindingNameShort" select="substring-before($bindingName,'Binding')"/>
                <xsl:variable name = "fullBindingName" select = "concat($tns_prefix,':',$bindingName)"/>
                <xsl:element name="service" namespace="http://schemas.xmlsoap.org/wsdl/">
                    <xsl:attribute name="name">
                        <xsl:if test="boolean($bindingNameShort)">
                            <xsl:value-of select="$bindingNameShort"/><xsl:text>Service</xsl:text>
                        </xsl:if>
                        <xsl:if test="not(boolean($bindingNameShort))">
                            <xsl:value-of select="$bindingName"/><xsl:text>Service</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:element name="port" namespace="http://schemas.xmlsoap.org/wsdl/">
                        <xsl:attribute name="name">
                            <xsl:if test="boolean($bindingNameShort)">
                                <xsl:value-of select="$bindingNameShort"/><xsl:text>Port</xsl:text>
                            </xsl:if>
                            <xsl:if test="not(boolean($bindingNameShort))">
                                <xsl:value-of select="$bindingName"/><xsl:text>Port</xsl:text>
                            </xsl:if>
                        </xsl:attribute>
                        <xsl:attribute name="binding">
                            <xsl:value-of select="concat($tns_prefix,':',$bindingName)"/>
                        </xsl:attribute>
                        <xsl:element name="address" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                            <xsl:attribute name="location">
                                <xsl:text>REPLACE_WITH_ACTUAL_WEB_SERVICE_URL</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:if> <!-- binding exists -->
            
            <!-- if binding doesn't exist - generate binding element first, then service element -->
            <xsl:if test="not(boolean($bindingNode))">
                
                <xsl:variable name = "portTypeName1" select="wsdl:portType[1]/@name"/>
                <xsl:variable name = "portTypeName1Short" select="substring-before($portTypeName1,'Port')"/>
                
                <xsl:for-each select="wsdl:portType">

                    <xsl:variable name = "portTypeName" select="@name"/>
                    <xsl:variable name = "portTypeNameShort" select="substring-before($portTypeName,'Port')"/>
                    <xsl:variable name = "fullPortTypeName" select = "concat($tns_prefix,':',$portTypeName)"/>

                    <xsl:variable name = "bindingName">
                        <xsl:if test="boolean($portTypeNameShort)">
                            <xsl:value-of select="$portTypeNameShort"/><xsl:text>Binding</xsl:text>
                        </xsl:if>
                        <xsl:if test="not(boolean($portTypeNameShort))">
                            <xsl:value-of select="$portTypeName"/><xsl:text>Binding</xsl:text>
                        </xsl:if>
                    </xsl:variable>

                    <!-- generate document/literal binding -->
                    <xsl:element name="binding" namespace="http://schemas.xmlsoap.org/wsdl/">
                        <xsl:attribute name="name">
                            <xsl:value-of select="$bindingName"/>
                        </xsl:attribute>
                        <xsl:attribute name="type">
                            <xsl:value-of select="$fullPortTypeName"/>
                        </xsl:attribute>
                        <xsl:element name="binding" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                            <xsl:attribute name="transport">http://schemas.xmlsoap.org/soap/http</xsl:attribute>
                            <xsl:attribute name="style">document</xsl:attribute>
                        </xsl:element>
                        <xsl:for-each select="wsdl:operation">
                            <xsl:element name="operation" namespace="http://schemas.xmlsoap.org/wsdl/">
                                <xsl:attribute name="name">
                                    <xsl:value-of select="@name"/>
                                </xsl:attribute>
                                <xsl:element name="operation" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                                    <xsl:attribute name="soapAction">
                                        <xsl:value-of select="@name"/><xsl:text>_action</xsl:text>
                                    </xsl:attribute>
                                </xsl:element>
                                <xsl:if test="wsdl:input">
                                    <xsl:element name="input" namespace="http://schemas.xmlsoap.org/wsdl/">
                                        <xsl:if test="wsdl:input/@name">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="wsdl:input/@name"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:element name="body" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                                            <xsl:attribute name="use">literal</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </xsl:if>
                                <xsl:if test="wsdl:output">
                                    <xsl:element name="output" namespace="http://schemas.xmlsoap.org/wsdl/">
                                        <xsl:if test="wsdl:output/@name">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="wsdl:output/@name"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:element name="body" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                                            <xsl:attribute name="use">literal</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </xsl:if>
                                <xsl:if test="wsdl:fault">
                                    <xsl:element name="fault" namespace="http://schemas.xmlsoap.org/wsdl/">
                                        <xsl:if test="wsdl:fault/@name">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="wsdl:fault/@name"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:element name="fault" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="wsdl:fault/@name"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="use">literal</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </xsl:if>
                            </xsl:element>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:for-each>
                    
                <!-- generate service element --> 
                <xsl:element name="service" namespace="http://schemas.xmlsoap.org/wsdl/">
                    <xsl:attribute name="name">
                        <xsl:if test="boolean($portTypeName1Short)">
                            <xsl:value-of select="$portTypeName1Short"/><xsl:text>Service</xsl:text>
                        </xsl:if>
                        <xsl:if test="not(boolean($portTypeName1Short))">
                            <xsl:value-of select="$portTypeName1"/><xsl:text>Service</xsl:text>
                        </xsl:if>
                    </xsl:attribute>
                    <xsl:for-each select="wsdl:portType">
                        
                        <xsl:variable name = "portTypeName" select="@name"/>
                        <xsl:variable name = "portTypeNameShort" select="substring-before($portTypeName,'Port')"/>
                        <xsl:variable name = "fullPortTypeName" select = "concat($tns_prefix,':',$portTypeName)"/>

                        <xsl:variable name = "bindingName">
                            <xsl:if test="boolean($portTypeNameShort)">
                                <xsl:value-of select="$portTypeNameShort"/><xsl:text>Binding</xsl:text>
                            </xsl:if>
                            <xsl:if test="not(boolean($portTypeNameShort))">
                                <xsl:value-of select="$portTypeName"/><xsl:text>Binding</xsl:text>
                            </xsl:if>
                        </xsl:variable>
                    
                        <xsl:element name="port" namespace="http://schemas.xmlsoap.org/wsdl/">
                            <xsl:attribute name="name">
                                <xsl:if test="boolean($portTypeNameShort)">
                                    <xsl:value-of select="$portTypeNameShort"/><xsl:text>Port</xsl:text>
                                </xsl:if>
                                <xsl:if test="not(boolean($portTypeNameShort))">
                                    <xsl:value-of select="$portTypeName"/><xsl:text>Port</xsl:text>
                                </xsl:if>
                            </xsl:attribute>
                            <xsl:attribute name="binding">
                                <xsl:value-of select="concat($tns_prefix,':',$bindingName)"/>
                            </xsl:attribute>
                            <xsl:element name="address" namespace="http://schemas.xmlsoap.org/wsdl/soap/">
                                <xsl:attribute name="location">
                                    <xsl:text>REPLACE_WITH_ACTUAL_WEB_SERVICE_URL</xsl:text>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:element>
                    </xsl:for-each>
                </xsl:element>            
            </xsl:if> <!-- binding doesn't exist -->
        </xsl:copy>
   
    </xsl:template>
 
</xsl:stylesheet> 
