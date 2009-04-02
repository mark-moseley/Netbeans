<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="cluster.name"/>

    <xsl:template match="filesystem/folder[@name='Templates']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Templates</xsl:attribute>
            <xsl:apply-templates mode="project-wizard"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='MIMEResolver']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Services</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">MIMEResolver</xsl:attribute>
                <xsl:apply-templates mode="mime-resolvers"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Debugger']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Debugger</xsl:attribute>
                <xsl:apply-templates mode="attach-types"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Servers']/folder[@name='Actions']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Servers</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">Actions</xsl:attribute>
                <xsl:apply-templates mode="actions"/>
            </xsl:element>
        </xsl:element>
        <xsl:call-template 
            name="actions-definition"
        >
            <xsl:with-param
                name="originalFile"
                select="file/attr[@name='originalFile']"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Services']/folder[@name='AntBasedProjectTypes']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Ergonomics</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name">AntBasedProjectTypes</xsl:attribute>
                <xsl:apply-templates mode="project-types"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="filesystem/folder[@name='Loaders']/folder/folder/folder[@name='Factories']">
        <xsl:element name="folder">
            <xsl:attribute name="name">Loaders</xsl:attribute>
            <xsl:element name="folder">
                <xsl:attribute name="name"><xsl:value-of select="../../@name"/></xsl:attribute>
                <xsl:element name="folder">
                    <xsl:attribute name="name"><xsl:value-of select="../@name"/></xsl:attribute>
                    <xsl:element name="folder">
                        <xsl:attribute name="name">Factories</xsl:attribute>
                        <xsl:element name="file">
                            <xsl:attribute name="name">Ergonomics.instance</xsl:attribute>
                            <xsl:element name="attr">
                                <xsl:attribute name="name">instanceCreate</xsl:attribute>
                                <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FodDataObjectFactory.create</xsl:attribute>
                            </xsl:element>
                            <xsl:element name="attr">
                                <xsl:attribute name="name">position</xsl:attribute>
                                <xsl:attribute name="intvalue">999999</xsl:attribute>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <!-- project wizard -->
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
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.api.Factory.newProject</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='templateWizardIterator']" mode="project-wizard">
        <xsl:element name="attr">
            <xsl:attribute name="name">instantiatingIterator</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.api.Factory.newProject</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="project-wizard">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- mime-resolvers -->
    <xsl:template match="file" mode="mime-resolvers">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:if test="@url">
                <xsl:attribute name="url"><xsl:value-of select="@url"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="mime-resolvers"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr" mode="mime-resolvers">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!-- attach type -->
    <xsl:template match="file" mode="attach-types">
        <xsl:if test="attr[@stringvalue='org.netbeans.spi.debugger.ui.AttachType']">
            <xsl:element name="file">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:if test="@url">
                    <xsl:attribute name="url"><xsl:value-of select="@url"/></xsl:attribute>
                </xsl:if>
                <xsl:apply-templates mode="attach-types"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="attr[@name='instanceCreate']" mode="attach-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceCreate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.debugger.AttachTypeProxy.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="attach-types">
        <xsl:copy-of select="."/>
    </xsl:template>


    <!-- actions -->
    <xsl:template match="file" mode="actions">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:if test="@url">
                <xsl:attribute name="url"><xsl:value-of select="@url"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="actions"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='delegate']" mode="actions">
        <xsl:element name="attr">
            <xsl:attribute name="name">delegate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureAction.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="actions">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="actions-definition">
        <xsl:param name="originalFile"/>
        <xsl:call-template name="actions-definition-impl">
            <xsl:with-param name="path" select="$originalFile/@stringvalue"/>
            <xsl:with-param name="query" select="'filesystem'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="actions-definition-impl">
        <xsl:param name="path"/>
        <xsl:param name="query"/>
        <xsl:variable name="category" select="substring-before($path,'/')"/>
        <xsl:choose>
            <xsl:when test="$category">
                <xsl:element name="folder">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$category"/>
                    </xsl:attribute>
                    <xsl:call-template name="actions-definition-impl">
                        <xsl:with-param name="path" select="substring-after($path,'/')"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates
                    select="//filesystem/folder[@name='Actions']/descendant::file[@name=$path]"
                    mode="actions"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- project type -->
    <xsl:template match="file" mode="project-types">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:if test="@url">
                <xsl:attribute name="url"><xsl:value-of select="@url"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="project-types"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='instanceCreate']" mode="project-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceCreate</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.modules.ide.ergonomics.fod.FeatureProjectFactory.create</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr[@name='instanceClass']" mode="project-types">
        <xsl:element name="attr">
            <xsl:attribute name="name">instanceClass</xsl:attribute>
            <xsl:attribute name="methodvalue">org.netbeans.spi.project.ProjectFactory</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attr" mode="project-types">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
