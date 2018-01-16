<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="OUTPUT_FOLDER_URL" as="xs:string?"/>
    <xsl:template name="NCC_HTML_HEAD">
    </xsl:template>
    <xsl:variable name="OUTPUT" as="node()+">
        <xsl:for-each-group select="/html:html/html:body/html:*"
            group-starting-with="html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
            <xsl:variable name="fileName" as="xs:string"
                select="concat('page-', format-number(position(), '0000'))"/>
            <xsl:variable name="id" as="xs:string"
                select="if (current-group()[1]/@id) then current-group()[1]/@id
                        else generate-id(current-group()[1])"/>
            <xsl:element name="{current-group()[1]/local-name()}">
                <xsl:copy-of select="current-group()[1]/@*"/>
                <a href="{concat($fileName, '.smil#', $id)}">
                    <xsl:copy-of select="current-group()[1]/text()"/>
                </a>
            </xsl:element>
            <html>
                <xsl:copy-of select="/html:html/@*|/html:html/html:head"/>
                <body>
                    <xsl:apply-templates mode="HTML" select="current-group()">
                        <xsl:with-param name="smilFileName" as="xs:string"
                            select="concat($fileName, '.smil')"/>
                    </xsl:apply-templates>
                </body>
            </html>
            <smil xmlns="">
                <body>
                    <xsl:apply-templates mode="SMIL" select="current-group()">
                        <xsl:with-param name="htmlFileName" as="xs:string"
                            select="concat($fileName, '.xhtml')"/>
                    </xsl:apply-templates>
                </body>
            </smil>
        </xsl:for-each-group>
    </xsl:variable>
    <xsl:template match="/html:html">
        <xsl:result-document
            href="{concat($OUTPUT_FOLDER_URL, 'ncc.html')}">
            <html>
                <head>
                    <xsl:copy-of select="/html:html/html:head/html:title"/>
                </head>
                <body>
                    <xsl:copy-of
                        select="$OUTPUT[not(self::html:html|self::smil)]"/>
                </body>
            </html>
        </xsl:result-document>
        <xsl:for-each select="$OUTPUT[self::html:html]">
            <xsl:result-document href="{concat($OUTPUT_FOLDER_URL}"></xsl:result-document>
        </xsl:for-each>
        <xsl:comment>SMIL files</xsl:comment>
        <xsl:copy-of select="$OUTPUT[self::smil]"/>
    </xsl:template>
    <xsl:template mode="HTML"
        match="html:div|html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
        <xsl:param name="smilFileName" as="xs:string?"/>
        <xsl:param name="id" as="xs:string" select="generate-id()"/>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <a href="{concat($smilFileName, '#', $id)}">
                <xsl:copy-of select="node()"/>
            </a>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="HTML SMIL" match="node()"/>
    <xsl:template mode="SMIL"
        match="html:div|html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
        <xsl:param name="htmlFileName" as="xs:string?"/>
        <xsl:variable name="id" as="xs:string" select="generate-id()"/>
        <par xmlns="" endsync="last">
            <text src="{concat($htmlFileName, '#', $id)}" id="{$id}"/>
        </par>
        <xsl:apply-templates mode="SMIL" select="html:div">
            <xsl:with-param name="htmlFileName" as="xs:string?"
                select="$htmlFileName"/>
        </xsl:apply-templates>
    </xsl:template>
</xsl:stylesheet>