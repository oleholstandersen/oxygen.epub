<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="OUTPUT_FOLDER_URL" as="xs:string?"/>
    <xsl:template match="/html:html">
        <xsl:apply-templates select="html:head|html:body"/>
    </xsl:template>
    <xsl:template match="html:area">
        <xsl:variable name="coordinates" as="xs:integer+"
            select="for $i in tokenize(@coords, ',')
                    return $i cast as xs:integer"/>
        <xsl:variable name="height" as="xs:integer"
            select="$coordinates[4] - $coordinates[2]"/>
        <xsl:variable name="width" as="xs:integer"
            select="$coordinates[3] - $coordinates[1]"/>
        <xsl:variable name="style" as="xs:string"
            select="concat('left:', $coordinates[1], 'px;top:', $coordinates[2],
                    'px;width:', $width, 'px;height:', $height, 'px;')"/>
        <div class="area" style="{$style}"/>
    </xsl:template>
    <xsl:template match="html:body">
        <xsl:apply-templates select="html:map">
            <xsl:sort order="descending"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="html:head">
        <xsl:variable name="pageNumber" as="xs:string?"
            select="replace(html:title/text(), '^image', '')"/>
        <div class="page-normal" epub:type="pagebreak" title="{$pageNumber}"/>
    </xsl:template>
    <xsl:template match="html:map">
        <xsl:variable name="image" as="element(html:img)"
            select="following-sibling::html:img"/>
        <xsl:variable name="pageNumber" as="xs:string?"
            select="replace(preceding::html:head[1]/html:title/text(),
                    '^image', '')"/>
        <xsl:variable name="style" as="xs:string"
            select="concat('height:', $image/@height, 'px;width:',
                    $image/@width, 'px;')"/>
        <xsl:variable name="title" as="xs:string"
            select="preceding::html:head[1]/html:title/text()"/>
        <xsl:message>
            <nota:image>
                <xsl:value-of
                	select="resolve-uri($image/@src, $OUTPUT_FOLDER_URL)"/>
            </nota:image>
       	</xsl:message>
        <div id="{$title}" class="page" style="{$style}">
            <img src="{concat('images/', $image/@src)}" class="page"
            	style="{$style}" alt="{concat('Side ', $pageNumber)}"/>
            <xsl:apply-templates select="html:area[not(@shape eq 'default')]"/>
        </div>            
    </xsl:template>
</xsl:stylesheet>