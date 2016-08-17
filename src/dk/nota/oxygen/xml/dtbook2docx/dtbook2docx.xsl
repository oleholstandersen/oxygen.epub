<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:file="http://expath.org/ns/file"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:import href="dtbook2docx-document.xsl"/>
    <xsl:import href="dtbook2docx-endnotes.xsl"/>
    <xsl:import href="dtbook2docx-footnotes.xsl"/>
    <xsl:import href="dtbook2docx-numbering.xsl"/>
    <xsl:import href="dtbook2docx-relationships.xsl"/>
    <xsl:import href="dtbook2docx-styles.xsl"/>
    <xsl:param name="BookNumber" select="0"/>
    <xsl:param name="outputDirectory"
        select="replace(document-uri(/), '/[^/]$', '/docx')"/>
    <xsl:param name="overwriteOutput" as="xs:boolean" select="true()"/>
    <xsl:param name="StampId" select="0"/>
    <xsl:output method="xml" indent="yes"/>
    <!-- KEYS -->
    <xsl:key name="notes" match="note" use="@id"/>
    <!-- TEMPLATES -->
    <xsl:template match="node()|@*"/>
    <xsl:template match="/dtbook">
        <!-- OUTPUT [Content_Types].xml -->
        <xsl:result-document href="{encode-for-uri('[Content_Types].xml')}">
            <Types
                xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="jpg" ContentType="image/jpeg"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Default Extension="rels"
                    ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Override PartName="/docProps/custom.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.custom-properties+xml"/>
                <Override PartName="/word/document.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                <Override PartName="/word/endnotes.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.endnotes+xml"/>
                <Override PartName="/word/footnotes.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml"/>
                <Override PartName="/word/numbering.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
                <Override PartName="/word/settings.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
                <Override PartName="/word/styles.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
            </Types>
        </xsl:result-document>
        <!-- OUTPUT _rels/.rels -->
        <xsl:result-document href="_rels/.rels">
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    Target="word/document.xml"/>
                <Relationship Id="rId2"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties"
                    Target="docProps/custom.xml"/>
            </Relationships>
        </xsl:result-document>
        <!-- OUTPUT docProps/custom.xml -->
        <xsl:result-document href="docProps/custom.xml">
            <Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties"
                xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
                <property fmtid="{{D5CDD505-2E9C-101B-9397-08002B2CF9AE}}"
                    pid="2" name="Identifier">
                    <vt:lpwstr>DK-NOTA-<xsl:copy-of select="$BookNumber"/></vt:lpwstr>
                </property>
                <property fmtid="{{D5CDD505-2E9C-101B-9397-08002B2CF9AE}}"
                    pid="3" name="Copyright">
                    <vt:lpwstr>Vær opmærksom på Notas vilkår for brug</vt:lpwstr>
                </property>
                <property fmtid="{{D5CDD505-2E9C-101B-9397-08002B2CF9AE}}"
                    pid="4" name="Stamp">
                    <vt:lpwstr><xsl:copy-of select="$StampId"/></vt:lpwstr>
                </property>
            </Properties>
        </xsl:result-document>
        <!-- OUTPUT word/document.xml -->
        <xsl:result-document href="word/document.xml">
            <xsl:apply-templates mode="DOCUMENT" select="book"/>
        </xsl:result-document>
        <!-- OUTPUT word/endnotes.xml -->
        <xsl:result-document href="word/endnotes.xml">
            <xsl:apply-templates mode="ENDNOTES" select="book"/>
        </xsl:result-document>
        <!-- OUTPUT word/footnotes.xml -->
        <xsl:result-document href="word/footnotes.xml">
            <xsl:apply-templates mode="FOOTNOTES" select="book"/>
        </xsl:result-document>
        <!-- OUTPUT word/numbering.xml -->
        <xsl:result-document href="word/numbering.xml">
            <xsl:apply-templates mode="NUMBERING" select="book"/>
        </xsl:result-document>
        <!-- OUTPUT word/settings.xml -->
        <xsl:result-document href="word/settings.xml">
            <w:settings
                xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                <w:characterSpacingControl w:val="doNotCompress"/>
                <w:proofState w:spelling="clean" w:grammar="clean"/>
                <w:zoom w:percent="100"/>
                <w:endnotePr>
                    <w:endnote w:id="-1"/>
                    <w:endnote w:id="0"/>
                </w:endnotePr>
                <w:footnotePr>
                    <w:footnote w:id="-1"/>
                    <w:footnote w:id="0"/>
                </w:footnotePr>
            </w:settings>
        </xsl:result-document>
        <!-- OUTPUT word/styles.xml -->
        <xsl:result-document href="word/styles.xml">
            <xsl:apply-templates mode="STYLES" select="book"/>
        </xsl:result-document>
        <!-- OUTPUT word/_rels/document.xml.rels -->
        <xsl:result-document href="word/_rels/document.xml.rels">
            <xsl:apply-templates mode="RELATIONSHIPS" select="book"/>
        </xsl:result-document>
        <!-- OUTPUT images -->
        <!--<xsl:for-each-group select="book//img" group-by="@src">
            <xsl:variable name="source" select="resolve-uri(@src)"/>
            <xsl:result-document href="word/media/{@src}">
                <img>
                    <xsl:value-of select="$source"/>
                </img>
            </xsl:result-document>
        </xsl:for-each-group>-->
    </xsl:template>
</xsl:stylesheet>