<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="dc epub nota opf saxon xhtml xs"
    version="2.0">
    <xsl:output name="default" method="xml" indent="yes"
        saxon:indent-spaces="0" omit-xml-declaration="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="CONTENT_FOLDER_URL" as="xs:string"
        select="replace(document-uri(/), '/[^/]*?$', '/')"/>
    <xsl:param name="NAVIGATION_DOCUMENT" as="document-node()?"
        select="document(concat($CONTENT_FOLDER_URL, 'nav.xhtml'))"/>
    <xsl:param name="OUTPUT_FOLDER_URL" as="xs:string"
        select="concat(replace(document-uri(/), '^zip:|[^/]*!.*?$', ''),
                'tryk/')"/>
    <xsl:variable name="HEADINGS_TO_EXCLUDE" as="xs:string+"
        select="('Nye lydbøger', 'Nye punktbøger')"/>
    <xsl:variable name="CONTENT_DOCUMENTS_FIRST_PASS" as="node()*">
        <xsl:for-each
            select="/opf:package/opf:spine/opf:itemref[@idref ne 'concat']">
            <xsl:variable name="item" as="node()?"
                select="//opf:item[@id = current()/@idref]"/>
            <xsl:variable name="reference" as="xs:string?"
                select="$item/@href"/>
            <xsl:variable name="navEntry" as="element()?"
                select="($NAVIGATION_DOCUMENT/xhtml:html/xhtml:body/xhtml:nav
                        [@epub:type eq 'toc']//xhtml:a[matches(@href, concat(
                        '^', $reference))])[1]"/>
            <xsl:variable name="depthModifier" as="xs:integer"
                select="if ($navEntry)
                        then $navEntry/count(ancestor::xhtml:li) - 1
                        else 0"/>
            <xsl:if test="$item/@media-type = 'application/xhtml+xml'">
                <xsl:variable name="documentUrl" as="xs:string"
                    select="concat($CONTENT_FOLDER_URL, $reference)"/>
                <xsl:apply-templates
                    select="document($documentUrl)/xhtml:html/xhtml:body">
                    <xsl:with-param name="depthModifier" as="xs:integer"
                        tunnel="yes" select="$depthModifier"/>
                </xsl:apply-templates>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="COMPLETE" as="element()">
        <docroot>
            <xsl:copy-of select="$CONTENT_DOCUMENTS_FIRST_PASS"/>
        </docroot>
    </xsl:variable>
    <xsl:template match="/opf:package">
        <xsl:result-document href="{concat($OUTPUT_FOLDER_URL, 'All.xml')}">
            <xsl:text>&#xa;</xsl:text>
            <xsl:value-of disable-output-escaping="yes"
                select="replace(saxon:serialize($COMPLETE, 'default'),
                        '&lt;katalogpost&gt;\s+&lt;/katalogpost&gt;',
                        '&lt;katalogpost&gt;&lt;/katalogpost&gt;')"/>
        </xsl:result-document>
        <xsl:for-each-group select="$COMPLETE/*"
            group-starting-with="*[nota:starts-file(.)]">
            <xsl:variable name="group" as="element()">
                <docroot>
                    <xsl:copy-of select="current-group()"/>
                </docroot>
            </xsl:variable>
            <xsl:variable name="fileName" as="xs:string"
                select="concat('fil_', format-number(position(), '000'),
                        '.xml')"/>
            <xsl:result-document
                href="{concat($OUTPUT_FOLDER_URL, $fileName)}">
                <xsl:text>&#xa;</xsl:text>
                <xsl:value-of disable-output-escaping="yes"
                    select="replace(saxon:serialize($group, 'default'),
                            '&lt;/katalogpost&gt;\s+&lt;katalogpost&gt;',
                            '&lt;/katalogpost&gt;&lt;katalogpost&gt;')"/>
            </xsl:result-document>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template match="xhtml:*">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="text()">
        <xsl:value-of select="replace(., '\s+', ' ')"/>
    </xsl:template>
    <xsl:template match="xhtml:body">
        <xsl:choose>
            <xsl:when
                test="normalize-space(xhtml:h1) = $HEADINGS_TO_EXCLUDE"/>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xhtml:br">
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>
    <xsl:template match="xhtml:*[matches(local-name(), '^h\d$')]">
        <xsl:param name="depthModifier" as="xs:integer" tunnel="yes"
            select="0"/>
        <xsl:variable name="depth" as="xs:integer"
            select="xs:integer(substring(local-name(), 2))"/>
        <xsl:element name="{concat('overskrift', $depth + $depthModifier)}">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="xhtml:li">
        <listitem>
            <xsl:apply-templates/>
        </listitem>
    </xsl:template>
    <xsl:template match="xhtml:ol|xhtml:ul">
        <list>
            <xsl:apply-templates/>
        </list>
    </xsl:template>
    <xsl:template match="xhtml:p">
        <paranormal>
            <xsl:apply-templates/>
        </paranormal>
    </xsl:template>
    <xsl:template match="xhtml:p[nota:has-classes(., 'kataloglinie')]">
        <katalogpost>
            <xsl:apply-templates/>
        </katalogpost>
    </xsl:template>
    <xsl:template match="xhtml:span[@class]">
        <xsl:variable name="elementName" as="xs:string"
            select="nota:map-class-to-element-name(@class)"/>
        <xsl:choose>
            <xsl:when test="string-length($elementName) gt 0">
                <xsl:element name="{$elementName}">
                    <xsl:apply-templates/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xhtml:strong">
        <bold>
            <xsl:apply-templates/>
        </bold>
    </xsl:template>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:map-class-to-element-name" as="xs:string">
        <xsl:param name="class" as="xs:string"/>
        <xsl:variable name="classes" as="xs:string*"
            select="tokenize($class, '\s+')"/>
        <xsl:value-of
            select="if ($classes = 'kataloglinie') then 'kataloglinie'
                    else if ($classes = 'titellinie') then 'titellinie'
                    else if ($classes = 'note') then 'note'
                    else if ($classes = 'seriesamhoerende')
                    then 'seriesamhoerende'
                    else if ($classes = 'typedescription')
                    then 'typedescription'
                    else if ($classes = 'masternummer') then 'masternummer'
                    else if ($classes = 'indlaeser') then 'indlaeser'
                    else if ($classes = ('OEEyear', 'OEPyear', 'OELyear',
                    'ekspresInfo', 'prodyear')) then 'prodyear'
                    else if ($classes = 'playingtime') then 'playingtime'
                    else if ($classes = 'seriepart') then 'seriepart'
                    else if ($classes = 'DK5') then 'DK5'
                    else if ($classes = ('addinfo', 'otheredition'))
                    then 'addinfo'
                    else if ($classes = ('bind', 'OEPbind')) then 'pbind'
                    else if ($classes = ('OEE', 'OEP', 'OEL'))
                    then 'othereditions'
                    else ''"/>
    </xsl:function>
    <xsl:function name="nota:starts-file" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="$n/self::overskrift1[following-sibling::*[1]/
                    self::overskrift2] or $n/self::overskrift2
                    [preceding-sibling::*[1]/not(self::overskrift1)]"/>
    </xsl:function>
</xsl:stylesheet>