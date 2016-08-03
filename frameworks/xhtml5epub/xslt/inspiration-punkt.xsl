<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nordic="http://www.mtm.se/epub/"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="xhtml xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="EDITION" as="xs:string"
        select="substring-after(/xhtml:html/xhtml:head/xhtml:title/text(),
                'Inspiration ')"/>
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="xhtml:body">
        <body>
            <section epub:type="titlepage frontmatter">
                <h1 class="title">Inspiration</h1>
                <p>
                    <xsl:value-of select="$EDITION"/>
                </p>
            </section>
            <xsl:apply-templates select="node()|@*"/>
            <xsl:apply-templates mode="MOVE_COLOPHON"
                select="xhtml:section[nota:has-types(., 'colophon')]"/>
        </body>
    </xsl:template>
    <xsl:template match="xhtml:div[nota:has-classes(., 'katalogpost')]">
        <xsl:if test="nota:is-first-post(.)">
            <ul class="list-style-type-none">
                <xsl:apply-templates
                    select="xhtml:p|(following-sibling::xhtml:div
                            [nota:has-classes(., 'katalogpost')] except
                            following-sibling::*[not(nota:has-classes(.,
                            'katalogpost'))][1]/(self::*|
                            following-sibling::node()))/xhtml:p"/>
            </ul>
        </xsl:if>
    </xsl:template>
    <xsl:template
        match="xhtml:div[nota:has-classes(., 'katalogpost')]/xhtml:p">
        <li>
            <xsl:apply-templates select="node()|@* except @class"/>
        </li>
    </xsl:template>
    <xsl:template match="xhtml:section">
        <xsl:choose>
            <xsl:when
                test="matches(xhtml:h1/text(), '^Mest læste (e-|lyd)bøger$')"/>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template
        match="xhtml:section[nota:has-types(., ('frontmatter', 'colophon'))]"/>
    <xsl:template mode="MOVE_COLOPHON"
        match="xhtml:section[nota:has-types(., 'colophon')]">
        <xsl:copy>
            <xsl:attribute name="epub:type"
                select="replace(@epub:type, 'frontmatter', 'backmatter')"/>
            <xsl:apply-templates
                select="node()|@* except @epub:type"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="xhtml:span">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="xhtml:span[@lang|@xml:lang]">
        <span>
            <xsl:copy-of select="@lang|@xml:lang"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:has-types" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@epub:type, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:is-first-post" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="not($n/preceding-sibling::xhtml:*[1]/self::xhtml:div
                    [nota:has-classes(., 'katalogpost')])"/>
    </xsl:function>
</xsl:stylesheet>