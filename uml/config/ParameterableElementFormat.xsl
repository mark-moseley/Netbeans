<?xml version="1.0"?>
<xsl:stylesheet id="ParameterableElementFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4" xmlns:uriHelper="org.netbeans.modules.uml.core.support.umlsupport.URIHelper"> 
   <xsl:import href="AliasFormat.xsl"/>
   <xsl:import href="MultiplicityFormat.xsl"/>
   <xsl:output method="text" />
   
   <xsl:template match="UML:ParameterableElement" >
      <xsl:call-template name="UML:ParameterableElement"/>
   </xsl:template>

   <xsl:template name="UML:ParameterableElement" >
	 <!-- Output the name of the ParameterableElement -->
	 <xsl:text> </xsl:text>
	<xsl:call-template name="CheckForAlias">
		<xsl:with-param name="curNode" select="." />
		<xsl:with-param name="alias" select="@alias"/>
		<xsl:with-param name="name" select="@name"/>
	</xsl:call-template>
	
	<xsl:variable name="defaultElement" select="@default" />
	<xsl:if test="string-length( $defaultElement )" >
		<xsl:text>::</xsl:text>
		<xsl:variable name="defName" select="xsltHelper:getValueFromExpansionVariable(., 'defaultName')"/>
		<xsl:variable name="defAlias" select="xsltHelper:getValueFromExpansionVariable(., 'defaultAlias')"/>
		<xsl:call-template name="CheckForAlias">
			<xsl:with-param name="curNode" select="." />
			<xsl:with-param name="alias" select="$defAlias"/>
			<xsl:with-param name="name" select="$defName"/>
		</xsl:call-template>
	</xsl:if>
   </xsl:template>

</xsl:stylesheet>

  