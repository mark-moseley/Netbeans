<?xml version="1.0"?>
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
		xmlns:xalan="http://xml.apache.org/xalan"
    	xmlns:java="http://xml.apache.org/xslt/java"
		exclude-result-prefixes="xalan java">

<xsl:key name="platform" match="ManagedReport" use="concat(@osName,@osVersion,@osArch)"/>
<xsl:key name="build" match="ManagedReport" use="@build"/>
<xsl:key name="platformAndBuild" match="ManagedReport" use="concat(@osName,@osVersion,@osArch,@build)"/>
<xsl:key name="host" match="ManagedReport" use="@host"/>

<xsl:include href="../library.xsl"/>

<!-- global variables -->
<xsl:variable name="testedType" select="//ManagedReport/@testedType"/>
<xsl:variable name="testingGroup" select="//ManagedReport/@testingGroup"/>
<xsl:variable name="buildNumber" select="//ManagedReport/@build"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">
			Test results for <xsl:value-of select="//ManagedReport/@project"/> 
			build <xsl:value-of select="$buildNumber"/>
		</xsl:with-param>
	</xsl:call-template>
</xsl:template>



<xsl:template match="ManagedGroup">
	<xsl:call-template name="MakeBuildSummaryTable"/>
</xsl:template>

<xsl:template name="MakeBuildSummaryTable">
		
	<xsl:variable name="project" select="//ManagedReport/@project"/>
	<xsl:variable name="groupName" select="/ManagedGroup/@name"/>
	
	<H2>
		Test results for <xsl:value-of select="$project"/> 
		build <xsl:value-of select="$buildNumber"/> (<xsl:value-of select="$testingGroup"/>-<xsl:value-of select="$testedType"/> tests)
	</H2>	

	<UL>		
		<LI><A HREF="../../../index.html">XTest Overall Results</A></LI>
		<xsl:variable name="groupIndex" select="concat('../../',$groupName,'-',$testingGroup,'-',$testedType,'.html')"/>
		<xsl:variable name="normalizedGroupIndex" select="java:org.netbeans.xtest.util.FileUtils.normalizeName($groupIndex)"/>
		<LI><A HREF="{$normalizedGroupIndex}"><xsl:value-of select="$project"/> results	(<xsl:value-of select="$testingGroup"/>-<xsl:value-of select="$testedType"/>) </A></LI>
	</UL>


	<TABLE width="98%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">				
			<TD bgcolor="#A6CAF0" rowspan="1" colspan="2"><B>Build Totals</B></TD>				
			<TD colspan="8" bgcolor="#A6CAF0">
				<B>Tested Platforms</B>
			</TD>
		</TR>
		
		<TR align="center">
			<TD bgcolor="#A6CAF0">
				<B>Passed</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Total</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Operating System</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Passed</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Total</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Unexpected Passes</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Expected Fails</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Unexpected Fails</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Errors</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Testing Host(s)</B>
			</TD>
		</TR>
				
		
		
			
			<xsl:variable name="uniquePlatorms" select="//ManagedReport[generate-id(.)=generate-id(key('platform',concat(./@osName,./@osVersion,./@osArch))[1])]"/>			
			<xsl:variable name="platformCount" select="count($uniquePlatorms)"/>
			
			<!--
			<TR>
			<TD colspan="9">
			<TABLE cellspacing="2" cellpadding="5" border="0" WIDTH="100%">
			-->
			<TR></TR>
			
			<xsl:for-each select="$uniquePlatorms">
				<xsl:sort select="@osName"/>
				<xsl:sort select="@osVersion"/>
				<xsl:sort select="@osArch"/>
								
				<TR align="center">
					<xsl:if test="position() = 1">
						
						<xsl:variable name="buildPassed" select="sum(//ManagedReport/@testsPass)"/>
						<xsl:variable name="buildTotal" select="sum(//ManagedReport/@testsTotal)"/>						
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="format-number($buildPassed div $buildTotal,'0.00%')"/></TD>
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="$buildTotal"/></TD>
					</xsl:if>
					<TD class="pass">
						<B><xsl:value-of select="@osName"/>-<xsl:value-of select="@osVersion"/>-<xsl:value-of select="@osArch"/></B>
					</TD>
					<xsl:variable name="passed" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsPass)"/>
					<xsl:variable name="unexpectedPasses" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsUnexpectedPass)"/>
					<xsl:variable name="expectedFails" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsExpectedFail)"/>
					<xsl:variable name="failed" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsFail)"/>
					<xsl:variable name="errors" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsError)"/>
					<xsl:variable name="total" select="sum(key('platform',concat(./@osName,./@osVersion,./@osArch))/@testsTotal)"/>
					<TD class="pass">
						<xsl:value-of select="format-number($passed div $total,'0.00%')"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$total"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$unexpectedPasses"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$expectedFails"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$failed - $expectedFails"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$errors"/>
					</TD>
					<TD class="pass">
						<xsl:for-each select="key('platform',concat(./@osName,./@osVersion,./@osArch))">
                        
							<A HREF="../../../{@webLink}">								
							<xsl:choose>
							  	<xsl:when test="@mappedHostname">
							   		<xsl:value-of select="@mappedHostname"/>
							   	</xsl:when>								       
							   	<xsl:otherwise>
							   	 	<xsl:value-of select="@host"/>
							   	</xsl:otherwise>
							</xsl:choose>
							</A>
                        
							<BR/>
						</xsl:for-each>
					</TD>
				</TR>
				
				
			</xsl:for-each>
			<!--
			</TABLE>
			</TD>
			</TR>
			-->

		
	</TABLE>

	<BR/>
	<HR width="90%"/>

</xsl:template>

</xsl:stylesheet>