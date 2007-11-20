<?xml version="1.0" ?>
<xsl:stylesheet id="JavaOperationFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4" xmlns:xsltHelper="org.netbeans.modules.uml.core.support.umlutils.XSLTHelper"> 
   <xsl:import href="AliasFormat.xsl"/>
   <xsl:import href="JavaParameterFormat.xsl"/>
   <xsl:output method="text" />
   
   <xsl:template match="UML:Operation" >
	     <xsl:call-template name="UML:Operation"/>
   </xsl:template>

   <xsl:template name="UML:Operation" >
 	      <!--
	         Output the visibility
	      -->
	      <xsl:variable name="visibility" select="@visibility" />
	      <xsl:variable name="showVisibility" select="xsltHelper:getPreferenceValue('Default|DisplaySettings|DisplayVisibility')"/>
	      <xsl:if test="$showVisibility = 'PSK_YES'">
		      <xsl:choose>
		         <xsl:when test="$visibility='public' or not($visibility)" >public </xsl:when>
		         <xsl:when test="$visibility='private'">private </xsl:when>
		         <xsl:when test="$visibility='protected'">protected </xsl:when>
			</xsl:choose>
	      </xsl:if>
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
		<!-- 
			Type 
		-->
	      <xsl:variable name="returnType" select="UML:Element.ownedElement/UML:Parameter[@direction = 'result']" />
	      <xsl:if test="count($returnType) = 1">
		      <xsl:for-each select="$returnType">
				<xsl:call-template name="UML:Parameter"/>
		      </xsl:for-each>
		      <!-- Space after return type -->         
			<xsl:text> </xsl:text>
	      </xsl:if>

		<!-- 
			Name/Alias processing 
		-->
		<xsl:call-template name="CheckForAlias">
			<xsl:with-param name="curNode" select="." />
			<xsl:with-param name="alias" select="@alias"/>
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
	      <!-- The starting paren for the parameters -->
	      <xsl:text>( </xsl:text>
	      <!--
	         Now we need to see if the operation has any parameters. We also don't want any
	         parameters that are result parameters. If we do have some parameters, we'll call the Parameters template to handle the formating.
	      -->
	      <xsl:variable name="parameters" select="UML:Element.ownedElement/UML:Parameter[not(@direction) or @direction != 'result']" />
	      <xsl:if test="count($parameters)">
	         <xsl:call-template name="Parameters">
	            <xsl:with-param name="parameters" select="$parameters"/>
	         </xsl:call-template>
	      </xsl:if>
	      <!-- Closing paren on the parameter list -->
	      <xsl:if test="count($parameters)">
		      <xsl:text> </xsl:text>
	      </xsl:if>
	      <xsl:text>)</xsl:text>
		<!-- 
			TaggedValues 
		-->
	      <xsl:variable name="showTVs" select="xsltHelper:getPreferenceValue('Default|DisplaySettings|DisplayTVs')"/>
	      <xsl:if test="$showTVs = 'PSK_YES'">
			<xsl:variable name="tvs" select="UML:Element.ownedElement/UML:TaggedValue[@name != 'documentation' and (@hidden != 'true' or not(@hidden))]" />
			<xsl:if test="count($tvs)">
				<xsl:text> { </xsl:text>
				<xsl:for-each select="$tvs">
					<xsl:call-template name="CheckForAlias">
						<xsl:with-param name="curNode" select="." />
						<xsl:with-param name="alias" select="@alias"/>
						<xsl:with-param name="name" select="@name"/>
					</xsl:call-template>
			       	<xsl:text>=</xsl:text><xsl:value-of select="UML:TaggedValue.dataValue"/>
			       	<xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
				</xsl:for-each>
				<xsl:text> } </xsl:text>
		      </xsl:if>
	      </xsl:if>
      
   </xsl:template>
   
   
   <!--
      The Parameters template handles the formatting of the parameter list for the
      select operation.
   -->
   <xsl:template name="Parameters" >
      <xsl:param name="parameters" />
      
      <!--
         Figure out how many parameters we have
      -->
      
      <xsl:variable name="numParms" select="count($parameters)"/>
      
      <xsl:for-each select="$parameters">
		<xsl:variable name="curNum" select="position()"/>
         
		<xsl:call-template name="UML:Parameter"/>
            
            <xsl:if test="$curNum &lt; $numParms">
               <xsl:text>, </xsl:text>
            </xsl:if>
            
      </xsl:for-each>
      
      <xsl:if test="$numParms &gt; 0">
         <xsl:text> </xsl:text>
      </xsl:if>
   </xsl:template>

</xsl:stylesheet>

  