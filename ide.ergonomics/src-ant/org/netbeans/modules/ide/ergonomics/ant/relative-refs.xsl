<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:param name="cluster.name"/>

    <xsl:template match="/">
        <xsl:apply-templates mode="project-wizard"/>
    </xsl:template>

    <xsl:template match="filesystem" mode="project-wizard">
        <xsl:element name="filesystem">
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="file" mode="project-wizard">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="folder" mode="project-wizard">
        <xsl:element name="folder">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='instantiatingIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">mine</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='SystemFileSystem.localizingBundle']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">SystemFileSystem.localizingBundle</xsl:attribute>
            <xsl:attribute name="stringvalue">org.netbeans.modules.ide.ergonomics.<xsl:value-of select="$cluster.name"/>.Bundle</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@urlvalue]" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="urlvalue">
                <xsl:text>nbresloc:/org/netbeans/modules/ide/ergonomics/</xsl:text>
                <xsl:value-of select="$cluster.name"/>
                <xsl:text>/</xsl:text>
                <xsl:call-template name="filename">
                    <xsl:with-param name="text" select="@urlvalue"/>
                </xsl:call-template>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="project-wizard">
        <xsl:copy-of select="."/>
    </xsl:template>


    <!-- utility to generate just file name after last slash -->
    <xsl:template name="filename">
        <xsl:param name="text"/>
        <xsl:variable name="after" select="substring-after($text,'/')"/>
        <xsl:choose>
            <xsl:when test="$after">
                <xsl:call-template name="filename">
                    <xsl:with-param name="text" select="$after"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
