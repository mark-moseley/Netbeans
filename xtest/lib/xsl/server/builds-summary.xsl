<?xml version="1.0"?>
<!--
                 Sun Public License Notice
 
 The contents of this file are subject to the Sun Public License
 Version 1.0 (the "License"). You may not use this file except in
 compliance with the License. A copy of the License is available at
 http://www.sun.com/
 
 The Original Code is NetBeans. The Initial Developer of the Original
 Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 Microsystems, Inc. All Rights Reserved.

-->
<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xalan="http://xml.apache.org/xalan"
		exclude-result-prefixes="xalan">

<xsl:key name="platform" match="ManagedReport" use="concat(@osName,@osVersion,@osArch)"/> 
<xsl:key name="build" match="ManagedReport" use="@build"/>
<xsl:key name="platformAndBuild" match="ManagedReport" use="concat(@osName,@osVersion,@osArch,@build)"/> 
<xsl:key name="host" match="ManagedReport" use="@host"/>

<xsl:include href="../library.xsl"/>

<xsl:param name="truncated"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 
	tested by <xsl:value-of select="//ManagedReport/@testingGroup"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>



<xsl:template match="XTestWebReport">
	<xsl:call-template name="MakeBuildsSummaryTable"/>
</xsl:template>

<xsl:template name="MakeBuildsSummaryTable">
		
	
	<H2>
		Results of <xsl:value-of select="//ManagedReport/@testedType"/> tests for <xsl:value-of select="//ManagedReport/@project"/> 	
		tested by <xsl:value-of select="//ManagedReport/@testingGroup"/>
	</H2>	

	<UL>
		<LI><A HREF="../index.html">XTest Overall Results</A></LI>
		<xsl:if test="not(@oldBuilds)">
			<LI><A HREF="#history">History Matrices</A></LI>
			<LI>
                           <xsl:if test="not(boolean($truncated))">
                              <A HREF="old-{ManagedReport/@testingGroup}-{ManagedReport/@testedType}.html">Older Builds</A>
                           </xsl:if>
                           <xsl:if test="boolean($truncated)">
                              <A><xsl:attribute name="href"><xsl:value-of select="translate(concat('old-',ManagedReport/@testingGroup,'-',ManagedReport/@testedType,'.html'),' ','_')"/></xsl:attribute>Older Builds</A>
                           </xsl:if>
                        </LI>
		</xsl:if>
		<xsl:if test="@oldBuilds">
			<LI>
                           <xsl:if test="not(boolean($truncated))">
                             <A HREF="{ManagedReport/@testingGroup}-{ManagedReport/@testedType}.html">Current builds</A>
                           </xsl:if>
                           <xsl:if test="boolean($truncated)">
                             <A><xsl:attribute name="href"><xsl:value-of select="translate(concat(ManagedReport/@testingGroup,'-',ManagedReport/@testedType,'.html'),' ','_')"/></xsl:attribute>Current builds</A>
                           </xsl:if>
                        </LI>
		</xsl:if>
	</UL>
	<BR/>

	<TABLE width="98%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">
			<TD bgcolor="#A6CAF0" rowspan="2"><B>Build</B></TD>		
			<TD bgcolor="#A6CAF0" rowspan="1" colspan="2"><B>Build Totals</B></TD>				
			<TD colspan="6" bgcolor="#A6CAF0">
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
			 	<B>Failed</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Errors</B>
			</TD>
			<TD bgcolor="#A6CAF0">
			 	<B>Testing Host(s)</B>
			</TD>
		</TR>
				
		<xsl:variable name="uniqueBuildList" select="//ManagedReport[generate-id(.)=generate-id(key('build',./@build)[1])]"/>
		
		<xsl:for-each select="$uniqueBuildList">
			<xsl:sort select="@build" order = "descending"/>
			<xsl:variable name="currentBuild" select="@build"/>
			<xsl:variable name="uniquePlatormsInBuild" select="//ManagedReport[generate-id(.)=generate-id(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))[1])]"/>			
			<xsl:variable name="platformCount" select="count($uniquePlatormsInBuild)"/>
			
			<!--
			<TR>
			<TD colspan="9">
			<TABLE cellspacing="2" cellpadding="5" border="0" WIDTH="100%">
			-->
			<TR></TR>
			
			<xsl:for-each select="$uniquePlatormsInBuild">
				<xsl:sort select="@osName"/>
				<xsl:sort select="@osVersion"/>
				<xsl:sort select="@osArch"/>
				
				<TR align="center">
					<xsl:if test="position() = 1">
						<TD bgcolor="#A6CAF0" rowspan="{$platformCount}"><B><xsl:value-of select="@build"/></B></TD>
						<xsl:variable name="buildPassed" select="sum(key('build',$currentBuild)/@testsPass)"/>
						<xsl:variable name="buildTotal" select="sum(key('build',$currentBuild)/@testsTotal)"/>						
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="format-number($buildPassed div $buildTotal,'0.00%')"/></TD>
						<TD class="pass" rowspan="{$platformCount}"><xsl:value-of select="$buildTotal"/></TD>
					</xsl:if>
					<TD class="pass">
						<B><xsl:value-of select="@osName"/>-<xsl:value-of select="@osVersion"/>-<xsl:value-of select="@osArch"/></B>
					</TD>
					<xsl:variable name="passed" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsPass)"/>
					<xsl:variable name="failed" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsFail)"/>
					<xsl:variable name="errors" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsError)"/>
					<xsl:variable name="total" select="sum(key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))/@testsTotal)"/>
					<TD class="pass">
						<xsl:value-of select="format-number($passed div $total,'0.00%')"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$total"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$failed"/>
					</TD>
					<TD class="pass">
						<xsl:value-of select="$errors"/>
					</TD>
					<TD class="pass">
						<xsl:for-each select="key('platformAndBuild',concat(./@osName,./@osVersion,./@osArch,$currentBuild))">
                                                    <xsl:if test="not(boolean($truncated))">
							<A HREF="../{@webLink}index.html"><xsl:value-of select="@host"/>
							</A>
                                                    </xsl:if>
                                                    <xsl:if test="boolean($truncated)">
                                                        <A><xsl:attribute name="href"><xsl:value-of select="translate(concat('../',@webLink,'index.html'),' ','_')"/></xsl:attribute><xsl:value-of select="@host"/>
							</A>
                                                    </xsl:if>    
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
		</xsl:for-each>
		
	</TABLE>
	<!--
	<P>
	<H5>Legend:</H5>
	<UL>
		<LI>Q: - quality of the product - (passed tests)/(total tests) in %</LI>
		<LI>T: - total number of run tests</LI>
		<LI>D: - date and time when the tests were run</LI>
	</UL>
	<BR/>
	</P>
	-->
	<BR/>
	<HR width="90%"/>
	<xsl:if test="not(//XTestWebReport/@oldBuilds)">
		<A name="history">
		<H5>History Matrices for:</H5>
		</A>
		<UL>
			<xsl:variable name="differentHosts" select="//ManagedReport[generate-id(.)=generate-id(key('host',./@host)[1])]"/>
			<xsl:for-each select="$differentHosts">
				<xsl:sort select="@host"/>
				<LI>				
					<B>
                                           <xsl:if test="not(boolean($truncated))">
                                              <A HREF="matrix-{@testingGroup}-{@testedType}-{@host}.html"><xsl:value-of select="@host"/></A>
                                           </xsl:if>
                                           <xsl:if test="boolean($truncated)">
                                              <A><xsl:attribute name="href"><xsl:value-of select="translate(concat('matrix-',@testingGroup,'-',@testedType,'-',@host,'.html'),' ','_')"/></xsl:attribute>
                                              <xsl:value-of select="@host"/></A>
                                           </xsl:if>
                                       </B>
				</LI>
			</xsl:for-each>
		</UL>
	</xsl:if>	
</xsl:template>

</xsl:stylesheet>