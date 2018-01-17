<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" indent="no"/>
    <xsl:strip-space elements="opf:*"/>
    <xsl:param name="CONTENT_FOLDER_URL" as="xs:string"
        select="replace(document-uri(/), '/[^/]*$', '/')"/>
    <xsl:param name="UPDATE_EPUB" as="xs:boolean" select="true()"/>
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
                <xsl:for-each
                    select="/opf:package/opf:manifest/opf:item
                            [@media-type eq 'text/css']">
                    <link rel="stylesheet" type="text/css" href="{@href}"/>
                </xsl:for-each>
            </head>
            <body>
                <xsl:apply-templates mode="XHTML_SECOND_PASS"
                    select="$CONTENT_DOCUMENTS_FIRST_PASS"/>
            </body>
        </html>
    </xsl:template>
    <!-- Content documents after first pass -->
    <xsl:variable name="CONTENT_DOCUMENTS_FIRST_PASS" as="node()*">
        <xsl:for-each
            select="/opf:package/opf:spine/opf:itemref[@idref ne 'concat']">
            <xsl:variable name="item" as="node()?"
                select="//opf:item[@id = current()/@idref]"/>
            <xsl:if test="not($item)">
                <xsl:message terminate="yes">
                    <nota:out>
                        <xsl:value-of
                            select="concat('ERROR: Spine reference ',
                                    @idref, ' does not resolve')"/>
                    </nota:out>
                    <nota:systemid>
                        <xsl:value-of
                            select="concat(base-uri(/), '#', @idref)"/>
                    </nota:systemid>
                </xsl:message>
            </xsl:if>
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
                <xsl:message>
                    <nota:document>
                        <xsl:value-of select="$reference"/>
                    </nota:document>
                </xsl:message>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:template name="ATTRIBUTE.ID">
        <xsl:if test="not(@id)">
            <xsl:variable name="id" as="xs:string" select="generate-id()"/>
            <xsl:message>
                <nota:out>
                    <xsl:value-of
                        select="concat('Assigning id &quot;', $id,
                                '&quot; to element ', local-name())"/>
                </nota:out>
            </xsl:message>
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
    <xsl:template match="@*|node()" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <xsl:message>
            <nota:out>CONCATENATING CONTENT DOCUMENTS...</nota:out>
        </xsl:message>
        <xsl:if test="$UPDATE_EPUB">
            <xsl:if
                test="document(concat($CONTENT_FOLDER_URL, 'concat.xhtml'))">
                <xsl:message terminate="yes">
                    <nota:out>ERROR: concat.xhtml already exists</nota:out>
                    <nota:systemid>
                        <xsl:value-of
                            select="concat($CONTENT_FOLDER_URL,
                                    'concat.xhtml')"/>
                    </nota:systemid>
                </xsl:message>
            </xsl:if>
        </xsl:if>
        <nota:documents>
            <nota:document
                url="{concat($CONTENT_FOLDER_URL, 'concat.xhtml')}">
                <xsl:call-template name="CONCAT_DOCUMENT"/>
            </nota:document>
            <xsl:if test="$UPDATE_EPUB">
                <nota:document
                    url="{concat($CONTENT_FOLDER_URL, 'package.opf')}">
                    <xsl:apply-templates mode="OPF"/>
                </nota:document>
            </xsl:if>
        </nota:documents>
    </xsl:template>
    <!-- XHTML first pass: Assign IDs and document names -->
    <xsl:template mode="XHTML_FIRST_PASS" match="xhtml:body">
        <xsl:param name="originalDocumentName" as="xs:string"/>
        <xsl:message>
            <nota:out>
                <xsl:value-of
                    select="concat('Adding document ', $originalDocumentName)"/>
            </nota:out>
        </xsl:message>
        <section nota:originalDocumentName="{$originalDocumentName}">
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:call-template name="ATTRIBUTE.LANG"/>
            <xsl:apply-templates mode="XHTML_FIRST_PASS"
                select="@*[not(local-name() eq 'lang')]|node()"/>
        </section>
    </xsl:template>
    <xsl:template mode="XHTML_FIRST_PASS"
        match="xhtml:section|xhtml:*[matches(local-name(), 'h\d')]">
        <xsl:copy>
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:apply-templates mode="XHTML_FIRST_PASS" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="XHTML_FIRST_PASS"
        match="xhtml:img[not(@height|@width)]">
    	<xsl:variable name="height" as="xs:integer"
    	    select="nota:get-image-size(concat($CONTENT_FOLDER_URL, @src))[2]"/>
        <xsl:variable name="width" as="xs:integer"
            select="nota:get-image-size(concat($CONTENT_FOLDER_URL, @src))[1]"/>
    	<xsl:copy>
    	    <xsl:copy-of select="@* except (@height|@width)"/>
    	    <xsl:if test="$height ne -1 and $width ne -1">
 	        	<xsl:attribute name="height" select="$height"/>
    	    	<xsl:attribute name="width" select="$width"/>
    	    </xsl:if>
    	</xsl:copy>
    </xsl:template>
    <!-- XHTML second pass: Update references -->
    <xsl:template mode="XHTML_SECOND_PASS" match="xhtml:a[@href]">
        <xsl:variable name="reference" as="xs:string"
            select="if (not(contains(@href, ':'))) then (
                    if (matches(@href, '#.+$'))
                    then concat('#', substring-after(@href, '#'))
                    else concat('#', ancestor::documents/xhtml:section[matches(
                    @nota:originalDocumentName, current()/@href)]/@id))
                    else @href"/>
        <xsl:copy>
            <xsl:attribute name="href" select="$reference"/>
            <xsl:apply-templates mode="XHTML_SECOND_PASS"
                select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="XHTML_SECOND_PASS"
        match="xhtml:section/@nota:originalDocumentName"/>
    <!-- OPF: Remove references to content documents and add concat.xhtml -->
    <xsl:template mode="OPF" match="opf:item">
        <xsl:if test="not(//opf:itemref[@idref = current()/@id])">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>
    <xsl:template mode="OPF" match="opf:itemref">
        <xsl:variable name="item" as="node()*"
            select="//opf:item[@id = current()/@idref]"/>
        <xsl:variable name="reference" as="xs:string*"
            select="$item/@href"/>
        <xsl:variable name="isXhtmlDocument" as="xs:boolean"
            select="$item/@media-type = 'application/xhtml+xml'"/>
        <xsl:if test="not($isXhtmlDocument)">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>
    <xsl:template mode="OPF" match="opf:manifest">
        <xsl:copy>
            <xsl:apply-templates mode="OPF" select="node()|@*"/>
            <item xmlns="http://www.idpf.org/2007/opf" href="concat.xhtml"
                id="concat" media-type="application/xhtml+xml" />
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="OPF" match="opf:spine">
        <xsl:copy>
            <xsl:apply-templates mode="OPF" select="node()|@*"/>
            <itemref xmlns="http://www.idpf.org/2007/opf" idref="concat"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>