<?xml version="1.0" encoding="UTF-8" ?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" />
    <xsl:param name="code.name.base" />


    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="//api[@type='export' and @group='java' and @category='official']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='official']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='stable']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='stable']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='devel']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='devel']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='friend']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='friend']" />
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="print-properties" >
        <xsl:param name="api" />
        
        <xsl:text>arch.</xsl:text>
        <xsl:value-of select="translate($code.name.base,'-','.')"/>
        <xsl:text>.name=</xsl:text>
        <xsl:value-of select="$api/@name"/>
        <xsl:text>
</xsl:text>

        <xsl:text>arch.</xsl:text>
        <xsl:value-of select="translate($code.name.base,'-','.')"/>
        <xsl:text>.category=</xsl:text>
        <xsl:value-of select="$api/@category"/>
        <xsl:text>
</xsl:text>
    </xsl:template>
</xsl:stylesheet> 
