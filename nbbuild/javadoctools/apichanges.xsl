<?xml version="1.0" encoding="UTF-8"?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
Microsystems, Inc. All Rights Reserved.
-->

<!--
This DOCTYPE assumes that the pseudo-DTD given at the end of the
XSLT specification is present in the named location. It cannot be
committed to CVS for legal reasons. You need to download it:
<!DOCTYPE xsl:stylesheet [
<!ENTITY % result-elements "
| a | abbr | acronym | address | area
| b | base | bdo | big | blockquote
| body | br | button | caption | cite
| code | col | colgroup | dd | del
| dfn | div | dl | dt | em
| fieldset | form | h1 | h2 | h3
| h4 | h5 | h6 | head | hr
| html | i | img | input | ins
| kbd | label | legend | li | link
| map | meta | noscript | object | ol
| optgroup | option | p | param | pre
| q | samp | script | select | small
| span | strong | style | sub | sup
| table | tbody | td | textarea | tfoot
| th | thead | title | tr | tt
| ul | var
">
<!ENTITY % xsl-struct SYSTEM "xsl.dtd">
%xsl-struct;
]>
-->

<!-- TODO:
- in details of a change, list all branches it applies to
- create separate lists for all changes between one branch and the next
- clearly mark incompatible bits in details of a change, besides text contents
- warn about any changes made after a certain date which incl. additions but
  have no associated API version
- ability to display authors in pretty format
- kill $api-list and select from /apichanges/apidefs/apidef instead to get an
  API list (but the hack is still needed for index by affected class)
- whenever <hN> tags are used as part of a changelist template, the proper
  header level should be computed based on the surrounding context
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- Overridable parameters: -->
    <xsl:param name="issue-url-base" select="'http://www.netbeans.org/issues/show_bug.cgi?id='"/>
    <xsl:param name="javadoc-url-base" select="'http://www.netbeans.org/download/dev/javadoc/OpenAPIs'"/>

    <!-- Main document structure: -->
    <xsl:template match="/">
        <html>
            <xsl:apply-templates select="apichanges/htmlcontents/node()"/>
        </html>
    </xsl:template>

    <!-- Summarizing links to changes: -->
    <xsl:template match="change" mode="summary">
        <li>
            <xsl:if test="date">(<xsl:apply-templates select="date"/>)<xsl:text> </xsl:text></xsl:if>
            <a><xsl:attribute name="href">#<xsl:call-template name="change-id"/></xsl:attribute><xsl:apply-templates select="summary/node()"/></a>
        </li>
    </xsl:template>

    <!-- Showing API version: -->
    <xsl:template match="change" mode="summary-show-version">
        <li>
            (<xsl:apply-templates select="version"/>)<xsl:text> </xsl:text>
            <a><xsl:attribute name="href">#<xsl:call-template name="change-id"/></xsl:attribute><xsl:apply-templates select="summary/node()"/></a>
        </li>
    </xsl:template>

    <!-- Hack to get XSLT to group things automatically: -->
    <xsl:variable name="all-apis">
        <xsl:for-each select="/apichanges/changes/change[api]">
            <xsl:sort data-type="text" order="ascending" select="api/@name"/>
            <xsl:value-of select="position()"/><xsl:text>!</xsl:text><xsl:value-of select="api/@name"/><xsl:text>!</xsl:text>
        </xsl:for-each>
    </xsl:variable>

    <!-- Summarizing links to changes; show API headers: -->
    <!-- (currently unused) -->
    <xsl:template match="change" mode="summary-group-api">
        <!--
        Tricky. We list all of the changes as usual, sorted by API and date.
        We have already computed what all of the API names are (incl. duplicates)
        in order and have this indexed by position in the list. To tell if it
        is necessary to start a new group we calculate whether the API of this
        change is different from that of the preceding one (and use the same trick
        to tell if the bullet needs to be closed). XSLT does not seem to have facilities
        to manipulate doc fragments sanely (i.e. use of for-each or array indexing) so
        we have to keep the info in a string and use string functions to do it, alas.
        -->
        <xsl:variable name="this-api" select="api/@name"/>
        <xsl:variable name="prev-api" select="substring-after(substring-before($all-apis, concat('!', position(), '!')), concat(position() - 1, '!'))"/>
        <xsl:variable name="next-api" select="substring-before(substring-after($all-apis, concat('!', position() + 1, '!')), '!')"/>
        <xsl:variable name="is-first" select="position() = 1 or $prev-api != $this-api"/>
        <xsl:variable name="is-last" select="position() = last() or $this-api != $next-api"/>
        <xsl:if test="$is-first">
            <h2>
                <xsl:apply-templates select="api"/>
            </h2>
            <!-- Need to use the slinky disable-output-escaping because we do not close the ul here: -->
            <xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
        </xsl:if>
        <xsl:apply-templates select="." mode="summary"/>
        <xsl:if test="$is-last">
            <xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="change" mode="details-group-api">
        <xsl:variable name="this-api" select="api/@name"/>
        <xsl:variable name="prev-api" select="substring-after(substring-before($all-apis, concat('!', position(), '!')), concat(position() - 1, '!'))"/>
        <!--<xsl:variable name="next-api" select="substring-before(substring-after($all-apis, concat('!', position() + 1, '!')), '!')"/>-->
        <xsl:variable name="is-first" select="position() = 1 or $prev-api != $this-api"/>
        <!--<xsl:variable name="is-last" select="position() = last() or $this-api != $next-api"/>-->
        <xsl:if test="$is-first">
            <hr style="width:50%"/>
            <h2>
                <a>
                    <xsl:attribute name="name"><xsl:value-of select="$this-api"/></xsl:attribute>
                    <xsl:apply-templates select="api"/>
                </a>
            </h2>
        </xsl:if>
        <xsl:apply-templates select="." mode="details"/>
    </xsl:template>

    <!-- Similar stuff but by class changed: -->
    <xsl:variable name="all-classes">
        <xsl:for-each select="/apichanges/changes/change/class">
            <!--<xsl:sort data-type="text" order="ascending" select="@package"/>-->
            <xsl:sort data-type="text" order="ascending" select="@name"/>
            <xsl:value-of select="position()"/><xsl:text>!</xsl:text><xsl:value-of select="@package"/>.<xsl:value-of select="@name"/><xsl:text>!</xsl:text>
        </xsl:for-each>
    </xsl:variable>

    <xsl:template match="change/class" mode="summary-group-class">
        <xsl:variable name="this-class" select="concat(@package, '.', @name)"/>
        <xsl:variable name="prev-class" select="substring-after(substring-before($all-classes, concat('!', position(), '!')), concat(position() - 1, '!'))"/>
        <xsl:variable name="next-class" select="substring-before(substring-after($all-classes, concat('!', position() + 1, '!')), '!')"/>
        <xsl:variable name="is-first" select="position() = 1 or $prev-class != $this-class"/>
        <xsl:variable name="is-last" select="position() = last() or $this-class != $next-class"/>
        <xsl:if test="$is-first">
            <xsl:if test="contains(@name, '.') or contains(@name, '$')">
                <xsl:message terminate="yes">No inner classes permitted in &lt;class/&gt;: <xsl:value-of select="@name"/></xsl:message>
            </xsl:if>
            <h2>
                <a><xsl:attribute name="name"><xsl:value-of select="$this-class"/></xsl:attribute>
                    <code><span style="color:gray"><xsl:value-of select="@package"/>.</span><xsl:value-of select="@name"/></code>
                </a>
            </h2>
            <xsl:text disable-output-escaping="yes">&lt;ul&gt;</xsl:text>
        </xsl:if>
        <xsl:apply-templates select=".." mode="summary"/>
        <xsl:if test="$is-last">
            <xsl:text disable-output-escaping="yes">&lt;/ul&gt;</xsl:text>
        </xsl:if>
    </xsl:template>

    <!-- The details of a change: -->
    <xsl:template match="change" mode="details">
        <xsl:comment> AUTOMATICALLY GENERATED - DO NOT EDIT ME! </xsl:comment>
        <h3><a><xsl:attribute name="name"><xsl:call-template name="change-id"/></xsl:attribute><xsl:apply-templates select="summary/node()"/></a></h3>
        <em>
            <xsl:choose>
                <xsl:when test="date">
                    <xsl:apply-templates select="date"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>(date unknown)</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="version">
                <xsl:text>; API spec. version: </xsl:text>
                <xsl:apply-templates select="version"/>
            </xsl:if>
            <xsl:if test="class">
                <xsl:text>; affected top-level classes:</xsl:text>
                <xsl:for-each select="class">
                    <xsl:text> </xsl:text>
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="package">
                <xsl:text>; affected packages:</xsl:text>
                <xsl:for-each select="package">
                    <xsl:text> </xsl:text>
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="author">
                <xsl:text>; made by:</xsl:text>
                <xsl:for-each select="author">
                    <xsl:text> </xsl:text>
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="issue">
                <xsl:text>; issues:</xsl:text>
                <xsl:for-each select="issue">
                    <xsl:text> </xsl:text>
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </xsl:if>
        </em>
        <br/>
        <xsl:apply-templates select="description/node()"/>
        <xsl:if test="compatibility/node()">
            <br/><strong>Compatibility: </strong> <xsl:apply-templates select="compatibility/node()"/>
        </xsl:if>
        <xsl:if test="compatibility/@source = 'incompatible' and compatibility/@binary = 'compatible'">
            <br/><em><a href="http://openide.netbeans.org/versioning-policy.html#bincompat">Binary-compatible</a></em>
        </xsl:if>
    </xsl:template>

    <!-- Link to one API group: -->
    <xsl:template match="api" mode="api-summary">
        <xsl:variable name="this-api" select="@name"/>
        <xsl:variable name="prev-api" select="substring-after(substring-before($all-apis, concat('!', position(), '!')), concat(position() - 1, '!'))"/>
        <xsl:variable name="is-first" select="position() = 1 or $prev-api != $this-api"/>
        <xsl:if test="$is-first">
            <li>
                <a>
                    <xsl:attribute name="href">#<xsl:value-of select="$this-api"/></xsl:attribute>
                    <xsl:apply-templates select="."/>
                </a>
            </li>
        </xsl:if>
    </xsl:template>

    <!-- Show various lists: -->

    <xsl:template match="changelist[@style = 'list-all-apis']">
        <xsl:call-template name="changelist-list-all-apis"/>
    </xsl:template>
    <xsl:template name="changelist-list-all-apis">
        <ul>
            <xsl:apply-templates select="/apichanges/changes/change/api" mode="api-summary">
                <xsl:sort data-type="text" order="ascending" select="@name"/>
            </xsl:apply-templates>
            <xsl:if test="/apichanges/changes/change[count(api) = 0]">
                <li><a href="#uncategorized-api">Uncategorized changes</a></li>
            </xsl:if>
        </ul>
    </xsl:template>

    <xsl:template match="changelist[@style = 'incompat-by-date']">
        <xsl:call-template name="changelist-incompat-by-date"/>
    </xsl:template>
    <xsl:template name="changelist-incompat-by-date">
        <ul>
            <xsl:apply-templates select="/apichanges/changes/change[compatibility/@binary='incompatible' or compatibility/@source='incompatible' or compatibility/@semantic='incompatible']" mode="summary">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="changelist[@style = 'compat-by-date']">
        <ul>
            <xsl:apply-templates select="/apichanges/changes/change[compatibility/@binary='compatible' and compatibility/@source='compatible' and compatibility/@semantic='compatible']" mode="summary">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="changelist[@style = 'all-by-date']">
        <xsl:call-template name="changelist-all-by-date"/>
    </xsl:template>
    <xsl:template name="changelist-all-by-date">
        <ul>
            <xsl:apply-templates select="/apichanges/changes/change" mode="summary">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="changelist[@style = 'all-by-version']">
        <xsl:call-template name="changelist-all-by-version"/>
    </xsl:template>
    <xsl:template name="changelist-all-by-version">
        <ul>
            <xsl:apply-templates select="/apichanges/changes/change[version]" mode="summary-show-version">
                <xsl:sort data-type="number" order="descending" select="version/@major"/>
                <xsl:sort data-type="number" order="descending" select="version/@minor"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="changelist[@style = 'all-by-api']">
        <xsl:apply-templates select="/apichanges/changes/change[api]" mode="summary-group-api">
            <xsl:sort data-type="text" order="ascending" select="api/@name"/>
            <xsl:sort data-type="number" order="descending" select="date/@year"/>
            <xsl:sort data-type="number" order="descending" select="date/@month"/>
            <xsl:sort data-type="number" order="descending" select="date/@day"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="changelist[@style = 'all-by-class']">
        <xsl:call-template name="changelist-all-by-class"/>
    </xsl:template>
    <xsl:template name="changelist-all-by-class">
        <xsl:apply-templates select="/apichanges/changes/change/class" mode="summary-group-class">
            <xsl:sort data-type="text" order="ascending" select="@name"/>
            <xsl:sort data-type="number" order="descending" select="../date/@year"/>
            <xsl:sort data-type="number" order="descending" select="../date/@month"/>
            <xsl:sort data-type="number" order="descending" select="../date/@day"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="changelist[@style = 'details-by-date']">
        <xsl:apply-templates select="/apichanges/changes/change" mode="details">
            <xsl:sort data-type="number" order="descending" select="date/@year"/>
            <xsl:sort data-type="number" order="descending" select="date/@month"/>
            <xsl:sort data-type="number" order="descending" select="date/@day"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="changelist[@style = 'details-by-api']">
        <xsl:call-template name="changelist-details-by-api"/>
    </xsl:template>
    <xsl:template name="changelist-details-by-api">
        <xsl:apply-templates select="/apichanges/changes/change[api]" mode="details-group-api">
            <xsl:sort data-type="text" order="ascending" select="api/@name"/>
            <xsl:sort data-type="number" order="descending" select="date/@year"/>
            <xsl:sort data-type="number" order="descending" select="date/@month"/>
            <xsl:sort data-type="number" order="descending" select="date/@day"/>
        </xsl:apply-templates>
        <xsl:if test="/apichanges/changes/change[count(api) = 0]">
            <hr style="width:50%"/><h2><a name="uncategorized-api">Uncategorized changes</a></h2>
            <xsl:apply-templates select="/apichanges/changes/change[count(api) = 0]" mode="details">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- Fallback: -->
    <xsl:template match="changelist" priority="-0.1">
        <xsl:message terminate="yes">
            Unrecognized changelist style: <xsl:value-of select="@style"/>
        </xsl:message>
    </xsl:template>
    
    <!-- Show all change lists usually needed: -->
    
    <xsl:template match="standard-changelists">
      <h1><a name="list-all-apis">Index of APIs</a></h1>
      <xsl:call-template name="changelist-list-all-apis"/>

      <h1><a name="incompat-by-date">Incompatible changes by date</a></h1>
      <p>Fuller descriptions of all changes can be found below (follow links).</p>
      <p>Not all deprecations are listed here, assuming that the deprecated
        APIs continue to essentially work. For a full deprecation list, please
        consult the
        <a href="deprecated-list.html">Javadoc</a>.</p>
      <xsl:call-template name="changelist-incompat-by-date"/>

      <h1><a name="all-by-date">All changes by date</a></h1>
      <xsl:call-template name="changelist-all-by-date"/>

      <h1><a name="all-by-version">Changes by version</a></h1>
      <p>
        These API specification versions may be used to indicate that a module
        requires a certain API feature in order to function. For example, if you
        see here a feature you need which is labelled <samp>1.20</samp>, your
        manifest should contain in its main attributes the line:
      </p>
      <xsl:choose>
          <xsl:when test="starts-with(@module-code-name, 'IDE/')">
              <pre>OpenIDE-Module-IDE-Dependencies: <xsl:value-of select="@module-code-name"/> &gt; 1.20</pre>
          </xsl:when>
          <xsl:otherwise>
              <pre>OpenIDE-Module-Module-Dependencies: <xsl:value-of select="@module-code-name"/> &gt; 1.20</pre>
          </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="changelist-all-by-version"/>

      <h1><a name="all-by-class">Changes by affected class</a></h1>
      <xsl:call-template name="changelist-all-by-class"/>

      <hr/><h1><a name="details-by-api">Details of all changes by API and date</a></h1>
      <xsl:call-template name="changelist-details-by-api"/>
    </xsl:template>

    <!-- Format dates readably: -->
    <xsl:template match="date">
        <xsl:choose>
            <xsl:when test="@month=1">Jan</xsl:when>
            <xsl:when test="@month=2">Feb</xsl:when>
            <xsl:when test="@month=3">Mar</xsl:when>
            <xsl:when test="@month=4">Apr</xsl:when>
            <xsl:when test="@month=5">May</xsl:when>
            <xsl:when test="@month=6">Jun</xsl:when>
            <xsl:when test="@month=7">Jul</xsl:when>
            <xsl:when test="@month=8">Aug</xsl:when>
            <xsl:when test="@month=9">Sep</xsl:when>
            <xsl:when test="@month=10">Oct</xsl:when>
            <xsl:when test="@month=11">Nov</xsl:when>
            <xsl:when test="@month=12">Dec</xsl:when>
        </xsl:choose><xsl:text> </xsl:text>
        <xsl:value-of select="@day"/> '<xsl:value-of select="substring(@year, 3, 2)"/>
    </xsl:template>

    <xsl:template match="version">
        <xsl:value-of select="@major"/>.<xsl:value-of select="@minor"/>
        <xsl:if test="@subminor">.<xsl:value-of select="@subminor"/></xsl:if>
        <xsl:if test="@subsubminor">.<xsl:value-of select="@subsubminor"/></xsl:if>
    </xsl:template>

    <xsl:template match="api">
        <xsl:variable name="apiname" select="@name"/>
        <xsl:variable name="def" select="/apichanges/apidefs/apidef[@name = $apiname]"/>
        <xsl:if test="count($def) = 0 or count($def) > 1">
            <xsl:message terminate="yes">Unknown API: <xsl:value-of select="$apiname"/> (in #<xsl:call-template name="change-id"><xsl:with-param name="node" select=".."/></xsl:call-template>)</xsl:message>
        </xsl:if>
        <xsl:apply-templates select="$def/node()"/>
    </xsl:template>

    <xsl:template match="class">
        <xsl:if test="@package = '' or @name = ''">
            <xsl:message terminate="yes">
                Classes must have both package and name specified: <xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>
            </xsl:message>
        </xsl:if>
        <a>
            <!-- XXX these links are often broken... how to generate them only when valid?? -->
            <xsl:attribute name="href"><xsl:value-of select="$javadoc-url-base"/>/<xsl:value-of select="translate(@package, '.', '/')"/>/<xsl:value-of select="@name"/>.html</xsl:attribute>
            <code><!--<xsl:value-of select="@package"/>.--><xsl:value-of select="@name"/></code>
        </a>
    </xsl:template>

    <xsl:template match="package">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$javadoc-url-base"/>/<xsl:value-of select="translate(@name, '.', '/')"/>/package-summary.html</xsl:attribute>
            <code><xsl:value-of select="@name"/></code>
        </a>
    </xsl:template>

    <xsl:template match="author">
        <xsl:value-of select="@login"/>
    </xsl:template>

    <xsl:template match="issue">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$issue-url-base"/><xsl:value-of select="@number"/></xsl:attribute>
            #<xsl:value-of select="@number"/>
        </a>
    </xsl:template>

    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Get or create a unique ID for a change node: -->
    <xsl:template name="change-id">
        <xsl:param name="node" select="."/>
        <xsl:choose>
            <xsl:when test="$node/@id"><xsl:value-of select="$node/@id"/></xsl:when>
            <xsl:when test="count($node/issue) = 1">
                <xsl:variable name="issue" select="$node/issue"/>
                <xsl:variable name="number" select="$issue/@number"/>
                <xsl:variable name="dupes" select="$node/../change/issue[@number = $number and generate-id() != generate-id($issue) and not(../@id)]"/>
                <xsl:variable name="genid">issue-<xsl:value-of select="$number"/></xsl:variable>
                <xsl:if test="$dupes">
                    <xsl:message terminate="yes">
                        "<xsl:value-of select="$genid"/>" cannot be used as the generated ID for change
                        "<xsl:value-of select="normalize-space($node/summary)"/>"
                        because it is not unique; shared also with
                        <xsl:for-each select="$dupes/../summary"><xsl:if test="position() &gt; 1"> and
                            </xsl:if>"<xsl:value-of select="normalize-space()"/>"</xsl:for-each>.
                        Please give an explicit "id" attribute for this change element.
                    </xsl:message>
                </xsl:if>
                <!-- XXX could also check that the generated ID does not clash with id attrs, but this is much less likely -->
                <xsl:value-of select="$genid"/>
            </xsl:when>
            <xsl:when test="$node/version">
                <xsl:variable name="version" select="$node/version"/>
                <xsl:variable name="dupes" select="//change/version[generate-id() != generate-id($version) and @major = $version/@major and @minor = $version/@minor and (@subminor = $version/@subminor or not(@subminor) and not($version/@subminor)) and (@subsubminor = $version/@subsubminor or not(@subsubminor) and not ($version/@subsubminor)) and not(../@id)]"/>
                <xsl:variable name="genid">version-<xsl:apply-templates select="$version"/></xsl:variable>
                <xsl:if test="$dupes">
                    <xsl:message terminate="yes">
                        "<xsl:value-of select="$genid"/>" cannot be used as the generated ID for change
                        "<xsl:value-of select="normalize-space($node/summary)"/>"
                        because it is not unique; shared also with
                        <xsl:for-each select="$dupes/../summary"><xsl:if test="position() &gt; 1"> and
                            </xsl:if>"<xsl:value-of select="normalize-space()"/>"</xsl:for-each>.
                        Please give an explicit "id" attribute for this change element.
                    </xsl:message>
                </xsl:if>
                <xsl:value-of select="$genid"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="generate-id($node)"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
