<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:saxon="http://saxon.sf.net/"
    exclude-result-prefixes="nota saxon xs"
    version="2.0">
    <xsl:output name="default" method="xml" indent="yes"
        omit-xml-declaration="yes"/>
    <xsl:param name="HEADINGS_TO_EXCLUDE" as="xs:string+"
        select="('Nye lydbøger', 'Nye punktbøger')"/>
    <xsl:param name="OUTPUT_FOLDER_URL" as="xs:string?"/>
    <xsl:strip-space elements="*"/>
    <xsl:template match="*">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="text()">
        <xsl:value-of select="replace(., '\s+', ' ')"/>
    </xsl:template>
    <xsl:template match="/dtbook">
        <xsl:variable name="firstPass" as="element()">
            <docroot>
                <xsl:apply-templates/>
            </docroot>
        </xsl:variable>
        <xsl:message>
            <nota:out>
                <xsl:value-of select="'Creating all.xml'"/>
            </nota:out>
        </xsl:message>
        <xsl:result-document href="{concat($OUTPUT_FOLDER_URL, 'all.xml')}"
            omit-xml-declaration="yes">
            <!--<xsl:copy-of select="$firstPass"/>-->
            <xsl:value-of disable-output-escaping="yes"
                select="replace(saxon:serialize($firstPass, 'default'),
                        '&lt;katalogpost&gt;\s+&lt;/katalogpost&gt;',
                        '&lt;katalogpost&gt;&lt;/katalogpost&gt;')"/>
        </xsl:result-document>
        <xsl:for-each-group group-starting-with="*[nota:starts-file(.)]"
            select="$firstPass/*">
            <xsl:variable name="group" as="element()">
                <docroot>
                    <xsl:copy-of select="current-group()"/>
                </docroot>
            </xsl:variable>
            <xsl:variable name="fileName" as="xs:string"
                select="concat('fil_', format-number(position(), '000'),
                        '.xml')"/>
            <xsl:message>
	            <nota:out>
	                <xsl:value-of select="concat('Creating ', $fileName)"/>
	            </nota:out>
            </xsl:message>
            <xsl:result-document
                href="{concat($OUTPUT_FOLDER_URL, $fileName)}"
                omit-xml-declaration="yes">
                <!--<xsl:copy-of select="$group"/>-->
                <xsl:value-of disable-output-escaping="yes"
                    select="replace(saxon:serialize($group, 'default'),
                            '&lt;/katalogpost&gt;\s+&lt;katalogpost&gt;',
                            '&lt;/katalogpost&gt;&lt;katalogpost&gt;')"/>
            </xsl:result-document>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template match="br">
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>
    <xsl:template match="br[nota:preceded-by-dk5(.)]"/>
    <xsl:template
        match="level[normalize-space(levelhd) = $HEADINGS_TO_EXCLUDE]"/>
    <xsl:template match="levelhd">
        <xsl:variable name="depth" as="xs:integer"
            select="xs:integer(@depth)"/>
        <xsl:element name="{concat('overskrift', $depth)}">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="li">
        <listitem>
            <xsl:apply-templates/>
        </listitem>
    </xsl:template>
    <xsl:template match="list">
        <list>
            <xsl:apply-templates/>
        </list>
    </xsl:template>
    <xsl:template match="p">
        <paranormal>
            <xsl:apply-templates/>
        </paranormal>
    </xsl:template>
    <xsl:template match="p[nota:has-classes(., 'kataloglinie')]">
        <katalogpost>
            <xsl:apply-templates/>
        </katalogpost>
    </xsl:template>
    <xsl:template match="span[@class]">
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
    <xsl:template match="span[nota:has-classes(., 'DK5')]" priority="1"/>
    <xsl:template match="strong">
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
    <xsl:function name="nota:preceded-by-dk5" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="exists($n/preceding-sibling::node()[not(normalize-space()
                    eq '')][1]/self::span[nota:has-classes(., 'DK5')])"/>
    </xsl:function>
    <xsl:function name="nota:starts-file" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="$n/self::overskrift1[following-sibling::*[1]/
                    self::overskrift2] or $n/self::overskrift2
                    [preceding-sibling::*[1]/not(self::overskrift1)]"/>
    </xsl:function>
</xsl:stylesheet>