<?xml version="1.0"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="library.xsl"/>

<xsl:template match="/">
	<xsl:call-template name="html-page">
		<xsl:with-param name="html-title">Summary from <xsl:value-of select="/XTestResultsReport/SystemInfo/@host"/> run at <xsl:value-of select="/XTestResultsReport/@timeStamp"/></xsl:with-param>
	</xsl:call-template>
</xsl:template>

<xsl:template match="XTestResultsReport">
	<H4>
		<I>
		<B>XTest Results Report:</B><BR/><BR/>
		<xsl:if test="@project">
			Tested project:<B><xsl:value-of select="@project"/></B><BR/>
		</xsl:if>
		<xsl:if test="@build">
			Tested build:<B><xsl:value-of select="@build"/></B><BR/>
		</xsl:if>
		Run on: <B><xsl:value-of select="SystemInfo/@host"/></B><BR/>
		Run at: <B><xsl:value-of select="@timeStamp"/></B><BR/>						
		</I>
		<UL>			
			<LI><A HREF="systeminfo.html" TARGET="report">System Info</A></LI>	
			<BR/><BR/>					
			<LI><A HREF="testreport.html" TARGET="report">Full Results Report</A></LI>			
			<UL>							
				<xsl:for-each select="TestRun">			
				<LI>
					<A HREF="testreport.html#{@runID}" TARGET="report">
						<xsl:if test="@name">
							<xsl:value-of select="@name"/>
						</xsl:if>
						<xsl:if test="not(@name)">
							Test Run
						</xsl:if>
					</A>
					</LI>
				<UL>
					<xsl:for-each select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
						<xsl:sort select="@module"/>
						<LI><A HREF="testreport.html#{parent::*/@runID}-{@module}" TARGET="report"><xsl:value-of select="@module"/></A></LI>				
					</xsl:for-each>		
				</UL>
				</xsl:for-each>	
			</UL>			
			<BR/>
			<LI><A HREF="testreport-failures.html" TARGET="report">Failures Report</A></LI>	
			<UL>
				<xsl:if test="count(document('testreport-failures.xml',/*)/XTestResultsReport/TestRun)=0">
					<LI>No failures/errors encountered</LI>
				</xsl:if>
				<xsl:for-each select="document('testreport-failures.xml',/*)/XTestResultsReport/TestRun">
					<LI>
						<A HREF="testreport-failures.html#{@runID}" TARGET="report">
							<xsl:if test="@name">
								<xsl:value-of select="@name"/>
							</xsl:if>
							<xsl:if test="not(@name)">
								Test Run
							</xsl:if>
						</A>
					</LI>
					<UL>
						<xsl:for-each select="TestBag[not(./@module = preceding-sibling::TestBag/@module)]">
							<xsl:sort select="@module"/>
							<LI><A HREF="testreport-failures.html#{parent::*/@runID}-{@module}" TARGET="report"><xsl:value-of select="@module"/></A></LI>				
						</xsl:for-each>		
					</UL>				
				</xsl:for-each>
			</UL>
                        
                        <xsl:if test="count(document('testreport-performance.xml',/*)/XTestResultsReport/TestRun/TestBag/UnitTestSuite/Data/PerformanceData)!=0">
                			<BR/><LI><A HREF="testreport-performance.html" TARGET="report">Performance Results</A></LI>	
			</xsl:if>
		</UL>
		<UL>
			<LI><A HREF="../index.html" TARGET="_top">Home</A></LI>
		</UL>
	</H4>	
</xsl:template>

</xsl:stylesheet>