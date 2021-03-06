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
Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
<!--
XXX should not have changed /1 to /2 for URI of *all* macrodefs; only the ones
that actually changed semantically as a result of supporting multiple compilation
units. E.g. <webproject1:property/> did not change at all, whereas
<webproject1:javac/> did. Need to only update URIs where necessary; otherwise we
cause gratuitous incompatibilities for people overriding macrodef targets. Also
we will need to have an upgrade guide that enumerates all build script incompatibilities
introduced by support for multiple source roots. -jglick
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:p="http://www.netbeans.org/ns/project/1"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:webproject1="http://www.netbeans.org/ns/web-project/1"
    xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
    xmlns:webproject3="http://www.netbeans.org/ns/web-project/3"
    xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
    xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"
    exclude-result-prefixes="xalan p projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
   
    <xsl:template match="/">

        <xsl:comment><![CDATA[
        *** GENERATED - DO NOT EDIT  ***

        For the purpose of easier reading the script
        is divided into following sections:
        - initialization
        - execution
        ]]></xsl:comment>

        <project>
            <xsl:attribute name="default">-am-deploy</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <target name="-pre-init-am">
                <subant>
                    <xsl:attribute name="target">-am-classpath-setup</xsl:attribute>
                    <xsl:attribute name="antfile">nbproject/am-deploy.xml</xsl:attribute>
                    <xsl:attribute name="buildpath">${basedir}</xsl:attribute>
                </subant>
                <property file="nbproject/private/private.properties_am"/>
                <delete file="nbproject/private/private.properties_am"/>
            </target>
            
            <target name="-run-deploy-am">
                <xsl:attribute name="depends">-am-deploy</xsl:attribute>
            </target>

            <xsl:comment>
                ======================
                INITIALIZATION SECTION
                ======================
            </xsl:comment>

            <target name="-am-init">
                <xsl:comment> Initialize properties here. </xsl:comment>
                <echo message="am-init:"/>
                <property file="nbproject/private/private.properties"/>
                <condition property="user.properties.file" value="${{netbeans.user}}/build.properties">
                    <not>
                        <isset property="user.properties.file"/>
                    </not>
                </condition>
                <property file="${{user.properties.file}}"/>
                <property file="${{deploy.ant.properties.file}}"/>
                <fail unless="user.properties.file">Must set user properties file</fail>
                <fail unless="deploy.ant.properties.file">Must set ant deploy properties</fail>
                <property file="nbproject/project.properties"/>
                <fail unless="sjsas.root">Must set Sun app server root</fail>
                <property name="am.as.url" value="[${{sjsas.root}}]deployer:Sun:AppServer::${{sjsas.host}}:${{sjsas.port}}"/>
                <condition property="amconf.dir" value="${{conf.dir}}" else="${{meta.inf}}">
                    <isset property="conf.dir"/>
                </condition>
                <property name="am.config.xml.dir" value="${{basedir}}/${{amconf.dir}}"/>
            </target>
            
            <target name="-am-task-init" unless="netbeans.home">
                <xsl:attribute name="depends">-am-init</xsl:attribute>
                <echo message="am-task-init:"/>
                <taskdef>
                    <xsl:attribute name="name">amdeploy</xsl:attribute>
                    <xsl:attribute name="classname">org.netbeans.modules.identity.ant.AMDeploy</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="path">${libs.IdentityAntTasks.classpath};${libs.jaxb20.classpath}</xsl:attribute>
                    </classpath>
                </taskdef>
                
                <taskdef>
                    <xsl:attribute name="name">amclasspathsetup</xsl:attribute>
                    <xsl:attribute name="classname">org.netbeans.modules.identity.ant.AMClassPathSetup</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="path">${libs.IdentityAntTasks.classpath}</xsl:attribute>
                    </classpath>
                </taskdef>
            </target>

            <xsl:comment>
                ======================
                EXECUTION SECTION
                ======================
            </xsl:comment>

            <target name="-am-deploy" if="libs.IdentityAntTasks.classpath">
                <xsl:attribute name="depends">-am-task-init</xsl:attribute>
                <xsl:attribute name="description">Deploy to Access Manager.</xsl:attribute>
                <echo message="am-deploy:"/>          
                <amdeploy>
                    <xsl:attribute name="amasurl">${am.as.url}</xsl:attribute>
                    <xsl:attribute name="amconfigxmldir">${am.config.xml.dir}</xsl:attribute>
                </amdeploy>
            </target>
            
            <target name="-am-classpath-setup" if="libs.IdentityAntTasks.classpath">
                <xsl:attribute name="depends">-am-task-init</xsl:attribute>
                <xsl:attribute name="description">Set up Access Manager classpath</xsl:attribute>
                <echo message="am-classpath-setup:"/>          
                <amclasspathsetup>
                    <xsl:attribute name="propertiesfile">${basedir}/nbproject/private/private.properties</xsl:attribute>
                    <xsl:attribute name="asroot">${sjsas.root}</xsl:attribute>
                </amclasspathsetup>
            </target>
        </project>
    </xsl:template>
</xsl:stylesheet>
