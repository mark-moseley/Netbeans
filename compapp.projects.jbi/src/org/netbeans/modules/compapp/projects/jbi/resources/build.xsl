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
                xmlns:project="http://www.netbeans.org/ns/project/1"
                xmlns:web="http://www.netbeans.org/ns/j2ee-ejbjarproject/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan project">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <!-- Annoyingly, the JAXP impl in JRE 1.4.2 seems to randomly reorder attrs. -->
        <!-- (I.e. the DOM tree gets them in an unspecified order?) -->
        <!-- As a workaround, use xsl:attribute for all but the first attr. -->
        <!-- This seems to produce them in the order you want. -->
        <!-- Tedious, but appears to do the job. -->
        <!-- Important for build.xml, which is very visible; not so much for build-impl.xml. -->

        <xsl:comment> You may freely edit this file. See commented blocks below for </xsl:comment>
        <xsl:comment> some examples of how to customize the build. </xsl:comment>
        <xsl:comment> (If you delete it and reopen the project it will be recreated.) </xsl:comment>
        
        <xsl:variable name="name" select="/project:project/project:configuration/web:data/web:name"/>
        <project name="{$name}">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">.</xsl:attribute>
            <description>Builds, tests, and runs the project <xsl:value-of select="$name"/>.</description>
            <import file="nbproject/build-impl.xml"/>

            <xsl:comment><![CDATA[

    There exist several targets which are by default empty and which can be 
    used for execution of your tasks. These targets are usually executed 
    before and after some main targets. They are: 

      pre-init:                 called before initialization of project properties 
      post-init:                called after initialization of project properties 
      pre-compile:              called before javac compilation 
      post-compile:             called after javac compilation 
      pre-compile-single:       called before javac compilation of single file
      post-compile-single:      called after javac compilation of single file
      pre-dist:                 called before jar building 
      post-dist:                called after jar building 
      post-clean:               called after cleaning build products 

    Example of pluging an obfuscator after the compilation could look like 

        <target name="post-compile">
            <obfuscate>
                <fileset dir="${build.classes.dir}"/>
            </obfuscate>
        </target>

    For list of available properties check the imported 
    nbproject/build-impl.xml file. 


    Other way how to customize the build is by overriding existing main targets.
    The target of interest are: 

      init-macrodef-javac:    defines macro for javac compilation
      init-macrodef-debug:    defines macro for class debugging
      do-dist:                jar archive building
      run:                    execution of project 
      javadoc-build:          javadoc generation 

    Example of overriding the target for project execution could look like 

        <target name="run" depends="<PROJNAME>-impl.jar">
            <exec dir="bin" executable="launcher.exe">
                <arg file="${dist.jar}"/>
            </exec>
        </target>

    Notice that overridden target depends on jar target and not only on 
    compile target as regular run target does. Again, for list of available 
    properties which you can use check the target you are overriding in 
    nbproject/build-impl.xml file. 

    ]]></xsl:comment>

            <target name="-check-netbeans-home">
                <condition property="no.netbeans.home">
                    <not>
                        <isset property="netbeans.home"/>
                    </not>
                </condition>
            </target>

            <target name="-init-caps" if="no.netbeans.home">
                <xsl:attribute name="if">no.netbeans.home</xsl:attribute>

                <property file="${{basedir}}/nbproject/private/private.properties"/>
                <property name="netbeans.home" value="${{caps.netbeans.home}}/platform8"/>
                <property name="netbeans.user" value="${{caps.netbeans.user}}"/>
                <property name="from.commandline" value="true"/>
            </target>

            <target name="pre-init">
                <xsl:attribute name="depends">-check-netbeans-home,-init-caps</xsl:attribute>
                <!--
                <echo>netbeans.home: ${netbeans.home}</echo>
                <echo>netbeans.user: ${netbeans.user}</echo>
                -->
            </target>

        </project>

    </xsl:template>
    
</xsl:stylesheet> 
