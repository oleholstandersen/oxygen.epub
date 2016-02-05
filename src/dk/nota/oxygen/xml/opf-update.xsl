<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns="http://www.idpf.org/2007/opf"
    exclude-result-prefixes="nota opf xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="HREFS" as="xs:string*"/>
    <xsl:param name="TYPES" as="xs:string*"/>
    <xsl:param name="ID_BASE" as="xs:string" select="'import'"/>
    <xsl:param name="ADD_TO_SPINE" as="xs:boolean*"/>
    <xsl:param name="IDS" as="xs:string*">
        <xsl:variable name="count" as="xs:integer"
            select="/opf:package/opf:manifest/count(opf:item[matches(@id,
                    concat('^', $ID_BASE, '_\d+$'))])"/>
        <xsl:sequence
            select="for $i in 1 to count($HREFS)
                    return concat($ID_BASE, '_', $count + $i)"/>
    </xsl:param>
    <xsl:template match="/">
        <nota:document url="{document-uri(.)}">
            <xsl:apply-templates/>
        </nota:document>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="opf:manifest">
        <xsl:copy>
            <xsl:copy-of select="@*|node()"/>
            <xsl:for-each select="$IDS">
                <xsl:variable name="index" as="xs:integer"
                    select="position()"/>
                <item id="{.}"
                    media-type="{$TYPES[$index]}"
                    href="{$HREFS[$index]}"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="opf:spine[$ADD_TO_SPINE]">
        <xsl:copy>
            <xsl:copy-of select="@*|node()"/>
            <xsl:for-each select="$IDS">
                <itemref idref="{.}"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>