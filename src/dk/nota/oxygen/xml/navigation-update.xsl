<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="dc epub nota opf xhtml xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="CONTENT_FOLDER_URL" as="xs:string"
        select="replace(document-uri(/), '/[^/]*?$', '/')"/>
    <xsl:variable name="EMPTY_SEQUENCE"/>
    <xsl:variable name="PID" as="xs:string"
        select="/opf:package/opf:metadata/dc:identifier/text()"/>
    <xsl:variable name="LANGUAGE" as="xs:string"
        select="/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:variable name="TITLE" as="xs:string"
        select="/opf:package/opf:metadata/dc:title/text()"/>
    <!-- Content documents after first pass -->
    <xsl:variable name="CONTENT_DOCUMENTS" as="node()*">
        <xsl:for-each select="/opf:package/opf:spine/opf:itemref">
            <xsl:variable name="item"
                select="//opf:item[@id eq current()/@idref]"/>
            <xsl:variable name="reference" as="xs:string"
                select="$item/@href"/>
            <xsl:variable name="isXHTMLDocument" as="xs:boolean"
                select="if ($item/@media-type eq 'application/xhtml+xml')
                        then true() else false()"/>
            <xsl:if test="$isXHTMLDocument">
                <xsl:message>
                    <nota:out>
                        <xsl:value-of
                            select="concat('Generating ids for document ',
                                    $reference)"/>
                    </nota:out>
                </xsl:message>
                <xsl:variable name="documentURL" as="xs:string"
                    select="concat($CONTENT_FOLDER_URL, $reference)"/>
                <nota:document name="{$reference}" url="{$documentURL}">
                    <xsl:apply-templates
                        select="document($documentURL)/xhtml:html"/>
                </nota:document>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <!-- NCX navigation document: Based on XHTML ditto -->
    <xsl:variable name="NCX_NAVIGATION_DOCUMENT" as="node()">
        <xsl:variable name="depth" as="xs:integer"
            select="max($XHTML_NAVIGATION_DOCUMENT//
                    xhtml:nav[@epub:type eq 'toc']/xhtml:ol/xhtml:li/
                    count(descendant-or-self::xhtml:li)) cast as xs:integer"/>
        <xsl:variable name="pageCount" as="xs:integer"
            select="count($PAGE_NUMBERS)"/>
        <xsl:variable name="lastPage" as="xs:string"
            select="$PAGE_NUMBERS[position() = last()]/@title"/>
        <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
            <head>
                <meta content="{$PID}" name="dtb:uid"/>
                <meta content="{$depth}" name="dtb:depth"/>
                <xsl:if test="$PAGE_NUMBERS">
                    <meta content="{$pageCount}" name="dtb:totalPageCount"/>
                    <meta content="{$lastPage}" name="dtb:maxPageNumber"/>
                </xsl:if>
            </head>
            <docTitle>
                <text>
                    <xsl:value-of select="$TITLE"/>
                </text>
            </docTitle>
            <navMap>
                <navLabel>
                    <text>Indhold</text>
                </navLabel>
                <xsl:apply-templates mode="GENERATE_NCX_HEADINGS"
                    select="$XHTML_NAVIGATION_DOCUMENT//
                            xhtml:nav[@epub:type eq 'toc']/xhtml:ol/xhtml:li"/>
            </navMap>
            <xsl:if test="$PAGE_NUMBERS">
                <pageList>
                    <navLabel>
                        <text>Liste over sider</text>
                    </navLabel>
                    <xsl:apply-templates mode="GENERATE_NCX_PAGES"
                        select="$PAGE_NUMBERS"/>
                </pageList>
            </xsl:if>
        </ncx>
    </xsl:variable>
    <!-- Page numbers -->
    <xsl:variable name="PAGE_NUMBERS" as="node()*"
        select="$CONTENT_DOCUMENTS//xhtml:*[@epub:type eq 'pagebreak']"/>
    <!-- Total number of TOC items: Dumb thing needed for NCX -->
    <xsl:variable name="TOC_ENTRY_COUNT" as="xs:integer"
        select="count($XHTML_NAVIGATION_DOCUMENT//
                xhtml:nav[@epub:type eq 'toc']//xhtml:li)"/>
    <!-- XHTML navigation document -->
    <xsl:variable name="XHTML_NAVIGATION_DOCUMENT" as="node()">
        <html xmlns="http://www.w3.org/1999/xhtml"
            xmlns:epub="http://www.idpf.org/2007/ops"
            xmlns:nordic="http://www.mtm.se/epub/"
            epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
            lang="{$LANGUAGE}"
            xml:lang="{$LANGUAGE}">
            <head>
                <meta charset="UTF-8"/>
                <title>
                    <xsl:value-of select="$TITLE"/>
                </title>
                <meta name="dc:identifier" content="{$PID}"/>
                <meta name="viewport" content="width=device-width"/>
                <xsl:for-each
                    select="/opf:package/opf:manifest/opf:item
                            [@media-type eq 'text/css']">
                    <link rel="stylesheet" type="text/css" href="{@href}"/>
                </xsl:for-each>
            </head>
            <body>
                <nav epub:type="toc">
                    <h1 lang="da" xml:lang="da">Indhold</h1>
                    <ol class="list-style-type-none">
                        <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                            select="$CONTENT_DOCUMENTS/xhtml:html/xhtml:body"/>
                    </ol>
                </nav>
                <xsl:if test="$PAGE_NUMBERS">
                    <nav epub:type="page-list">
                        <h1 lang="da" xml:lang="da">Liste over sider</h1>
                        <ol class="list-style-type-none">
                            <xsl:apply-templates mode="GENERATE_NAV_PAGES"
                                select="$PAGE_NUMBERS"/>
                        </ol>
                    </nav>
                </xsl:if>
            </body>
        </html>
    </xsl:variable>
    <xsl:template match="node()|@*" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <xsl:message>
            <nota:out>UPDATING NAVIGATION DOCUMENTS...</nota:out>
        </xsl:message>
        <nota:documents>
            <xsl:copy-of select="$CONTENT_DOCUMENTS"/>
            <nota:document
                url="{concat($CONTENT_FOLDER_URL, 'nav.xhtml')}">
                <xsl:copy-of select="$XHTML_NAVIGATION_DOCUMENT"/>
            </nota:document>
            <nota:document
                url="{concat($CONTENT_FOLDER_URL, 'nav.ncx')}">
                <xsl:copy-of select="$NCX_NAVIGATION_DOCUMENT"/>
            </nota:document>
        </nota:documents>
    </xsl:template>
    <!-- XHTML: Generate ids -->
    <xsl:template
        match="xhtml:body|xhtml:section|xhtml:*[matches(local-name(), 'h\d')]">
        <xsl:copy>
            <xsl:if test="not(@id)">
                <xsl:variable name="id" as="xs:string" select="generate-id()"/>
                <xsl:message>
                    <nota:out>
                        <xsl:value-of
                            select="concat('+++ Assigning id &quot;', $id,
                                    '&quot; to element ', local-name())"/>
                    </nota:out>
                </xsl:message>
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <!-- XHTML navigation -->
    <xsl:template mode="GENERATE_NAV_HEADINGS"
        match="xhtml:body[ancestor::nota:document/@name eq 'concat.xhtml']">
        <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
            select="xhtml:section"/>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_HEADINGS"
        match="xhtml:body|xhtml:section">
        <xsl:variable name="heading" as="node()*"
            select="xhtml:*[matches(local-name(), 'h\d')]"/>
        <xsl:variable name="headingText" as="xs:string"
            select="if ($heading)
                    then normalize-space(string-join($heading//text(), ''))
                    else '[***]'"/>
        <xsl:variable name="documentName" as="xs:string"
            select="ancestor::nota:document/@name"/>
        <xsl:variable name="id" as="xs:string"
            select="if ($heading) then $heading/@id
                    else @id"/>
        <li>
            <a href="{concat($documentName, '#', $id)}">
                <xsl:value-of select="$headingText"/>
            </a>
            <xsl:if
                test="xhtml:section[not(matches(@epub:type, 'z3998:(poem|verse)'))]">
                <ol>
                    <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                        select="xhtml:section"/>
                </ol>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_PAGES"
        match="xhtml:*[@epub:type = 'pagebreak']">
        <xsl:variable name="documentName" as="xs:string"
            select="ancestor::nota:document/@name"/>
        <li>
            <a href="{concat($documentName, '#', @id)}">
                <xsl:value-of select="@title"/>
            </a>
        </li>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_HEADINGS"
        match="xhtml:section[matches(@epub:type, 'z3998:(poem|verse)')]"/>
    <!-- NCX navigation -->
    <xsl:template mode="GENERATE_NCX_HEADINGS" match="xhtml:li">
        <xsl:variable name="count" as="xs:integer"
            select="count((preceding::xhtml:li|ancestor-or-self::xhtml:li)
                    intersect ancestor::xhtml:nav//node())"/>
        <navPoint xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{concat('navPoint-', $count)}" playOrder="{$count}">
            <navLabel>
                <text>
                    <xsl:value-of select="xhtml:a/text()"/>
                </text>
            </navLabel>
            <content src="{xhtml:a/@href}"/>
            <xsl:if test="xhtml:ol">
                <xsl:apply-templates mode="GENERATE_NCX_HEADINGS"
                    select="xhtml:ol/xhtml:li"/>
            </xsl:if>
        </navPoint>
    </xsl:template>
    <xsl:template mode="GENERATE_NCX_PAGES"
        match="xhtml:*[@epub:type eq 'pagebreak']">
        <xsl:variable name="count" as="xs:integer" select="position()"/>
        <xsl:variable name="type" as="xs:string"
            select="if (@class = 'page-front') then 'front'
                    else if (@class = 'page-special') then 'special'
                    else 'normal'"/>
        <xsl:variable name="documentName" as="xs:string"
            select="ancestor::nota:document/@name"/>
        <pageTarget xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{concat('pageTarget-', $count)}"
            playOrder="{$TOC_ENTRY_COUNT + $count}" type="{$type}">
            <navLabel>
                <text>
                    <xsl:value-of select="@title"/>
                </text>
            </navLabel>
            <content src="{concat($documentName, '#', @id)}"/>
        </pageTarget>
    </xsl:template>
</xsl:stylesheet>