<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" indent="no"/>
    <xsl:template match="nota:documents">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="nota:document">
        <xsl:variable name="fileName" as="xs:string"
            select="tokenize(@url, '/')[position() = last()]"/>
        <xsl:message>
            <nota:out>
                <xsl:value-of select="concat('Writing document ', $fileName)"/>
            </nota:out>
            <nota:systemid>
                <xsl:value-of select="@url"/>
            </nota:systemid>
        </xsl:message>
        <xsl:result-document href="{@url}">
            <xsl:if test="xhtml:html">
                <xsl:value-of disable-output-escaping="yes"
                    select="'&#xa;&lt;!DOCTYPE html&gt;&#xa;'"/>
            </xsl:if>
            <xsl:copy-of select="node()"/>
        </xsl:result-document>
    </xsl:template>
</xsl:stylesheet>