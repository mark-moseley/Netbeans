<?xml version="1.0" ?>
<xsl:stylesheet id="UMLAttributeFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4" xmlns:xsltHelper="org.netbeans.modules.uml.core.support.umlutils.XSLTHelper">
	<xsl:import href="AliasFormat.xsl"/>
	<xsl:import href="MultiplicityFormat.xsl"/>
   <xsl:output method="text" />

   <xsl:template match="UML:Attribute" >
      <xsl:call-template name="UML:Attribute"/>
   </xsl:template>

   <xsl:template name="UML:Attribute" >
   
	      <!--
	         Output the visibility
	      -->
	      <xsl:variable name="visibility" select="@visibility" />
	      <xsl:variable name="showVisibility" select="xsltHelper:getPreferenceValue('Default|DisplaySettings|DisplayVisibility')"/>
	      <xsl:if test="$showVisibility = 'PSK_YES'">
		      <xsl:choose>
		         <xsl:when test="$visibility='public' or not($visibility)" >+ </xsl:when>
		         <xsl:when test="$visibility='private'">- </xsl:when>
		         <xsl:when test="$visibility='protected'"># </xsl:when>
		         <xsl:when test="$visibility='package'">~ </xsl:when>
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
			If the Attribute is derived, show the '/' before the name of the attribute
		-->
		<xsl:if test="@isDerived = 'true'">
			<xsl:text>/</xsl:text>
		</xsl:if>   
		<!-- 
			Name/Alias processing 
		-->
		<xsl:call-template name="CheckForAlias">
			<xsl:with-param name="curNode" select="." />
			<xsl:with-param name="alias" select="@alias"/>
			<xsl:with-param name="name" select="@name"/>
		</xsl:call-template>
		<xsl:text> : </xsl:text>
		<!-- 
			Type 
		-->
      		<xsl:variable name="typeStr" select="xsltHelper:getValueFromExpansionVariable(., 'typeName')"/>
	      <xsl:value-of select="$typeStr" />
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
		<!-- 
			Default Value 
		-->
	      <xsl:variable name="expression" select="UML:Attribute.default/UML:Expression/UML:Expression.body" />
	      <xsl:if test="string-length( $expression )"> = <xsl:value-of select="$expression" />
	      </xsl:if>
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

</xsl:stylesheet>

  