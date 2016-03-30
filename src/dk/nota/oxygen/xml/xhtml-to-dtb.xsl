<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns=""
    exclude-result-prefixes="dc epub nota opf xhtml xs" version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="OPF_DOCUMENT" as="document-node()*"
        select="document(replace(document-uri(/), '/[^/]+$', '/package.opf'))"/>
    <xsl:variable name="INLINE_ELEMENT_NAMES" as="xs:string+"
        select="('b', 'big', 'i', 'small', 'tt', 'abbr', 'acronym', 'cite',
                'code', 'dfn', 'em', 'kbd', 'strong', 'samp', 'time', 'var',
                'a', 'bdo', 'br', 'img', 'map', 'object', 'q', 'script', 'span',
                'sub', 'sup', 'button', 'input', 'label', 'select', 'textarea')"/>
    <xsl:variable name="LANGUAGE" as="xs:string*"
        select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:variable name="TITLE" as="xs:string*"
        select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:title/text()"/>
    <xsl:template match="/">
        <xsl:message>
            <nota:out>CONVERTING TO DTBOOK...</nota:out>
        </xsl:message>
        <xsl:variable name="frontmatter" as="node()*">
            <xsl:apply-templates
                select="//xhtml:html/xhtml:body/(xhtml:section
                        [nota:has-epub-types(., 'cover')]|xhtml:section
                        [nota:has-epub-types(., 'frontmatter')])"/>
        </xsl:variable>
        <xsl:variable name="bodymatter" as="node()*">
            <xsl:apply-templates
                select="//xhtml:html/xhtml:body/xhtml:section
                        [nota:has-epub-types(., 'bodymatter')]"/>
        </xsl:variable>
        <xsl:variable name="rearmatter" as="node()*">
            <xsl:apply-templates
                select="//xhtml:html/xhtml:body/xhtml:section
                        [nota:has-epub-types(., 'backmatter')]"/>
        </xsl:variable>
        <dtbook version="1.1.0">
            <head>
                <title>
                    <xsl:value-of select="$TITLE"/>
                </title>
                <meta name="prod:AutoBrailleReady" content="yes"/>
                <xsl:apply-templates
                    select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:*"/>
            </head>
            <book lang="{$LANGUAGE}">
                <frontmatter>
                    <doctitle>
                        <xsl:value-of select="$TITLE"/>
                    </doctitle>
                    <xsl:copy-of select="$frontmatter"/>
                </frontmatter>
                <bodymatter>
                    <xsl:copy-of select="$bodymatter"/>
                </bodymatter>
                <xsl:if test="$rearmatter">
                    <rearmatter>
                        <xsl:copy-of select="$rearmatter"/>
                    </rearmatter>
                </xsl:if>
            </book>
        </dtbook>
    </xsl:template>
    <xsl:template match="node()">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="text()" priority="1">
        <xsl:copy/>
    </xsl:template>
    <!-- OPF -->
    <!-- Convert metadata elements -->
    <xsl:template match="dc:*">
        <meta name="{name()}" content="{text()}"/>
    </xsl:template>
    <xsl:template match="dc:source">
        <meta name="{name()}" content="{replace(text(), '^urn:isbn:|-', '')}"/>
    </xsl:template>
    <!-- XHTML -->
    <!-- Named templates for attributes -->
    <xsl:template name="ATTRIBUTES.GENERIC">
        <xsl:copy-of select="@id|@lang"/>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.GENERIC.WITH_CLASS">
        <xsl:call-template name="ATTRIBUTES.GENERIC"/>
        <xsl:copy-of select="@class"/>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.IMAGE">
        <xsl:call-template name="ATTRIBUTES.GENERIC"/>
        <xsl:copy-of select="@alt|@height|@width"/>
        <xsl:attribute name="src" select="nota:get-file-name-from-path(@src)"/>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.LANGUAGE.PARENT">
        <xsl:copy-of select="parent::*/@lang"/>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.TABLE.CELL">
        <xsl:call-template name="ATTRIBUTES.GENERIC"/>
        <xsl:copy-of select="@colspan|@rowspan"/>
    </xsl:template>
    <!-- Named template for copied element with generic attributes -->
    <xsl:template name="ELEMENT.COPY.GENERIC">
        <xsl:element name="{local-name()}">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <!-- Named template for page numbers -->
    <xsl:template name="ELEMENT.LIST_ITEM.PAGENUM.AFTER">
        <xsl:for-each
            select="descendant::xhtml:*[nota:is-page-break(.)]
                    [nota:ends-list-item(.)] except descendant::xhtml:li/
                    descendant::node()">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="ELEMENT.LIST_ITEM.PAGENUM.BEFORE">
        <xsl:for-each
            select="descendant::xhtml:*[nota:is-page-break(.)]
                    [nota:starts-list-item(.)] except descendant::xhtml:li/
                    descendant::node()">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="ELEMENT.PAGENUM">
        <pagenum>
            <xsl:attribute name="id" select="@id"/>
            <xsl:attribute name="page" select="replace(@class, '^page-', '')"/>
            <xsl:value-of select="@title"/>
        </pagenum>
    </xsl:template>
    <!-- Named template for table captions -->
    <xsl:template name="ELEMENT.TABLE.CAPTION">
        <xsl:if test="xhtml:caption">
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"
                select="xhtml:caption/node()">
                <xsl:with-param name="parentElementDissolved" as="xs:boolean"
                    select="true()"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    <!-- Generic template for XHTML elements -->
    <xsl:template match="xhtml:*">
        <xsl:call-template name="ELEMENT.COPY.GENERIC"/>
    </xsl:template>
    <!-- Special mode for grouping inline content in paragraphs -->
    <xsl:template mode="GROUP_INLINE_CONTENT"
        match="node()[nota:is-inline(.)]">
        <xsl:param name="parentElementDissolved" as="xs:boolean*"/>
        <xsl:choose>
            <xsl:when test="preceding-sibling::node()[1][nota:is-inline(.)]"/>
            <xsl:otherwise>
                <xsl:variable name="group" as="node()*"
                    select="self::node()|following-sibling::node() except
                            following-sibling::node()[not(nota:is-inline(.))]
                            [1]/(self::node()|following-sibling::node())"/>
                <xsl:if test="$group[normalize-space() ne '']">
                    <p>
                        <xsl:if test="$parentElementDissolved">
                            <xsl:call-template
                                name="ATTRIBUTES.LANGUAGE.PARENT"/>
                        </xsl:if>
                        <xsl:apply-templates select="$group"/>
                    </p>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="GROUP_INLINE_CONTENT"
        match="node()[not(nota:is-inline(.))]">
        <xsl:param name="parentElementDissolved" as="xs:boolean*"/>
        <xsl:choose>
            <xsl:when test="$parentElementDissolved">
                <xsl:variable name="element" as="node()">
                    <xsl:apply-templates select="."/>
                </xsl:variable>
                <xsl:element name="{$element/local-name()}">
                    <xsl:copy-of select="$element/@*"/>
                    <xsl:call-template name="ATTRIBUTES.LANGUAGE.PARENT"/>
                    <xsl:copy-of select="$element/node()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- A -->
    <xsl:template  match="xhtml:a">
        <xsl:variable name="isExternal" as="xs:boolean"
            select="matches(@href, '^[a-z]+:')"/>
        <xsl:choose>
            <xsl:when test="$isExternal">
                <a>
                    <xsl:call-template name="ATTRIBUTES.GENERIC"/>
                    <xsl:copy-of select="@href"/>
                    <xsl:apply-templates/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template
        match="xhtml:a[nota:has-classes(., 'noteref')]">
        <noteref idref="{replace(@href, '^.*?#', '')}">
        	<xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </noteref>
    </xsl:template>
    <!-- ASIDE -->
    <xsl:template match="xhtml:aside">
        <sidebar>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </sidebar>
    </xsl:template>
    <xsl:template match="xhtml:figure/xhtml:aside">
        <prodnote class="imgprodnote" render="required">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </prodnote>
    </xsl:template>
    <!-- BLOCKQUOTE -->
    <xsl:template match="xhtml:blockquote">
        <div class="blockquote">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <!-- EM and STRONG -->
    <xsl:template match="xhtml:em|xhtml:strong">
        <xsl:param name="discardEmStrong" as="xs:boolean?" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="$discardEmStrong">
                <xsl:apply-templates>
                    <xsl:with-param name="discardEmStrong" as="xs:boolean"
                        tunnel="yes" select="$discardEmStrong"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="ELEMENT.COPY.GENERIC"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- FIGCAPTION -->
    <xsl:template match="xhtml:figcaption">
        <prodnote class="caption">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </prodnote>
    </xsl:template>
    <!-- FIGURE -->
    <xsl:template match="xhtml:figure">
        <imggroup>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:choose>
                <xsl:when test="xhtml:figure">
                    <xsl:apply-templates select="xhtml:figure/xhtml:*"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="xhtml:img"/>
                    <xsl:apply-templates select="xhtml:figcaption"/>
                    <xsl:apply-templates select="xhtml:aside"/>
                </xsl:otherwise>
            </xsl:choose>
        </imggroup>
    </xsl:template>
    <xsl:template match="xhtml:figure[nota:has-classes(., 'sidebar')]">
        <sidebar>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </sidebar>
    </xsl:template>
    <!-- H1, H2, H3 etc. -->
    <xsl:template match="xhtml:*[matches(local-name(), '^h\d$')]">
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor::xhtml:section)"/>
        <xsl:variable name="demote" as="xs:boolean"
            select="$depth > 6 or parent::xhtml:aside or parent::xhtml:section
                    [nota:is-poem(.)]"/>
        <xsl:choose>
            <xsl:when test="$demote">
                <p class="bridgehead">
                    <xsl:call-template name="ATTRIBUTES.GENERIC"/>
                    <xsl:apply-templates/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <levelhd>
                    <xsl:call-template name="ATTRIBUTES.GENERIC"/>
                    <xsl:attribute name="depth" select="$depth"/>
                    <xsl:apply-templates/>
                </levelhd>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xhtml:*[matches(local-name(), '^h\d$')]//xhtml:span">
        <xsl:apply-templates/>
    </xsl:template>
    <!-- HR -->
    <xsl:template match="xhtml:hr"/>
    <!-- IMG -->
    <xsl:template match="xhtml:img">
        <img>
            <xsl:call-template name="ATTRIBUTES.IMAGE"/>
        </img>
        <xsl:message>
            <nota:image>
                <xsl:value-of select="@src"/>
            </nota:image>
        </xsl:message>
    </xsl:template>
    <!-- LI -->
    <xsl:template match="xhtml:li">
        <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.BEFORE"/>
        <li>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </li>
        <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.AFTER"/>
    </xsl:template>
    <xsl:template mode="NUMBER_LIST_ITEMS" match="xhtml:li">
        <xsl:param name="type" as="xs:string" select="'1'"/>
        <xsl:param name="reversed" as="xs:boolean" select="false()"/>
        <xsl:param name="start" as="xs:integer" select="1"/>
        <xsl:variable name="value" as="xs:integer"
            select="if (@value) then xs:integer(@value)
                    else if ($reversed)
                    then count(following-sibling::xhtml:li) + $start
                    else count(preceding-sibling::xhtml:li) + $start"/>
        <xsl:variable name="formattedNumber" as="xs:string">
            <xsl:number value="$value" format="{$type}"/>
        </xsl:variable>
        <xsl:variable name="numberString" as="xs:string"
            select="if (descendant::text()[normalize-space() ne ''][1]/
                    matches(., '^\s+')) then concat($formattedNumber, '.')
                    else concat($formattedNumber, '. ')"/>
        <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.BEFORE"/>
        <li>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:choose>
                <xsl:when
                    test="node()[normalize-space() ne ''][1]/self::xhtml:p">
                    <p>
                        <xsl:copy-of select="xhtml:p[1]/(@id|@lang)"/>
                        <xsl:value-of select="$numberString"/>
                        <xsl:apply-templates select="xhtml:p[1]/node()"/>
                    </p>
                    <xsl:apply-templates mode="GROUP_INLINE_CONTENT"
                        select="xhtml:p[1]/following-sibling::node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$numberString"/>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
        <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.AFTER"/>
    </xsl:template>
    <xsl:template match="xhtml:li//xhtml:*[nota:is-page-break(.)]" priority="1">
        <xsl:if test="not(nota:starts-list-item(.) or nota:ends-list-item(.))">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:if>
    </xsl:template>
    <!-- NOTES -->
    <xsl:template match="xhtml:*[nota:is-note(.)]" priority="1">
        <xsl:variable name="class" as="xs:string*">
            <xsl:variable name="types" as="xs:string*"
                select="tokenize(@epub:type, '\s+')"/>
            <xsl:value-of
                select="if ($types = 'footnote') then 'footnote required'
                        else if ($types = 'rearnote') then 'endnote required'
                        else ''"/>
        </xsl:variable>
        <note>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </note>
    </xsl:template>
    <!-- OL -->
    <xsl:template match="xhtml:ol">
        <list type="ul" bullet="none">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:choose>
                <xsl:when test="nota:has-classes(., 'list-style-type-none')">
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="NUMBER_LIST_ITEMS">
                        <xsl:with-param name="type" as="xs:string"
                            select="if (@type) then @type else '1'"/>
                        <xsl:with-param name="reversed" as="xs:boolean"
                            select="exists(@reversed)"/>
                        <xsl:with-param name="start" as="xs:integer"
                            select="if (@start) then xs:integer(@start)
                                    else 1"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </list>
    </xsl:template>
    <xsl:template match="xhtml:ol[xhtml:li[nota:is-note(.)]]">
    	<xsl:apply-templates/>
    </xsl:template>
    <!-- P -->
    <xsl:template match="xhtml:p[nota:has-classes(., 'line')]" priority="1">
        <line>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </line>
    </xsl:template>
    <xsl:template match="xhtml:p[nota:is-bridgehead(.)]" priority="1">
        <p>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:attribute name="class" select="'bridgehead'"/>
            <xsl:apply-templates>
                <xsl:with-param name="discardEmStrong" as="xs:boolean"
                    tunnel="yes" select="true()"/>
            </xsl:apply-templates>
        </p>
    </xsl:template>
    <xsl:template match="xhtml:p[preceding-sibling::*[1]/self::xhtml:hr]">
        <xsl:variable name="class" as="xs:string*"
            select="if (nota:has-classes(preceding-sibling::*[1],
                    'emptyline')) then 'precedingemptyline'
                    else if (nota:has-classes(preceding-sibling::*[1],
                    'separator')) then 'precedingseparator'
                    else ''"/>
        <p>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:apply-templates/>
        </p>
    </xsl:template>
    <!-- PAGE BREAK -->
    <xsl:template
        match="xhtml:*[nota:is-page-break(.)]">
        <xsl:call-template name="ELEMENT.PAGENUM"/>
    </xsl:template>
    <!-- SECTION -->
    <xsl:template match="xhtml:section">
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor-or-self::xhtml:section)"/>
        <xsl:choose>
            <xsl:when test="$depth le 6">
                <level depth="{$depth}">
                    <xsl:call-template name="ATTRIBUTES.GENERIC"/>
                    <xsl:apply-templates/>
                </level>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xhtml:section[nota:is-poem(.)]">
        <div class="poem">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <xsl:template
        match="xhtml:section[nota:is-poem(.)]/xhtml:div[nota:is-stanza(.)]">
        <div class="stanza">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <!-- SPAN -->
    <xsl:template match="xhtml:span[nota:has-classes(., 'lic')]">
        <lic class="pageref">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </lic>
    </xsl:template>
    <xsl:template 
        match="xhtml:span[nota:has-classes(., 'linenum')]">
        <linenum>
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:apply-templates/>
        </linenum>
    </xsl:template>
    <!-- TABLE -->
    <xsl:template match="xhtml:table">
        <xsl:call-template name="ELEMENT.TABLE.CAPTION"/>
        <table>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:apply-templates/>
        </table>
    </xsl:template>
    <xsl:template match="xhtml:table[nota:get-page-breaks(.)]">
        <xsl:variable name="attributes" as="attribute()*"
            select="@class|@lang"/>
        <xsl:variable name="attributesWithId" as="attribute()*"
            select="$attributes|@id"/>
        <xsl:call-template name="ELEMENT.TABLE.CAPTION"/>
        <xsl:variable name="firstPass" as="node()">
            <table>
                <xsl:for-each
                    select="descendant::xhtml:tr[nota:get-page-breaks(.)]">
                    <xsl:apply-templates select="nota:get-preceding-rows(.)"/>
                    <xsl:for-each
                        select="nota:get-page-breaks(.)[nota:starts-row(.)]">
                        <xsl:call-template name="ELEMENT.PAGENUM"/>
                    </xsl:for-each>
                    <xsl:apply-templates select="self::xhtml:tr"/>
                    <xsl:for-each
                        select="nota:get-page-breaks(.)[nota:ends-row(.)]">
                        <xsl:call-template name="ELEMENT.PAGENUM"/>
                    </xsl:for-each>
                    <xsl:apply-templates select="nota:get-following-rows(.)"/>
                </xsl:for-each>
            </table>
        </xsl:variable>
        <xsl:for-each-group
            group-adjacent="generate-id(following-sibling::pagenum[1])"
            select="$firstPass/*">
            <xsl:copy-of select="current-group()[self::pagenum]"/>
            <table>
                <xsl:copy-of
                    select="if (position() eq 1) then $attributesWithId
                            else $attributes"/>
                <xsl:copy-of select="current-group()[self::tr]"/>
            </table>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template match="xhtml:table/xhtml:caption"/>
    <xsl:template match="xhtml:table//xhtml:*[nota:is-page-break(.)]"
        priority="1"/>
    <xsl:template match="xhtml:tbody|xhtml:tfoot|xhtml:thead">
        <xsl:apply-templates/>
    </xsl:template>
    <!-- TABLE CELLS -->
    <xsl:template match="xhtml:td">
        <td>
            <xsl:call-template name="ATTRIBUTES.TABLE.CELL"/>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </td>
    </xsl:template>
    <xsl:template match="xhtml:th">
        <th>
            <xsl:call-template name="ATTRIBUTES.TABLE.CELL"/>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </th>
    </xsl:template>
    <!-- UL -->
    <xsl:template match="xhtml:ul">
        <list type="ul">
            <xsl:call-template name="ATTRIBUTES.GENERIC"/>
            <xsl:attribute name="bullet"
                select="if (nota:has-classes(., 'list-style-type-none'))
                        then 'none' else 'yes'"/>
            <xsl:apply-templates/>
        </list>
    </xsl:template>
    <!-- FUNCTIONS -->
    <xsl:function name="nota:ends-list-item" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(following::node() intersect ancestor::xhtml:li
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    <xsl:function name="nota:ends-row" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(following::node() intersect ancestor::xhtml:tr
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    <xsl:function name="nota:get-page-breaks" as="element()*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence select="$n/descendant::xhtml:*[nota:is-page-break(.)]"/>
    </xsl:function>
    <xsl:function name="nota:get-file-name-from-path" as="xs:string">
        <xsl:param name="path" as="xs:string"/>
        <xsl:value-of select="tokenize($path, '/')[position() = last()]"/>
    </xsl:function>
    <xsl:function name="nota:get-preceding-rows" as="element(xhtml:tr)*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence
            select="$n/((preceding::xhtml:tr intersect ancestor::xhtml:table[1]/
                    descendant::xhtml:tr) except preceding::xhtml:tr
                    [nota:get-page-breaks(.)][1]/(self::xhtml:tr|
                    preceding::xhtml:tr))"/>
    </xsl:function>
    <xsl:function name="nota:get-following-rows" as="element(xhtml:tr)*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence
            select="$n/((following::xhtml:tr intersect ancestor::xhtml:table[1]/
                    descendant::xhtml:tr) except following::xhtml:tr
                    [nota:get-page-breaks(.)][1]/(self::xhtml:tr|
                    following::xhtml:tr))"/>
    </xsl:function>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:has-epub-types" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:param name="types" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@epub:type, '\s+') = $types"/>
    </xsl:function>
    <xsl:function name="nota:is-bridgehead" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="$n/(nota:has-classes(., 'bridgehead') or
                    nota:has-epub-types(., 'bridgehead'))"/>
    </xsl:function>
    <xsl:function name="nota:is-inline" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="$n/(self::text() or local-name() = $INLINE_ELEMENT_NAMES)"/>
    </xsl:function>
    <xsl:function name="nota:is-note" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="nota:has-epub-types($n, ('note', 'footnote', 'rearnote'))"/>
    </xsl:function>
    <xsl:function name="nota:is-page-break" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of select="nota:has-epub-types($n, 'pagebreak')"/>
    </xsl:function>
    <xsl:function name="nota:is-poem" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="nota:has-epub-types($n, ('z3998:poem', 'z3998:verse'))"/>
    </xsl:function>
    <xsl:function name="nota:is-stanza" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of select="nota:has-classes($n, 'linegroup')"/>
    </xsl:function>
    <xsl:function name="nota:starts-list-item" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(preceding::node() intersect ancestor::xhtml:li
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    <xsl:function name="nota:starts-row" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(preceding::node() intersect ancestor::xhtml:tr
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
</xsl:stylesheet>