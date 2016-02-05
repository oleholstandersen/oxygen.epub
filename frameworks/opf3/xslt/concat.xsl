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
    <xsl:variable name="contentDocumentsFirstPass">
        <xsl:for-each
            select="/opf:package/opf:spine/opf:itemref
                    [not(@idref = 'concat')]">
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
                <xsl:apply-templates mode="XHTML_FIRST_PASS"
                    select="document($documentURL)/xhtml:html/xhtml:body">
                    <xsl:with-param name="originalDocumentName"
                        select="$reference"/>
                </xsl:apply-templates>
            </xsl:if>
            <xsl:message select="concat(@idref, ',', $reference)"/>
        </xsl:for-each>
    </xsl:variable>
    <xsl:template match="node()|@*" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when
                test="document(concat($EPUB_FOLDER_URL, 'concat.xhtml'))">
                <xsl:message terminate="yes"
                    select="'concat.xhtml already exists'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:result-document
                    href="{concat($EPUB_FOLDER_URL, 'concat.xhtml')}">
                    <xsl:call-template name="finalDocument"/>
                </xsl:result-document>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="finalDocument">
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
                <xsl:apply-templates mode="XHTML_SECOND_PASS"
                    select="$contentDocumentsFirstPass/node()"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="xhtml:body" mode="XHTML_FIRST_PASS">
        <xsl:param name="originalDocumentName" as="xs:string"/>
        <section originalDocumentName="{$originalDocumentName}">
            <xsl:if test="not(@id)">
                <xsl:attribute name="id" select="generate-id()"/>
            </xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </section>
    </xsl:template>
    <xsl:template match="xhtml:section|xhtml:*[matches(local-name(), 'h\d')]"
        mode="XHTML_FIRST_PASS">
        <xsl:copy>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id" select="generate-id()"/>
            </xsl:if>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="xhtml:a" mode="XHTML_SECOND_PASS">
        <xsl:variable name="isInternalReference" as="xs:boolean"
            select="if (matches(@href, concat('^', $PID, '-\d+-.+\.xhtml')))
                    then true()
                    else false()"/>
        <xsl:copy>
            <xsl:attribute name="href"
                select="if ($isInternalReference) then (
                        if (matches(@href, '#.+$')) then
                        concat('#', substring-after(@href, '#'))
                        else
                        concat('#',
                        ancestor::documents/xhtml:section
                        [matches(current()/@href, @originalDocumentName)]/@id))
                        else @href"/>
            <xsl:apply-templates mode="XHTML_SECOND_PASS"
                select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="xhtml:section/@originalDocumentName"
        mode="XHTML_SECOND_PASS"/>    
</xsl:stylesheet>