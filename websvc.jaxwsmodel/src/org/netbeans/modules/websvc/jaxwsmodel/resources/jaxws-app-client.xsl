<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:carproject="http://www.netbeans.org/ns/car-project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"> 
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:param name="jaxwsversion">jaxws21lib</xsl:param>
    <xsl:param name="xnocompile">true</xsl:param>
    <xsl:template match="/">
        
        <project>        
            
            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>
            
            <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <target name="wsimport-init" depends="init">
                    <mkdir dir="${{build.generated.dir}}/wsimport/client"/>
                    <mkdir dir="${{build.generated.dir}}/wsimport/binaries"/>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{java.home}}/../lib/tools.jar:${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}"/>
                    </taskdef>
                </target>
            </xsl:if>
            <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <xsl:variable name="wsname" select="@name"/>
                <xsl:variable name="package_name" select="jaxws:package-name"/>
                <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                <xsl:variable name="wsdl_url_actual" select="jaxws:wsdl-url"/>
                <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                <target name="wsimport-client-check-{$wsname}" depends="wsimport-init">
                    <condition property="wsimport-client-{$wsname}.notRequired">
                        <xsl:choose>
                            <xsl:when test="jaxws:package-name">
                                <available file="${{build.generated.dir}}/wsimport/client/{$package_path}/{$wsname}.java"/>    
                            </xsl:when>
                            <xsl:otherwise>
                                <available file="${{build.generated.dir}}/wsimport/client/dummy" type="dir"/>
                            </xsl:otherwise>
                        </xsl:choose>                       
                    </condition>
                </target>
                <target name="wsimport-client-{$wsname}" depends="wsimport-init,wsimport-client-check-{$wsname}" unless="wsimport-client-{$wsname}.notRequired">
                    <property name="wsdl" location="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"/>
                    <xsl:if test="jaxws:package-name/@forceReplace">
                        <xsl:choose>
                            <xsl:when test="$xnocompile = 'true'">
                                <wsimport
                                    xnocompile="true"
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    package="{$package_name}"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{wsdl}}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text> ,</xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:when>
                            <xsl:otherwise>
                                <wsimport
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    package="{$package_name}"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{wsdl}}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text> ,</xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport> 
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <xsl:if test="not(jaxws:package-name/@forceReplace)">
                        <xsl:choose>
                            <xsl:when test="$xnocompile = 'true'">
                                <wsimport
                                    xnocompile="true"
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{wsdl}}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text> ,</xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:when>
                            <xsl:otherwise>
                                <wsimport
                                    sourcedestdir="${{build.generated.dir}}/wsimport/client"
                                    destdir="${{build.generated.dir}}/wsimport/binaries"
                                    wsdl="${{wsdl}}"
                                    wsdlLocation="{$wsdl_url_actual}"
                                    catalog="{$catalog}">
                                    <xsl:if test="jaxws:wsimport-options">
                                        <xsl:for-each select="jaxws:wsimport-options/jaxws:wsimport-option">
                                            <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                            <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                            <xsl:variable name="wsoption">
                                                <xsl:text><xsl:value-of select="$wsoptionname"/></xsl:text>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}"><xsl:value-of select="$wsoptionvalue"/></xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:if>
                                    <xsl:if test="jaxws:binding">
                                        <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
                                            <xsl:attribute name="includes">
                                                <xsl:for-each select="jaxws:binding">
                                                    <xsl:if test="position()!=1"><xsl:text> ,</xsl:text></xsl:if>
                                                    <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                                </xsl:for-each>
                                            </xsl:attribute>
                                        </binding>
                                    </xsl:if>
                                </wsimport>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </target>
                <target name="wsimport-client-clean-{$wsname}" depends="-init-project">
                    <delete dir="${{build.generated.dir}}/wsimport/client/{$package_path}"/>
                </target>
            </xsl:for-each>
            
            <!-- wsimport-client-generate and wsimport-client-compile targets -->
            <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <target name="wsimport-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="@name"/>
                            </xsl:variable>
                            <xsl:text>wsimport-client-</xsl:text><xsl:value-of select="@name"/>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
                <target name="wsimport-client-compile" depends="-pre-pre-compile">
                    <carproject:javac srcdir="${{build.generated.dir}}/wsimport/client" destdir="${{classes.dir}}"/>
                    <copy todir="${{classes.dir}}">
                        <fileset dir="${{build.generated.dir}}/wsimport/binaries" includes="**/*.xml"/>
                    </copy>
                </target>
            </xsl:if>
            
        </project>
        
    </xsl:template>
    
</xsl:stylesheet>
