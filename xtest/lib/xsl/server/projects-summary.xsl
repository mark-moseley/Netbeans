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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:include href="../library.xsl"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">XTest Overall Results</xsl:with-param>
	</xsl:call-template>
</xsl:template>


<xsl:template match="XTestWebReport">
	<xsl:call-template name="MakeProjectsTestsSummaryTable"/>
</xsl:template>

<xsl:template name="MakeProjectsTestsSummaryTable">
	<xsl:variable name="differentProjectsExpression" select="//ManagedReport[not(./@project = preceding-sibling::ManagedReport/@project)]"/>
	<H1>XTest Overall Results:</H1>	
	<BR/>
	<BR/>
	<TABLE width="90%" cellspacing="2" cellpadding="5" border="0" >		
		<TR align="center">
			<TD></TD><TD></TD>
			<TD colspan="{count($differentProjectsExpression)}" bgcolor="#A6CAF0">
				<B>Tested Products</B>
			</TD>
		</TR>				
		<TR align="center">
		<TD bgcolor="#A6CAF0"><B>Testing Group</B></TD><TD bgcolor="#A6CAF0"><B>Tested Type</B></TD>
		<xsl:for-each select="$differentProjectsExpression">
			<xsl:sort select="@project"/>
			<TD class="pass">
				<B><xsl:value-of select="@project"/></B>
			</TD>
		</xsl:for-each>
		</TR>
		<xsl:for-each select="//ManagedReport[not(./@testingGroup = preceding-sibling::ManagedReport/@testingGroup)]">
			<xsl:sort select="@testingGroup"/>
			<xsl:variable name="currentTestingGroup" select="@testingGroup"/>					
					
			<xsl:for-each select="//ManagedReport[(@testingGroup=$currentTestingGroup) and not (./@testedType = preceding-sibling::ManagedReport[@testingGroup=$currentTestingGroup]/@testedType)]">
				<xsl:sort select="@testedType"/>
				<xsl:variable name="currentTestedType" select="@testedType"/>
				<TR align="center">
				<!-- now do owned test types -->
					<TD class="pass">
						<B><xsl:value-of select="@testingGroup"/></B>
					</TD>
					<TD class="pass">
						<B><xsl:value-of select="@testedType"/></B>
					</TD>
					<!-- now get results for these tests -->
					<xsl:for-each select="//ManagedReport[not(./@project = preceding-sibling::ManagedReport/@project)]">
						<xsl:sort select="@project"/>
						<xsl:variable name="currentProject" select="@project"/>
						<xsl:variable name="expression" select="//ManagedReport[(@testingGroup=$currentTestingGroup)and(@testedType=$currentTestedType)and(@project=$currentProject)]"/>
						<TD class="pass">
							<xsl:variable name="testsTotal" select="sum($expression/@testsTotal)"/>
							
							<xsl:if test="$testsTotal &gt; 0">
								<A HREF="{$currentProject}-{$currentTestingGroup}-{$currentTestedType}.html">
								Q: <xsl:value-of select="format-number(sum(($expression)/@testsPass) div sum(($expression)/@testsTotal),'0.00%')"/>, 
								T: <xsl:value-of select="sum($expression/@testsTotal)"/>,
								R:<xsl:value-of select="count($expression)"/>
								</A>
							</xsl:if>
							<xsl:if test="$testsTotal = 0">
								-
							</xsl:if>
						</TD>
					</xsl:for-each>
				</TR>	
			</xsl:for-each>
			
		</xsl:for-each>
		
	</TABLE>	
	<P>
	<H5>Legend:</H5>
	<UL>
		<LI>Q: - quality of the product - (passed tests)/(total tests) in %</LI>
		<LI>T: - total number of run tests</LI>
		<LI>R: - total number of test runs (i.e. how many times tests were run)</LI>
	</UL>
	<BR/>
	</P>
	<P>
	<H5>Some othe interesting statistics:</H5>
	<UL>
		<LI>Total number of run tests: <xsl:value-of select="sum(//ManagedReport/@testsTotal)"/></LI>
		<LI>Total number of passing tests : <xsl:value-of select="sum(//ManagedReport/@testsPass)"/></LI>
		<LI>Total number of failing tests : <xsl:value-of select="sum(//ManagedReport/@testsFail)"/></LI>
		<LI>Total number of tests finished with an error: <xsl:value-of select="sum(//ManagedReport/@testsError)"/></LI>
		<xsl:variable name="totalTime" select="sum(//ManagedReport/@time)"/>
		<xsl:variable name="totalMinutes" select="$totalTime div 1000 div 60"/>
		<LI>
			Total amount of time spent on running tests: <xsl:value-of select="format-number($totalMinutes,'00.00')"/> minutes.
		</LI>
	</UL>
	<HR width="90%"/>
	</P>
	<P>
		<H5><I>Please note, this is a pilot project of XTest, so the layout and functionality of these pages may not be final and can change anytime. If you 
		are interested in the project, please see details at 
		<A HREF="http://xtest.netbeans.org">XTest homepage</A>.</I></H5>
	</P>
</xsl:template>




</xsl:stylesheet>