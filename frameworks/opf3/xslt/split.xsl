<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/epub"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="dc epub nota opf xhtml xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="EPUB_FOLDER_URL" as="xs:string"
        select="replace(document-uri(.), '(.+/)package.opf$', '$1')"/>
    <xsl:variable name="PID" as="xs:string"
        select="/xhtml:html/xhtml:head/xhtml:meta[@name = 'dc:identifier']/
                @content"/>
    <xsl:template match="node()|@*" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <xsl:for-each select="xhtml:html/xhtml:body/xhtml:section">
            <xsl:variable name="documentName" as="xs:string"
                select="nota:create-document-name(.)"/>
            <xsl:result-document
                href="{concat($EPUB_FOLDER_URL, $documentName)}">
                <xsl:value-of disable-output-escaping="yes"
                    select="'&#xa;&lt;!DOCTYPE html&gt;&#xa;'"/>
                <html xmlns="http://www.w3.org/1999/xhtml"
                    xmlns:epub="http://www.idpf.org/2007/ops"
                    xmlns:nordic="http://www.mtm.se/epub/"
                    epub:prefix="z3998:http://www.daisy.org/z3998/2012/vocab/structure/#"
                    lang="{@lang}"
                    xml:lang="{@xml:lang}">
                    <xsl:sequence
                        select="if (@lang or @xml:lang) then @lang|@xml:lang
                                else /xhtml:html/(@lang|@xml:lang)"/>
                    <xsl:sequence select="/xhtml:html/xhtml:head"/>
                    <body>
                        <xsl:if test="not(@id)">
                            <xsl:attribute name="id" select="generate-id()"/>
                        </xsl:if>
                        <xsl:apply-templates
                            select="@*[not(matches(name(), '(xml:)*lang'))]|
                            node()"/>
                    </body>
                </html>
            </xsl:result-document>
            <xsl:message select="$documentName"/>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="xhtml:a[matches(@href, '^#.+$')]">
        <xsl:variable name="referencedId" as="xs:string"
            select="substring-after(@href, '#')"/>
        <xsl:variable name="referencedElement"
            select="//xhtml:*[@id = $referencedId]"/>
        <xsl:variable name="referencedSection"
            select="$referencedElement/ancestor::xhtml:section
                    [position() = last()]"/>
        <xsl:variable name="referencedFile" as="xs:string"
            select="if (ancestor::node() intersect $referencedSection)
                    then ''
                    else nota:create-document-name($referencedSection)"/>
        <xsl:copy>
            <xsl:attribute name="href"
                select="concat($referencedFile, @href)"/>
            <xsl:apply-templates select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:function name="nota:create-document-name" as="xs:string">
        <xsl:param name="node"/>
        <xsl:variable name="position" as="xs:integer"
            select="count($node/preceding-sibling::xhtml:section) + 1"/>
        <xsl:variable name="number" as="xs:string"
            select="format-number($position, '000')"/>
        <xsl:variable name="type" as="xs:string"
            select="nota:get-primary-type($node/@epub:type)"/>
        <xsl:value-of
            select="concat($PID, '-', $number, '-', $type, '.xhtml')"/>
    </xsl:function>
    <xsl:function name="nota:get-primary-type" as="xs:string">
        <xsl:param name="typeString" as="xs:string"/>
        <xsl:variable name="types"
            select="tokenize(normalize-space($typeString), '\s+')"/>
        <xsl:value-of
            select="if (count($types) gt 1)
                    then $types[not(matches(., '(back|body|front)matter'))][1]
                    else $types[1]"/>
    </xsl:function>
</xsl:stylesheet>