<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="div level"/>
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="br[nota:preceded-by-dk5(.)]"/>
    <xsl:template match="frontmatter">
        <frontmatter>
            <xsl:copy-of select="doctitle"/>
            <level depth="1" class="title">
                <levelhd depth="1" class="title"><xsl:value-of
                    select="/dtbook/head/title"/></levelhd>
                <xsl:if test="level[nota:has-classes(., 'colophon')]">
                    <div class="kolofon">
                        <xsl:copy-of
                            select="level[nota:has-classes(., 'colophon')]/*"/>
                    </div>
                </xsl:if>
            </level>
        </frontmatter>
    </xsl:template>
    <xsl:template match="span[nota:has-classes(., 'DK5')]"/>
    <xsl:template
        match="span[nota:has-classes(., ('OEE', 'OEP', 'OEL', 'typedescription'))]">
        <strong>
            <xsl:apply-templates/>
        </strong>
    </xsl:template>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:preceded-by-dk5" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="exists($n/preceding-sibling::node()[not(normalize-space()
                    eq '')][1]/self::span[nota:has-classes(., 'DK5')])"/>
    </xsl:function>
</xsl:stylesheet>