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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:sproject="http://www.netbeans.org/ns/nb-module-suite-project/1"
                exclude-result-prefixes="xalan p">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
]]></xsl:comment>
        <xsl:variable name="name" select="/p:project/p:configuration/sproject:data/sproject:name"/>
        <project name="{$name}-impl">
            <xsl:attribute name="basedir">..</xsl:attribute>
            <property file="nbproject/private/platform-private.properties"/>
            <property file="nbproject/platform.properties"/>
            <macrodef name="property" uri="http://www.netbeans.org/ns/nb-module-suite-project/1">
                <attribute name="name"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{name}}" value="${{@{{value}}}}"/>
                </sequential>
            </macrodef>
            <!-- Do not do this, as it is legal to define harness.dir and netbeans.dest.dir explicitly:
            <fail unless="nbplatform.active">You must set platform.properties to name your active NB platform</fail>
            -->
            <property file="${{user.properties.file}}"/>
            <sproject:property name="harness.dir" value="nbplatform.${{nbplatform.active}}.harness.dir"/>
            <sproject:property name="netbeans.dest.dir" value="nbplatform.${{nbplatform.active}}.netbeans.dest.dir"/>
            <fail message="You must define 'nbplatform.${{nbplatform.active}}.harness.dir'">
                <condition>
                    <not>
                        <available file="${{harness.dir}}" type="dir"/>
                    </not>
                </condition>
            </fail>
            <import file="${{harness.dir}}/suite.xml"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
