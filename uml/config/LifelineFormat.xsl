<?xml version="1.0" ?>
<xsl:stylesheet id="LifelineFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4" xmlns:xsltHelper="org.netbeans.modules.uml.core.support.umlutils.XSLTHelper">
  <xsl:import href="AliasFormat.xsl" /> 
  <xsl:output method="text" />

	<!--
		This script formats the data found on a lifeline in order to 
		display what is seen at the head of the lifeline. The format,
		as dictated in the UML2.0 spec is as follows:

		lifelineident ::= partname [ selector ] [: class of Part] [decomposition]
		selector ::= expression
		decomposition ::= ref interactionident
	-->

   <xsl:template match="UML:Lifeline" >

		<!--
			Output the name of the part. Don't do this if the Part is actually
			an Actor
		-->

		<xsl:call-template name="CheckForAlias">
		  <xsl:with-param name="curNode" select="." /> 
		  <xsl:with-param name="alias" select="@alias" /> 
		  <xsl:with-param name="name" select="@name" />
		</xsl:call-template>

		<!--
			If the part has a discriminator, display it
		-->

		<xsl:variable name="discriminator" select="UML:Lifeline.discriminator/UML:Expression.body" />
		<xsl:if test="string-length( $discriminator )" >
			<xsl:text> [ </xsl:text><xsl:value-of select="$discriminator" /><xsl:text> ] </xsl:text>
		</xsl:if>

		<xsl:text> : </xsl:text>

		<xsl:choose>
			<xsl:when test="xsltHelper:getProjectAliased(.)" >
				<xsl:variable name="classifierAlias" select="xsltHelper:getValueFromExpansionVariable(., 'lifelineClassifierAlias')" />
				<xsl:choose>
					<xsl:when test="string-length( $classifierAlias )">
						<xsl:value-of select="$classifierAlias" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="classifierName" select="xsltHelper:getValueFromExpansionVariable(., 'lifelineClassifierName')" />
						<xsl:choose>
							<xsl:when test="string-length( $classifierName )">
								<xsl:value-of select="$classifierName " />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="xsltHelper:getValueFromExpansionVariable(., 'lifelineName')" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="classifierName" select="xsltHelper:getValueFromExpansionVariable(., 'lifelineClassifierName')" />
				<xsl:choose>
					<xsl:when test="string-length( $classifierName )">
						<xsl:value-of select="$classifierName " />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="xsltHelper:getValueFromExpansionVariable(., 'lifelineName')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
   
</xsl:stylesheet>

  