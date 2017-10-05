<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="div level"/>
    <xsl:variable name="EDITION" as="xs:string">
        <xsl:variable name="year" as="xs:string?"
            select="substring(/dtbook/head/meta[@name eq 'dc:identifier']/
                    @content, 5, 4)"/>
        <xsl:variable name="issue" as="xs:string?"
            select="substring(/dtbook/head/meta[@name eq 'dc:identifier']/
                    @content, 12, 1)"/>
        <xsl:value-of select="concat('nr. ', $issue, ', ', $year)"/>
    </xsl:variable>
    <xsl:template mode="#default STRIP_LEADING_WHITESPACE" match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="book">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <frontmatter>
                <xsl:copy-of select="frontmatter/doctitle"/>
                <level depth="1" class="title">
                    <levelhd depth="1" class="title">Inspiration</levelhd>
                    <p><xsl:value-of select="$EDITION"/></p>
                </level>
            </frontmatter>
            <xsl:apply-templates select="bodymatter"/>
            <xsl:choose>
                <xsl:when test="rearmatter">
                    <xsl:apply-templates select="rearmatter"/>    
                </xsl:when>
                <xsl:otherwise>
                    <rearmatter>
                        <xsl:copy-of
                            select="frontmatter/level[nota:has-classes(.,
                                    'colophon')]"/>
                    </rearmatter>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="br[nota:preceded-by-dk5(.)]"/>
    <xsl:template match="div[nota:has-classes(., 'katalogpost')]">
        <xsl:if test="nota:is-first-cat-post(.)">
            <list type="ul" bullet="none">
                <xsl:apply-templates
                    select="p|(following-sibling::div[nota:has-classes(.,
                            'katalogpost')] except following-sibling::*[not(
                            nota:has-classes(., 'katalogpost'))][1]/(self::*|
                            following-sibling::node()))/p"/>
            </list>
        </xsl:if>
    </xsl:template>
    <xsl:template match="div[nota:has-classes(., 'katalogpost')]/p">
        <xsl:variable name="firstPass" as="element()">
            <li>
                <xsl:apply-templates select="node()|@* except @class"/>
            </li>
        </xsl:variable>
        <xsl:apply-templates mode="STRIP_LEADING_WHITESPACE"
            select="$firstPass"/>
    </xsl:template>
    <xsl:template
        match="span[nota:has-classes(., ('OEE', 'OEL', 'OEP', 'typedescription'))]//a">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="level[levelhd]">
        <xsl:variable name="text" as="xs:string"
            select="normalize-space(string-join(levelhd//text(), ''))"/>
        <xsl:choose>
            <xsl:when test="matches($text, '^Nye (e-|lyd)bøger$')"/>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="li">
        <xsl:variable name="firstPass">
            <xsl:next-match/>
        </xsl:variable>
        <xsl:apply-templates mode="STRIP_LEADING_WHITESPACE"
            select="$firstPass"/>
    </xsl:template>
    <xsl:template mode="STRIP_LEADING_WHITESPACE"
        match="text()[not(preceding::text() intersect ancestor::li[1]//text())]">
        <xsl:value-of select="replace(., '^\s+', '')"/>
    </xsl:template>
    <xsl:template match="rearmatter">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <xsl:copy-of
                select="parent::book/frontmatter/level[nota:has-classes(.,
                        'colophon')]"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="span">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="span[@lang]">
        <span>
            <xsl:copy-of select="@lang"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="span[nota:has-classes(., 'DK5')]"/>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:is-first-cat-post" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="not($n/preceding-sibling::*[1]/self::div
            [nota:has-classes(., 'katalogpost')])"/>
    </xsl:function>
    <xsl:function name="nota:preceded-by-dk5" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="exists($n/preceding-sibling::node()[not(normalize-space() eq
            		'')][1]/self::span[nota:has-classes(., 'DK5')])"/>
    </xsl:function>
</xsl:stylesheet>