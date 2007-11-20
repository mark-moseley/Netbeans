<?xml version="1.0" ?>
<xsl:stylesheet id="NamedElementFormat" version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:UML="omg.org/UML/1.4"> 
   <xsl:import href="AliasFormat.xsl"/>
   <xsl:output method="text" />

   <xsl:template match="*" >   
   
	   <!-- 
		   Name/Alias processing 
	   -->
	   <xsl:call-template name="CheckForAlias">
		   <xsl:with-param name="curNode" select="." />
		   <xsl:with-param name="alias" select="@alias"/>
		   <xsl:with-param name="name" select="@name"/>
	   </xsl:call-template>
	
	</xsl:template>
	            
</xsl:stylesheet>

  