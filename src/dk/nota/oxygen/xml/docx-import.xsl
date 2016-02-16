<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    xmlns:rel="http://schemas.openxmlformats.org/package/2006/relationships"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="a dc epub nota opf pic r rel w wp xhtml xs"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="SPLIT_DOCUMENTS" as="xs:boolean" select="false()"/>
    <xsl:param name="WORD_FOLDER_URLS" as="xs:string*"/>
    <xsl:variable name="OPF_LANGUAGE" as="xs:string*"
        select="/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:variable name="OPF_PID" as="xs:string*"
        select="/opf:package/opf:metadata/dc:identifier/text()"/>
    <xsl:variable name="OPF_TITLE" as="xs:string*"
        select="/opf:package/opf:metadata/dc:title/text()"/>
    <xsl:variable name="ROOT" as="node()" select="/"/>
    <xsl:variable name="FIRST_PASS" as="node()*">
        <xsl:for-each select="$WORD_FOLDER_URLS">
            <xsl:variable name="document" as="document-node()*"
                select="document(concat(., 'document.xml'))"/>
            <xsl:variable name="numbering" as="document-node()*"
                select="document(concat(., 'numbering.xml'))"/>
            <xsl:variable name="relationships" as="document-node()*"
                select="document(concat(., '_rels/document.xml.rels'))"/>
            <xsl:variable name="styles" as="document-node()*"
                select="document(concat(., 'styles.xml'))"/>
            <xsl:apply-templates select="$document/w:document/w:body">
                <xsl:with-param name="numbering" tunnel="yes"
                    select="$numbering"/>
                <xsl:with-param name="relationships" tunnel="yes"
                    select="$relationships"/>
                <xsl:with-param name="styles" tunnel="yes" select="$styles"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:variable>
    <xsl:template match="/xhtml:html">
        <nota:documents>
            <nota:document url="{document-uri(/)}">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:copy-of select="xhtml:head"/>
                    <body>
                        <xsl:copy-of select="xhtml:body/@*"/>
                        <xsl:apply-templates mode="SECOND_PASS"
                            select="$FIRST_PASS"/>
                    </body>
                </xsl:copy>
            </nota:document>
        </nota:documents>
    </xsl:template>
    <xsl:template match="/opf:package">
        <xsl:variable name="id" as="xs:string">
            <xsl:variable name="count" as="xs:integer"
                select="opf:manifest/
                        count(opf:item[matches(@id, '^import_\d$')])"/>
            <xsl:value-of select="concat('import_', $count + 1)"/>
        </xsl:variable>
        <xsl:variable name="fileName" as="xs:string">
            <xsl:variable name="count" as="xs:integer"
                select="opf:manifest/
                        count(opf:item[@id = //opf:itemref/@idref])"/>
            <xsl:value-of
                select="concat($OPF_PID, '-', format-number($count + 1, '000'),
                        '-chapter.xhtml')"/>
        </xsl:variable>
        <xsl:variable name="outputUrl" as="xs:string"
            select="replace(document-uri(/), '/[^/]*$', concat('/',
                    $fileName))"/>
        <nota:documents>
            <nota:document url="{$outputUrl}">
                <html xmlns:epub="http://www.idpf.org/2007/ops"
                    xmlns:nordic="http://www.mtm.se/epub/"
                    epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
                    lang="{$OPF_LANGUAGE}"
                    xml:lang="{$OPF_LANGUAGE}">
                    <head>
                        <meta charset="UTF-8"/>
                        <title>
                            <xsl:value-of select="$OPF_TITLE"/>
                        </title>
                        <meta name="dc:identifier" content="{$OPF_PID}"/>
                        <meta name="viewport" content="width=device-width"/>
                        <xsl:for-each
                            select="opf:manifest/opf:item[@media-type eq
                                    'text/css']">
                            <link rel="stylesheet" type="text/css"
                                href="{@href}"/>
                        </xsl:for-each>
                    </head>
                    <body epub:type="bodymatter chapter">
                        <xsl:apply-templates mode="SECOND_PASS"
                            select="$FIRST_PASS"/>
                    </body>
                </html>
            </nota:document>
            <nota:document url="{document-uri(/)}">
                <xsl:copy xmlns="http://www.idpf.org/2007/opf">
                    <xsl:copy-of select="@*|opf:metadata"/>
                    <manifest>
                        <xsl:copy-of select="opf:manifest/(@*|*)"/>
                        <item id="{$id}" media-type="application/xhtml+xml"
                            href="{$fileName}"/>
                    </manifest>
                    <spine>
                        <xsl:copy-of select="opf:spine/(@*|*)"/>
                        <itemref idref="{$id}"/>
                    </spine>
                </xsl:copy>
            </nota:document>
        </nota:documents>
    </xsl:template>
    <xsl:template match="/opf:package[$SPLIT_DOCUMENTS]">
        <xsl:variable name="documents" as="node()*">
            <xsl:for-each select="$FIRST_PASS">
                <xsl:variable name="id" as="xs:string">
                    <xsl:variable name="count" as="xs:integer"
                        select="$ROOT/opf:package/opf:manifest/
                                count(opf:item[matches(@id, '^import_\d$')])"/>
                    <xsl:value-of
                        select="concat('import_', $count + position())"/>
                </xsl:variable>
                <xsl:variable name="fileName" as="xs:string">
                    <xsl:variable name="count" as="xs:integer"
                        select="$ROOT/opf:package/opf:manifest/
                                count(opf:item[@id = //opf:itemref/@idref])"/>
                    <xsl:value-of
                        select="concat($OPF_PID, '-', format-number($count
                                + position(), '000'), '-chapter.xhtml')"/>
                </xsl:variable>
                <xsl:variable name="outputUrl" as="xs:string"
                    select="replace(document-uri($ROOT), '/[^/]*$', concat('/',
                            $fileName))"/>
                <nota:document url="{$outputUrl}" id="{$id}" name="{$fileName}">
                    <html xmlns:epub="http://www.idpf.org/2007/ops"
                        xmlns:nordic="http://www.mtm.se/epub/"
                        epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
                        lang="{$OPF_LANGUAGE}"
                        xml:lang="{$OPF_LANGUAGE}">
                        <head>
                            <meta charset="UTF-8"/>
                            <title>
                                <xsl:value-of select="$OPF_TITLE"/>
                            </title>
                            <meta name="dc:identifier" content="{$OPF_PID}"/>
                            <meta name="viewport" content="width=device-width"/>
                            <xsl:for-each
                                select="$ROOT/opf:package/opf:manifest/opf:item
                                        [@media-type eq 'text/css']">
                                <link rel="stylesheet" type="text/css"
                                    href="{@href}"/>
                            </xsl:for-each>
                        </head>
                        <body epub:type="bodymatter chapter">
                            <xsl:apply-templates mode="SECOND_PASS"
                                select="@*|xhtml:p
                                        [not(preceding-sibling::nota:hd)]|
                                        nota:hd[@depth = 1]"/>
                        </body>
                    </html>
                </nota:document>
            </xsl:for-each>
        </xsl:variable>
        <nota:documents>
            <xsl:copy-of select="$documents"/>
            <nota:document url="{document-uri(/)}">
                <xsl:copy xmlns="http://www.idpf.org/2007/opf">
                    <xsl:copy-of select="@*|opf:metadata"/>
                    <manifest>
                        <xsl:copy-of select="opf:manifest/(@*|*)"/>
                        <xsl:for-each select="$documents">
                            <item id="{@id}" media-type="application/xhtml+xml"
                                href="{@name}"/>
                        </xsl:for-each>
                    </manifest>
                    <spine>
                        <xsl:copy-of select="opf:spine/(@*|*)"/>
                        <xsl:for-each select="$documents">
                            <itemref idref="{@id}"/>
                        </xsl:for-each>
                    </spine>
                </xsl:copy>
            </nota:document>  
        </nota:documents>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    <xsl:template match="w:body">
        <body>
            <xsl:apply-templates/>
        </body>
    </xsl:template>
    <xsl:template match="w:p">
        <xsl:param name="numbering" as="document-node()*" tunnel="yes"/>
        <xsl:param name="styles" as="document-node()*" tunnel="yes"/>
        <xsl:variable name="styleName" as="xs:string*"
            select="w:pPr/w:pStyle/@w:val"/>
        <xsl:variable name="styleItem" as="node()*"
            select="$styles/w:styles/w:style
                        [@w:styleId eq $styleName]"/>
        <xsl:variable name="outlineLevel" as="xs:integer"
            select="if (w:pPr/w:outlineLvl)
                    then xs:integer(w:pPr/w:outlineLvl/@w:val)
                    else if ($styleItem/w:pPr/w:outlineLvl)
                    then xs:integer($styleItem/w:pPr/w:outlineLvl/@w:val)
                    else -1"/>
        <xsl:variable name="numPr" as="node()*" select="w:pPr/w:numPr"/>
        <xsl:choose>
            <xsl:when test="$outlineLevel gt -1">
                <nota:hd depth="{$outlineLevel + 1}">
                    <xsl:apply-templates/>
                </nota:hd>
            </xsl:when>
            <xsl:when test="$numPr">
                <xsl:variable name="depth" as="xs:integer*"
                    select="xs:integer($numPr/w:ilvl/@w:val)"/>
                <xsl:variable name="abstractNumberId" as="xs:string*"
                    select="$numbering/w:numbering/w:num[@w:numId = $numPr/
                            w:numId/@w:val]/w:abstractNumId/@w:val"/>
                <xsl:variable name="numberFormat" as="xs:string*"
                    select="$numbering/w:numbering/w:abstractNum
                            [@w:abstractNumId = $abstractNumberId]/w:lvl
                            [@w:ilvl = $depth]/w:numFmt/@w:val"/>
                <li depth="{$depth}" format="{$numberFormat}">
                    <xsl:apply-templates/>
                </li>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:apply-templates/>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="w:p[not(w:r/w:t)]"/>
    <xsl:template match="w:r">
        <xsl:variable name="properties" as="node()*"
            select="w:rPr/(w:b|w:i)"/>
        <xsl:for-each select="w:t/text()">
            <xsl:call-template name="CONVERT_RUN">
                <xsl:with-param name="properties" select="$properties"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="w:tbl">
        <table>
            <xsl:apply-templates/>
        </table>
    </xsl:template>
    <xsl:template match="w:tc">
        <xsl:variable name="position" as="xs:integer"
            select="position()"/>
        <xsl:variable name="colSpan" as="xs:integer"
            select="if (w:tcPr/w:gridSpan) then w:tcPr/w:gridSpan/@w:val
                    else 0"/>
        <xsl:variable name="rowSpan" as="xs:integer"
            select="if (w:tcPr/w:vMerge/@w:val eq 'restart')
                    then nota:count-spanned-rows(parent::w:tr, position())
                    else 0"/>
        <td>
            <xsl:if test="$colSpan gt 0">
                <xsl:attribute name="colspan" select="$colSpan"/>
            </xsl:if>
            <xsl:if test="$rowSpan gt 0">
                <xsl:attribute name="rowspan"
                    select="$rowSpan"/>
            </xsl:if>
            <xsl:apply-templates/>
        </td>
    </xsl:template>
    <xsl:template match="w:tc[w:tcPr/w:vMerge[not(@w:val eq 'restart')]]"/>
    <xsl:template match="w:tr">
        <tr>
            <xsl:apply-templates select="w:tc"/>
        </tr>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="SECOND_PASS" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="xhtml:body">
        <xsl:apply-templates mode="SECOND_PASS"
            select="@*|xhtml:p[not(preceding-sibling::nota:hd)]|
                    nota:hd"/>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="xhtml:em">
        <xsl:copy>
            <xsl:for-each select="nota:expand-inline(.)">
                <xsl:apply-templates mode="SECOND_PASS"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="xhtml:em[preceding-sibling::node()[1]/self::xhtml:em]"/>
    <xsl:template mode="SECOND_PASS NEST_SECTIONS" match="nota:hd">
        <xsl:variable name="depth" as="xs:integer"
            select="xs:integer(@depth)"/>
        <section>
            <xsl:element name="{concat('h', $depth)}">
                <xsl:apply-templates mode="SECOND_PASS"
                    select="@*|node()"/>
            </xsl:element>
            <xsl:apply-templates mode="SECOND_PASS"
                select="following-sibling::* except
                        following-sibling::nota:hd[1]/
                        (self::nota:hd|following-sibling::*)"/>
            <xsl:apply-templates mode="NEST_SECTIONS"
                select="following-sibling::nota:hd[@depth - $depth = 1] except
                        following-sibling::nota:hd[@depth &lt;= $depth][1]/
                        (self::nota:hd|following-sibling::*)"/>
        </section>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="nota:hd[@depth &gt; preceding-sibling::nota:hd/@depth]"/>
    <xsl:template mode="SECOND_PASS" match="nota:hd/@depth"/>
    <xsl:template mode="SECOND_PASS THIRD_PASS" match="xhtml:li">
        <xsl:variable name="listElementName" as="xs:string"
            select="if (@format eq 'bullet') then 'ul' else 'ol'"/>
        <xsl:variable name="depth" as="xs:integer" select="xs:integer(@depth)"/>
        <xsl:element name="{$listElementName}">
            <xsl:for-each
                select="self::xhtml:li|following-sibling::xhtml:li except
                        following-sibling::*[not(self::xhtml:li)][1]/
                        (self::xhtml:li|following-sibling::xhtml:li)">
                <xsl:copy>
                    <xsl:apply-templates mode="SECOND_PASS"
                        select="@*|node()"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="xhtml:li[preceding-sibling::node()[1]/self::xhtml:li]"/>
    <xsl:template mode="SECOND_PASS" match="xhtml:li/@depth|xhtml:li/@format"/>
    <xsl:template mode="SECOND_PASS" match="xhtml:strong">
        <xsl:copy>
            <xsl:for-each select="nota:expand-inline(.)">
                <xsl:apply-templates mode="SECOND_PASS"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="xhtml:strong[preceding-sibling::node()[1]/self::xhtml:strong]"/>
    <xsl:template mode="SECOND_PASS" match="xhtml:td">
        <xsl:copy>
            <xsl:apply-templates mode="SECOND_PASS" select="@*"/>
            <xsl:choose>
                <xsl:when test="xhtml:p and count(*) eq 1">
                    <xsl:apply-templates mode="SECOND_PASS"
                        select="xhtml:p/node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="SECOND_PASS"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    <xsl:template name="CONVERT_RUN">
        <xsl:param name="content" as="node()" select="."/>
        <xsl:param name="properties" as="node()*"/>
        <xsl:variable name="property" as="node()*" select="$properties[1]"/>
        <xsl:variable name="convertedRun">
            <xsl:choose>
                <xsl:when test="$property/self::w:i">
                    <em><xsl:copy-of select="$content"/></em>
                </xsl:when>
                <xsl:when test="$property/self::w:b">
                    <strong><xsl:copy-of select="$content"/></strong>
                </xsl:when>
                <xsl:when test="$property/@w:val = 'subscript'">
                    <sub><xsl:copy-of select="$content"/></sub>
                </xsl:when>
                <xsl:when test="$property/@w:val = 'superscript'">
                    <sup><xsl:copy-of select="$content"/></sup>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$content"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$properties[2]">
                <xsl:call-template name="CONVERT_RUN">
                    <xsl:with-param name="content" select="$convertedRun"/>
                    <xsl:with-param name="properties"
                        select="$properties[position() gt 1]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$convertedRun"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:function name="nota:count-spanned-rows" as="xs:integer">
        <xsl:param name="row" as="element(w:tr)"/>
        <xsl:param name="position" as="xs:integer"/>
        <xsl:variable name="nextRow" as="element(w:tr)*"
            select="$row/following-sibling::w:tr[1]"/>
        <xsl:variable name="mergedCellBelow" as="element(w:vMerge)*"
            select="$nextRow/w:tc[$position]/w:tcPr/w:vMerge
                        [not(@w:val eq 'restart')]"/>
        <xsl:value-of
            select="if ($mergedCellBelow)
                    then 1 + nota:count-spanned-rows($nextRow, $position)
                    else 1"/>
    </xsl:function>
    <xsl:function name="nota:expand-inline" as="element()*">
        <xsl:param name="element" as="element()"/>
        <xsl:variable name="elementName" as="xs:string"
            select="$element/name()"/>
        <xsl:variable name="firstFollowingSibling" as="node()*"
            select="$element/following-sibling::node()[1]"/>
        <xsl:sequence
            select="if ($firstFollowingSibling/name() eq $elementName)
                    then $element|nota:expand-inline($firstFollowingSibling)
                    else $element"/>
    </xsl:function>
</xsl:stylesheet>