<?xml version="1.0" ?>
<xsl:stylesheet id="JavaParameterFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4" xmlns:xsltHelper="org.netbeans.modules.uml.core.support.umlutils.XSLTHelper"> 
   <xsl:import href="AliasFormat.xsl"/>
   <xsl:import href="MultiplicityFormat.xsl"/>
   <xsl:output method="text" />
   
   <xsl:template match="UML:Parameter" >
      <xsl:call-template name="UML:Parameter"/>
   </xsl:template>

   <xsl:template name="UML:Parameter" >
     
         <xsl:variable name="direction" select="@direction"/>
	  <xsl:variable name="projLang" select="ancestor::UML:Project/@defaultLanguage"/>
         
         <xsl:choose>
	         <!--
	         	Processing for the return type parameter
	         -->
		<xsl:when test="$direction = 'result' ">
			<!-- 
				Name/Alias processing 
			-->
	      		<xsl:variable name="typeName" select="xsltHelper:getValueFromExpansionVariable(., 'typeName')"/>
	      		<xsl:variable name="typeAlias" select="xsltHelper:getValueFromExpansionVariable(., 'typeAlias')"/>
	            <xsl:call-template name="CheckForAlias">
	                <xsl:with-param name="curNode" select="." />
	                <xsl:with-param name="alias" select="$typeAlias"/>
	                <xsl:with-param name="name" select="$typeName"/>
	            </xsl:call-template>
			<!-- 
				Check for Multiplicity 
			-->
			<xsl:variable name="ranges" select="UML:TypedElement.multiplicity//UML:MultiplicityRange" />
			<xsl:variable name="left">[</xsl:variable>
			<xsl:variable name="right">]</xsl:variable>
			<xsl:if test="count($ranges)">
			         <xsl:call-template name="UML:Multiplicity">
	            			  <xsl:with-param name="ranges" select="$ranges"/>
	            			  <xsl:with-param name="left" select="$left"/>
	            			  <xsl:with-param name="right" select="$right"/>
			         </xsl:call-template>
		      </xsl:if>
		</xsl:when>
		<xsl:otherwise>
		         <!--
		         	Processing for all other parameters
		         -->
			<!-- 
				Show stereotype based on a preference
			-->
		      <xsl:variable name="showStereo" select="xsltHelper:getPreferenceValue('Default|DisplaySettings|DisplayStereotype')"/>
		      <xsl:if test="$showStereo = 'PSK_YES'">
		      		<xsl:variable name="curNode" select="."/>
		      		<xsl:variable name="stereoStr" select="xsltHelper:getValueFromExpansionVariable($curNode, 'stereotypeName')"/>
		      		<xsl:if test="string-length($stereoStr)">
		      			<xsl:text>&lt;&lt;</xsl:text><xsl:value-of select="$stereoStr"/><xsl:text>&gt;&gt; </xsl:text>
		      		</xsl:if>
		      </xsl:if>

			<!-- Parameter Type -->
		      <xsl:variable name="typeStr" select="xsltHelper:getValueFromExpansionVariable(., 'typeName')"/>
		      <xsl:value-of select="$typeStr" />

	            <!-- Output the name of the Parameter -->
	            <xsl:text> </xsl:text>
			<xsl:call-template name="CheckForAlias">
				<xsl:with-param name="curNode" select="." />
				<xsl:with-param name="alias" select="@alias"/>
				<xsl:with-param name="name" select="@name"/>
			</xsl:call-template>
	            
	            <!-- Check for Multiplicity -->
			<xsl:variable name="ranges" select="UML:TypedElement.multiplicity//UML:MultiplicityRange" />
			<xsl:variable name="left">[</xsl:variable>
			<xsl:variable name="right">]</xsl:variable>
			<xsl:if test="count($ranges)">
			         <xsl:call-template name="UML:Multiplicity">
	            			  <xsl:with-param name="ranges" select="$ranges"/>
	            			  <xsl:with-param name="left" select="$left"/>
	            			  <xsl:with-param name="right" select="$right"/>
			         </xsl:call-template>
		      </xsl:if>
		            
	            <!-- Let's check to see if the parameter has a default value -->
	            <xsl:variable name="defaultValue" select="UML:Parameter.default/UML:Expression/UML:Expression.body" />
	            <xsl:if test="string-length( $defaultValue )" >
	               <xsl:text> = </xsl:text>
	               <xsl:value-of select="$defaultValue" />
	            </xsl:if>
		</xsl:otherwise>
	  </xsl:choose>
   </xsl:template>

</xsl:stylesheet>

  