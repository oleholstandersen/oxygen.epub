<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="DEPTH" as="xs:integer" select="2"/>
    <xsl:template match="/dtbook">
        <xsl:choose>
            <xsl:when
                test="book/frontmatter/doctitle/matches(., '^Top 10')">
                <ol>
                    <xsl:apply-templates mode="CONVERT_TO_LIST"
                        select="book/frontmatter/level/div"/>
                </ol>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each-group group-by="nota:get-classification(.)"
                    select="book/frontmatter/level/div">
                    <section>
                        <xsl:element name="{concat('h', $DEPTH)}">
                            <xsl:value-of select="current-grouping-key()"/>
                        </xsl:element>
                        <xsl:apply-templates select="current-group()"/>
                    </section>
                </xsl:for-each-group>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="text()|@*">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="*">
    	<xsl:element name="{local-name()}"
    		namespace="http://www.w3.org/1999/xhtml">
    		<xsl:apply-templates select="node()|@*"/>	
   		</xsl:element>
    </xsl:template>
    <xsl:template match="@id"/>
    <xsl:template match="span[@class eq 'DK5']"/>
    <xsl:template match="br[preceding-sibling::*[1]/self::span/@class eq 'DK5']"/>
    <xsl:template mode="CONVERT_TO_LIST" match="div[@class eq 'katalogpost']">
        <li>
            <xsl:apply-templates select="p/node()"/>
        </li>
    </xsl:template>
    <xsl:function name="nota:get-classification" as="xs:string">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="if ($n/p/span[@class eq 'DK5']/matches(text(), '^99'))
                    then 'Erindringer og biografier'
                    else if ($n/p/span[@class eq 'DK5']) then 'Faglitteratur'
                    else 'Skønlitteratur'"/>
    </xsl:function>
</xsl:stylesheet>