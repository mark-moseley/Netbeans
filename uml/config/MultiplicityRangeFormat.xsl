<?xml version="1.0" ?>
<xsl:stylesheet id="MultiplicityRangeFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4">
   <xsl:output method="text" />

	<!--
		Handles the formatting of the Multiplity ranges
	-->
   <xsl:template match="UML:MultiplicityRange" >
      <xsl:call-template name="UML:MultiplicityRange"/>
   </xsl:template>

   <xsl:template name="UML:MultiplicityRange" >
         <xsl:variable name="lower" select="@lower"/>
	  <xsl:variable name="upper" select="@upper"/>
        
	<xsl:choose>
		<xsl:when test="( string-length( $lower ) &gt; 0 ) and ( string-length( $upper ) &gt; 0 )" >
			<xsl:value-of select="$lower" />
			<xsl:text>..</xsl:text>
			<xsl:value-of select="$upper" />
		</xsl:when>

		<xsl:when test="( string-length( $lower ) &gt; 0 ) and ( string-length( $upper ) = 0 )" >
			<xsl:value-of select="$lower" />
		</xsl:when>

		<xsl:otherwise>
			<xsl:value-of select="$upper" />
		</xsl:otherwise>
	</xsl:choose>
   </xsl:template>

</xsl:stylesheet>

  