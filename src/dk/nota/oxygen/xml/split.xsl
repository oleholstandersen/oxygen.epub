<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" indent="no"/>
    <xsl:param name="CONTENT_FOLDER_URL" as="xs:string"
        select="replace(document-uri(/), '[^/]*$', '')"/>
    <xsl:param name="ID_BASE" as="xs:string" select="'split'"/>
    <xsl:param name="ID_COUNT" as="xs:integer"
        select="/opf:package/opf:manifest/count(opf:item[matches(@id,
                concat('^', $ID_BASE, '_\d+$'))])">
    </xsl:param>
    <xsl:variable name="PID" as="xs:string"
        select="/opf:package/opf:metadata/dc:identifier/text()"/>
    <xsl:variable name="LANGUAGE" as="xs:string"
        select="/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:template name="ID_ATTRIBUTE">
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
    <!-- XHTML content document template -->
    <xsl:template name="XHTML_DOCUMENT">
        <html xmlns="http://www.w3.org/1999/xhtml"
            xmlns:epub="http://www.idpf.org/2007/ops"
            xmlns:nordic="http://www.mtm.se/epub/"
            epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#">
            <xsl:choose>
                <xsl:when test="@lang or @xml:lang">
                    <xsl:attribute name="lang" select="@xml:lang"/>
                    <xsl:attribute name="xml:lang" select="@xml:lang"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="lang" select="$LANGUAGE"/>
                    <xsl:attribute name="xml:lang" select="$LANGUAGE"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="ancestor::xhtml:html/xhtml:head"/>
            <body>
                <xsl:call-template name="ID_ATTRIBUTE"/>
                <xsl:apply-templates
                    select="@*[not(local-name() eq 'lang')]|node()"/>
            </body>
        </html>
    </xsl:template>
    <xsl:variable name="CONCAT_DOCUMENT" as="document-node()*">
        <xsl:sequence
            select="document(concat($CONTENT_FOLDER_URL, $CONCAT_ITEM/@href))"/>
    </xsl:variable>
    <xsl:variable name="CONCAT_ITEM" as="node()*"
        select="/opf:package/opf:manifest/opf:item[@id eq 'concat']"/>
    <xsl:template match="@*|node()" mode="#all">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <xsl:message>
            <nota:out>SPLITTING CONCAT DOCUMENT...</nota:out>
        </xsl:message>
        <xsl:if test="not($CONCAT_ITEM)">
            <xsl:message terminate="yes">
                <nota:out>ERROR: No manifest item with id "concat"</nota:out>
            </xsl:message>
        </xsl:if>
        <xsl:if test="not($CONCAT_DOCUMENT)">
            <xsl:message terminate="yes">
                <nota:out>ERROR: Unable to process concat document</nota:out>
            </xsl:message>
        </xsl:if>
        <nota:documents>
            <xsl:for-each
                select="$CONCAT_DOCUMENT/xhtml:html/xhtml:body/xhtml:section">
                <xsl:variable name="documentName" as="xs:string"
                    select="nota:create-document-name(.)"/>
                <xsl:if test="nota:get-primary-type(@epub:type) eq ''">
                    <xsl:message>
                        <nota:out>
                            <xsl:value-of
                                select="concat('WARNING: The division at ',
                                        local-name(), position(), ' has no epub:type')"/>
                        </nota:out>
                    </xsl:message>
                </xsl:if>
                <nota:document
                    url="{concat($CONTENT_FOLDER_URL, $documentName)}">
                    <xsl:call-template name="XHTML_DOCUMENT"/>
                </nota:document>
            </xsl:for-each>
            <nota:document
                url="{concat($CONTENT_FOLDER_URL, 'package.opf')}">
                <xsl:apply-templates/>
            </nota:document>
        </nota:documents>
    </xsl:template>
    <!-- XHTML: Fix internal references -->
    <xsl:template match="xhtml:a[matches(@href, '^#.+$')]">
        <xsl:variable name="referencedId" as="xs:string"
            select="substring-after(@href, '#')"/>
        <xsl:variable name="referencedElement" as="node()*"
            select="//xhtml:*[@id = $referencedId]"/>
        <xsl:if test="not($referencedElement)">
            <xsl:message>
                <nota:out>
                    <xsl:value-of
                        select="concat('WARNING: Reference to id &quot;',
                                $referencedId, '&quot; does not resolve')"/>
                </nota:out>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="referencedSection" as="node()*"
            select="$referencedElement/ancestor-or-self::xhtml:section
                    [position() = last()]"/>
        <xsl:variable name="referencedFile" as="xs:string"
            select="if (ancestor::node() intersect $referencedSection) then ''
                    else nota:create-document-name($referencedSection)"/>
        <xsl:copy>
            <xsl:attribute name="href"
                select="concat($referencedFile, @href)"/>
            <xsl:apply-templates select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- OPF: Remove references to concat document, add new content documents -->
    <xsl:template
        match="opf:item[@id eq 'concat']|opf:itemref[@idref eq 'concat']"/>
    <xsl:template match="opf:manifest">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each
                select="$CONCAT_DOCUMENT/xhtml:html/xhtml:body/xhtml:section">
                <xsl:variable name="documentName" as="xs:string"
                    select="nota:create-document-name(.)"/>
                <xsl:variable name="id" as="xs:string"
                    select="concat($ID_BASE, '_', $ID_COUNT + position())"/>
                <item xmlns="http://www.idpf.org/2007/opf"
                    href="{$documentName}"
                    id="{$id}"
                    media-type="application/xhtml+xml"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="opf:spine">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each
                select="$CONCAT_DOCUMENT/xhtml:html/xhtml:body/xhtml:section">
                <xsl:variable name="id" as="xs:string"
                    select="concat($ID_BASE, '_', $ID_COUNT + position())"/>
                <itemref xmlns="http://www.idpf.org/2007/opf"
                    idref="{$id}">
                    <xsl:if test="nota:get-primary-type(@epub:type) eq 'cover'">
                        <xsl:attribute name="linear" select="'no'"/>
                    </xsl:if>
                </itemref>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <!-- Functions -->
    <xsl:function name="nota:create-document-name" as="xs:string">
        <xsl:param name="n" as="node()*"/>
        <xsl:variable name="position" as="xs:integer"
            select="count($n/preceding-sibling::xhtml:section) + 1"/>
        <xsl:variable name="number" as="xs:string"
            select="format-number($position, '000')"/>
        <xsl:variable name="type" as="xs:string*"
            select="nota:get-primary-type($n/@epub:type)"/>
        <xsl:value-of
            select="concat($PID, '-', $number, '-', $type, '.xhtml')"/>
    </xsl:function>
    <xsl:function name="nota:get-primary-type" as="xs:string">
        <xsl:param name="typeString" as="xs:string*"/>
        <xsl:variable name="types"
            select="tokenize(normalize-space($typeString), '\s+')"/>
        <xsl:value-of
            select="if (count($types) gt 1)
                    then $types[not(matches(., '(back|body|front)matter'))][1]
                    else $types[1]"/>
    </xsl:function>
</xsl:stylesheet>