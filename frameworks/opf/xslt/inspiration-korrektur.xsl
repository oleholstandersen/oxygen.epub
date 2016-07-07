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
        select="replace(document-uri(/), '/[^/]*$', '/')"/>
    <xsl:param name="NAVIGATION_DOCUMENT" as="document-node()?"
        select="document(concat($CONTENT_FOLDER_URL, 'nav.xhtml'))"/>
    <xsl:param name="OUTPUT_FOLDER_URL" as="xs:string"
        select="replace(document-uri(/), '/.*?!.*$', '/')"/>
    <xsl:variable name="PID" as="xs:string?"
        select="/opf:package/opf:metadata/dc:identifier[1]/text()"/>
    <xsl:variable name="LANGUAGE" as="xs:string?"
        select="/opf:package/opf:metadata/dc:language[1]/text()"/>
    <xsl:variable name="TITLE" as="xs:string?"
        select="/opf:package/opf:metadata/dc:title[1]/text()"/>
    <!-- Concat document template -->
    <xsl:template name="CONCAT_DOCUMENT">
        <html xmlns:epub="http://www.idpf.org/2007/ops"
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
                <style type="text/css">
                    body {
                        font-style: normal;
                        font-weight: normal;
                        font-family: arial, verdana, sans-serif;
                        margin-right: 10%;
                        margin-left: 10%;
                    }
                    h1 {
                        color:#006400;
                    }
                    h2 {
                        color:#006400;
                    }
                    h3 {
                        color:#006400;
                    }
                    h4 {
                        color:#006400;
                    }
                    h5 {
                        color:#006400;
                    }
                    h6 {
                        color:#006400;
                    }
                    span.titellinie {
                        font-weight: bold;
                    }
                    span.typedescription {
                        font-weight: bold;
                        font-style: italic;
                    }
                    span.masternummer {
                        font-weight: bold;
                        font-style: italic;
                    }
                    span.othereditions {
                        font-weight: bold;
                        font-style: italic;
                    }
                    span.OEE {
                        font-weight: bold;
                        font-style: italic;
                    }
                    span.OEL {
                        font-weight: bold;
                        font-style: italic;
                    }
                    span.OEP {
                        font-weight: bold;
                        font-style: italic;
                    }
                </style>
            </head>
            <body>
                <xsl:call-template name="SECTIONS.GROUP">
                    <xsl:with-param name="sections"
                        select="$CONTENT_DOCUMENTS_FIRST_PASS"/>
                </xsl:call-template>
            </body>
        </html>
    </xsl:template>
    <!-- Content documents after first pass -->
    <xsl:variable name="CONTENT_DOCUMENTS_FIRST_PASS" as="element()*">
        <xsl:for-each
            select="/opf:package/opf:spine/opf:itemref[@idref ne 'concat']">
            <xsl:variable name="item" as="node()?"
                select="//opf:item[@id = current()/@idref]"/>
            <xsl:variable name="reference" as="xs:string?"
                select="$item/@href"/>
            <xsl:if test="$item/@media-type = 'application/xhtml+xml'">
                <xsl:variable name="documentUrl" as="xs:string"
                    select="concat($CONTENT_FOLDER_URL, $reference)"/>
                <xsl:apply-templates mode="XHTML_FIRST_PASS"
                    select="document($documentUrl)/xhtml:html/xhtml:body">
                    <xsl:with-param name="originalDocumentName" as="xs:string"
                        select="$reference"/>
                </xsl:apply-templates>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:template name="ATTRIBUTE.ID">
        <xsl:if test="not(@id)">
            <xsl:variable name="id" as="xs:string" select="generate-id()"/>
            <xsl:attribute name="id" select="$id"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="ATTRIBUTE.LANG">
        <xsl:variable name="langAttribute" as="attribute()?"
            select="ancestor-or-self::xhtml:*[@lang][1]/@lang"/>
        <xsl:variable name="xmlLangAttribute" as="attribute()?"
            select="ancestor-or-self::xhtml:*[@xml:lang][1]/@xml:lang"/>
        <xsl:if test="not($langAttribute = $LANGUAGE)">
            <xsl:copy-of select="$langAttribute"/>
        </xsl:if>
        <xsl:if test="not($xmlLangAttribute = $LANGUAGE)">
            <xsl:copy-of select="$xmlLangAttribute"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="SECTIONS.GROUP">
        <xsl:param name="sections" as="element()*"/>
        <xsl:param name="level" as="xs:integer" select="0"/>
        <xsl:for-each-group select="$sections"
            group-starting-with="xhtml:section[@nota:depthModifier = $level]">
            <section>
                <xsl:apply-templates select="current-group()[1]/node()">
                    <xsl:with-param name="depthModifier" as="xs:integer"
                        tunnel="yes" select="$level - 1"/>
                </xsl:apply-templates>
                <xsl:call-template name="SECTIONS.GROUP">
                    <xsl:with-param name="sections" as="element()*"
                        select="current-group()[position() gt 1]"/>
                    <xsl:with-param name="level" as="xs:integer"
                        select="$level + 1"/>
                </xsl:call-template>
            </section>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template match="@*|node()" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/opf:package">
        <!--<xsl:copy-of select="$CONTENT_DOCUMENTS_FIRST_PASS"/>-->
        <xsl:call-template name="CONCAT_DOCUMENT"/>
    </xsl:template>
    <!-- XHTML first pass: Assign IDs and document names -->
    <xsl:template mode="XHTML_FIRST_PASS" match="xhtml:body">
        <xsl:param name="originalDocumentName" as="xs:string"/>
        <xsl:variable name="navEntry" as="element()?"
            select="($NAVIGATION_DOCUMENT/xhtml:html/xhtml:body/xhtml:nav
                    [@epub:type eq 'toc']//xhtml:a[matches(@href, concat('^',
                    $originalDocumentName))])[1]"/>
        <xsl:variable name="depthModifier" as="xs:integer"
            select="if ($navEntry) then $navEntry/count(ancestor::xhtml:li) - 1
                    else 0"/>
        <section nota:depthModifier="{$depthModifier}"
            nota:originalDocumentName="{$originalDocumentName}">
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:call-template name="ATTRIBUTE.LANG"/>
            <xsl:apply-templates mode="XHTML_FIRST_PASS"
                select="@*[not(local-name() eq 'lang')]|node()">
                <xsl:with-param name="depthModifier" as="xs:integer"
                    tunnel="yes" select="$depthModifier"/>
            </xsl:apply-templates>
        </section>
    </xsl:template>
    <xsl:template mode="XHTML_FIRST_PASS"
        match="xhtml:*[matches(local-name(), 'h\d')]">
        <xsl:param name="depthModifier" as="xs:integer" tunnel="yes"
            select="0"/>
        <xsl:variable name="depth" as="xs:integer"
            select="xs:integer(substring(local-name(), 2))"/>
        <xsl:element name="{concat('h', $depth + $depthModifier)}">
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:apply-templates mode="XHTML_FIRST_PASS" select="node()|@*"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="xhtml:section">
        <xsl:copy>
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:apply-templates mode="XHTML_FIRST_PASS" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <!-- XHTML second pass: Update references -->
    <xsl:template mode="XHTML_SECOND_PASS" match="xhtml:a">
        <xsl:variable name="reference" as="xs:string"
            select="if (not(contains(@href, ':')))
                    then concat('#', substring-after(@href, '#'))
                    else @href"/>
        <xsl:copy>
            <xsl:attribute name="href" select="$reference"/>
            <xsl:apply-templates mode="XHTML_SECOND_PASS"
                select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="XHTML_SECOND_PASS" match="@nota:depthModifier"/>
    <xsl:template mode="XHTML_SECOND_PASS" match="@nota:originalDocumentName"/>
</xsl:stylesheet>