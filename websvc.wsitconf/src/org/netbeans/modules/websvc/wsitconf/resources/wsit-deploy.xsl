<?xml version="1.0" encoding="UTF-8"?>
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
Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.
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
        <project>
            <xsl:attribute name="default">-post-run-deploy</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <target name="-wsit-init">
                <property file="nbproject/private/private.properties"/>
                <condition property="user.properties.file" value="${{netbeans.user}}/build.properties">
                    <not>
                        <isset property="user.properties.file"/>
                    </not>
                </condition>
                <property file="${{deploy.ant.properties.file}}"/>
                <fail unless="user.properties.file">Must set user properties file</fail>
                <fail unless="sjsas.root">Must set Sun app server root</fail>
            </target>

            <target name="-post-run-deploy" depends="-wsit-init">
                <echo file="nbproject/wsit.properties" >AS_ADMIN_USERPASSWORD=changeit</echo>
                <exec executable="${{sjsas.root}}/bin/asadmin" failonerror="false" failifexecutionfails="false">
                    <arg value="create-file-user"/>
                    <arg value="--passwordfile"/>
                    <arg value="nbproject/wsit.properties"/>
                    <arg value="wsitUser"/>
                </exec>
            </target>
            
        </project>
    </xsl:template>
</xsl:stylesheet>
