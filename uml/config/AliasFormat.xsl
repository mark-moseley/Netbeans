<?xml version="1.0"?>
<xsl:stylesheet id="AliasFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4" xmlns:uriHelper="org.netbeans.modules.uml.core.support.umlsupport.URIHelper" xmlns:xsltHelper="org.netbeans.modules.uml.core.support.umlutils.XSLTHelper">
   <xsl:output method="text" />
   
   <xsl:template name="CheckForAlias" >
		<!--
			This is the current node being processed
		-->

		<xsl:param name="curNode" />

		<!--
			This is generally the value of an Alias property
		-->
      <xsl:param name="alias" />

		<!--
			Actual value of a name field
		-->
		<xsl:param name="name" />

      <!--
			If the alias field is set, use that instead of the actual name field
		-->

		<xsl:choose>
			<xsl:when test="xsltHelper:getProjectAliased($curNode)" >
				<!-- when alias is on, use the alias value -->
		            <xsl:choose>
	       	        <xsl:when test="string-length( $alias )" >
	       	        	<!-- if there is a value in alias, use it -->
						<xsl:value-of select="string(uriHelper:translateString( string( $alias )))"/>
		               </xsl:when>
		               <xsl:otherwise>
	       	        	<!-- if there is a no value in alias, use the name -->
					<xsl:value-of select="string( uriHelper:translateString( string( $name )))"/>
		               </xsl:otherwise>
	       	     </xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- when alias is not on -->
				<xsl:value-of select="string( uriHelper:translateString( string( $name )))"/>
			</xsl:otherwise>
		</xsl:choose>

   </xsl:template>

</xsl:stylesheet>
