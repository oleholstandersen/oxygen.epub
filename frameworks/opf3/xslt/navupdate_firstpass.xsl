<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/epub"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="dc epub nota opf xhtml xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="EPUB_FOLDER_URL" as="xs:string"
        select="replace(document-uri(.), '(.+/)package.opf$', '$1')"/>
    <xsl:variable name="EMPTY_SEQUENCE"/>
    <xsl:variable name="PID" as="xs:string"
        select="/opf:package/opf:metadata/dc:identifier/text()"/>
    <xsl:variable name="LANGUAGE" as="xs:string"
        select="/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:variable name="TITLE" as="xs:string"
        select="/opf:package/opf:metadata/dc:title/text()"/>
    <xsl:template match="node()|@*" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <xsl:sequence select="$contentDocumentsFirstPass"/>
        <document xmlns="" name="nav.xhtml"
            URL="{concat($EPUB_FOLDER_URL, 'nav.xhtml')}">
            <xsl:sequence select="$xhtmlNavigationDocument"/>
        </document>
        <document xmlns="" name="nav.ncx"
            URL="{concat($EPUB_FOLDER_URL, 'nav.ncx')}">
            <xsl:sequence select="$ncxNavigationDocument"/>
        </document>
    </xsl:template>
    <xsl:variable name="contentDocumentsFirstPass">
        <xsl:for-each select="/opf:package/opf:spine/opf:itemref">
            <xsl:variable name="item"
                select="//opf:item[@id = current()/@idref]"/>
            <xsl:variable name="reference" as="xs:string"
                select="$item/@href"/>
            <xsl:variable name="isXHTMLDocument" as="xs:boolean"
                select="if ($item/@media-type = 'application/xhtml+xml')
                then true() else false()"/>
            <xsl:if test="$isXHTMLDocument">
                <xsl:variable name="documentURL" as="xs:string"
                    select="concat($EPUB_FOLDER_URL, $reference)"/>
                <document xmlns="" name="{$reference}" URL="{$documentURL}">
                    <xsl:apply-templates mode="GENERATE_IDS"
                        select="document($documentURL)"/>
                </document>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="pageNumbers"
        select="$contentDocumentsFirstPass//
                xhtml:*[@epub:type = 'pagebreak']"/>
    <xsl:template
        match="xhtml:body|xhtml:section|xhtml:*[matches(local-name(), 'h\d')]"
        mode="GENERATE_IDS">
        <xsl:copy>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id" select="generate-id()"/>
            </xsl:if>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:variable name="xhtmlNavigationDocument">
        <xsl:value-of disable-output-escaping="yes"
            select="'&#xa;&lt;!DOCTYPE html&gt;&#xa;'"/>
        <html xmlns:epub="http://www.idpf.org/2007/ops"
            xmlns:nordic="http://www.mtm.se/epub/"
            epub:prefix="z3998:http://www.daisy.org/z3998/2012/vocab/structure/#"
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
                            [@media-type ='text/css']">
                    <link rel="stylesheet" type="text/css" href="{@href}"/>
                </xsl:for-each>
            </head>
            <body>
                <nav epub:type="toc">
                    <h1 lang="da" xml:lang="da">Indhold</h1>
                    <ol class="list-style-type-none">
                        <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                            select="$contentDocumentsFirstPass/
                                    document/xhtml:html/xhtml:body"/>
                    </ol>
                </nav>
                <xsl:if test="$pageNumbers">
                    <nav epub:type="page-list">
                        <h1 lang="da" xml:lang="da">Liste over sider</h1>
                        <ol class="list-style-type-none">
                            <xsl:apply-templates mode="GENERATE_NAV_PAGES"
                                select="$pageNumbers"/>
                        </ol>
                    </nav>
                </xsl:if>
                
            </body>
        </html>
    </xsl:variable>
    <xsl:template match="xhtml:body|xhtml:section" mode="GENERATE_NAV_HEADINGS">
        <xsl:variable name="heading"
            select="xhtml:*[matches(local-name(), 'h\d')]"/>
        <xsl:variable name="headingText" as="xs:string"
            select="if ($heading)
                    then string-join($heading/descendant::text(), '')
                    else '***'"/>
        <xsl:variable name="fileName" select="ancestor::document/@fileName"/>
        <xsl:variable name="id"
            select="if ($heading) then $heading/@id
                    else @id"/>
        <li>
            <a href="{concat($fileName, '#', $id)}">
                <xsl:value-of select="$headingText"/>
            </a>
            <xsl:if
                test="xhtml:section[not(matches(@epub:type, 'z3998:verse'))]">
                <ol>
                    <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                        select="xhtml:section"/>
                </ol>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template match="xhtml:*[@epub:type = 'pagebreak']"
        mode="GENERATE_NAV_PAGES">
        <xsl:variable name="fileName" as="xs:string"
            select="ancestor::document/@fileName"/>
        <li>
            <a href="{concat($fileName, '#', @id)}">
                <xsl:value-of select="@title"/>
            </a>
        </li>
    </xsl:template>
    <xsl:template match="xhtml:section[matches(@epub:type, 'z3998:verse')]"
        mode="GENERATE_NAV_HEADINGS"/>
    <xsl:variable name="ncxNavigationDocument">
        <xsl:variable name="depth" as="xs:integer"
            select="max($xhtmlNavigationDocument/xhtml:html/xhtml:body/
                    xhtml:nav[@epub:type = 'toc']//xhtml:li/
                    count(ancestor-or-self::xhtml:li)) cast as xs:integer"/>
        <xsl:variable name="pageCount" as="xs:integer"
            select="count($xhtmlNavigationDocument/xhtml:html/xhtml:body/
                    xhtml:nav[@epub:type = 'page-list']/xhtml:ol/xhtml:li)"/>
        <xsl:variable name="lastPage" as="xs:integer"
            select="max($xhtmlNavigationDocument/xhtml:html/xhtml:body/
                    xhtml:nav[@epub:type = 'page-list']/xhtml:ol/xhtml:li/
                    xhtml:a/text()[matches(., '^\d+$')]) cast as xs:integer"/>
        <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
            <head>
                <meta content="{$PID}" name="dtb:uid"/>
                <meta content="{$depth}" name="dtb:depth"/>
                <meta content="{$pageCount}" name="dtb:totalPageCount"/>
                <meta content="{$lastPage}" name="dtb:maxPageNumber"/>
            </head>
            <docTitle>
                <text>
                    <xsl:value-of select="$TITLE"/>
                </text>
            </docTitle>
            <navMap>
                <xsl:apply-templates mode="GENERATE_NCX_HEADINGS"
                    select="$xhtmlNavigationDocument/xhtml:html/xhtml:body/
                            xhtml:nav[@epub:type = 'toc']/xhtml:ol/xhtml:li"/>
            </navMap>
            <xsl:if test="$pageNumbers">
                <pageList>
                    <xsl:apply-templates mode="GENERATE_NCX_PAGES"
                        select="$pageNumbers"/>
                </pageList>
            </xsl:if>
        </ncx>
    </xsl:variable>
    <xsl:template match="xhtml:li" mode="GENERATE_NCX_HEADINGS">
        <xsl:variable name="count" as="xs:integer"
            select="count((preceding::xhtml:li|ancestor-or-self::xhtml:li)
                    intersect ancestor::xhtml:nav/descendant::node())"/>
        <navPoint xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{concat('navPoint-', $count)}" playOrder="{$count}">
            <navLabel>
                <text>
                    <xsl:value-of select="xhtml:a/text()"/>
                </text>
                <content src="{xhtml:a/@href}"/>
            </navLabel>
            <xsl:if test="xhtml:ol">
                <xsl:apply-templates mode="GENERATE_NCX_HEADINGS"
                    select="xhtml:ol/xhtml:li"/>
            </xsl:if>
        </navPoint>
    </xsl:template>
    <xsl:variable name="countFrom" as="xs:integer"
        select="count($xhtmlNavigationDocument/xhtml:html/xhtml:body/
                xhtml:nav[@epub:type = 'toc']//xhtml:li)"/>
    <xsl:template match="xhtml:*[@epub:type = 'pagebreak']"
        mode="GENERATE_NCX_PAGES">
        <xsl:variable name="count" as="xs:integer"
            select="count(preceding::xhtml:*[@epub:type = 'pagebreak']) + 1"/>
        <xsl:variable name="type" as="xs:string"
            select="if (@class = 'page-front') then 'front'
                    else if (@class = 'page-special') then 'special'
                    else 'normal'"/>
        <xsl:variable name="fileName" as="xs:string"
            select="ancestor::document/@fileName"/>
        <pageTarget xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{concat('pageTarget-', $count)}"
            playOrder="{$countFrom + $count}" type="{$type}">
            <navLabel>
                <text>
                    <xsl:value-of select="@title"/>
                </text>
                <content src="{concat($fileName, '#', @id)}"/>
            </navLabel>
        </pageTarget>
    </xsl:template>
</xsl:stylesheet>