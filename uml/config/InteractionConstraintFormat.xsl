<?xml version="1.0"?>
<xsl:stylesheet id="InteractionConstraintFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4">
   <xsl:output method="text" />
   
   <xsl:template match="UML:InteractionConstraint" >
      <xsl:call-template name="FormatInteractionConstraint">
      <xsl:with-param name="node" select="."/>
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="FormatInteractionConstraint" >
      <xsl:param name="node" />

      <!-- Output the associated expression. -->
	<xsl:text>[</xsl:text>
	<xsl:value-of select="$node/UML:Constraint.specification/UML:Expression/UML:Expression.body" />
	<xsl:text>]</xsl:text>
   </xsl:template>

</xsl:stylesheet>
