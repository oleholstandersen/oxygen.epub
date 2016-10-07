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
    <xsl:param name="IMPORT_TO_CONCAT" as="xs:boolean"
        select="matches(document-uri(/), 'concat\.xhtml$')"/>
    <xsl:param name="SOURCE_URLS" as="xs:string*"/>
    <xsl:variable name="OPF_LANGUAGE" as="xs:string*"
        select="/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:variable name="OPF_PID" as="xs:string*"
        select="/opf:package/opf:metadata/dc:identifier/text()"/>
    <xsl:variable name="OPF_TITLE" as="xs:string*"
        select="/opf:package/opf:metadata/dc:title/text()"/>
    <xsl:variable name="ROOT" as="node()" select="/"/>
    <xsl:variable name="FIRST_PASS" as="node()*">
        <xsl:variable name="inputReferences" as="element()*">
            <xsl:for-each select="$SOURCE_URLS">
                <xsl:choose>
                    <xsl:when test="matches(., '\.docx!/word/$')">
                        <nota:docx url="{.}"/>
                    </xsl:when>
                    <xsl:when test="matches(., '\.kat$')">
                    	<xsl:message>
                    		<nota:out>
                    			<xsl:value-of select="doc-available(.)"/>
                    			<xsl:value-of select="concat(': ', .)"/>
                    		</nota:out>
                    	</xsl:message>
                        <nota:cat url="{.}"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:for-each-group select="$inputReferences"
            group-starting-with="nota:docx">
            <xsl:call-template name="DOCX.CONVERT">
                <xsl:with-param name="wordFolderUrl" as="xs:string"
                    select="current-group()[1]/@url"/>
                <xsl:with-param name="catLists" as="document-node()*"
                    select="document(current-group()[position() gt 1]/@url)"/>
            </xsl:call-template>
        </xsl:for-each-group>
    </xsl:variable>
    <xsl:template name="DOCX.CONVERT" as="element()">
        <xsl:param name="wordFolderUrl" as="xs:string" select="."/>
        <xsl:param name="catLists" as="document-node()*"/>
        <xsl:variable name="document" as="document-node()*"
            select="document(concat($wordFolderUrl, 'document.xml'))"/>
        <xsl:variable name="numbering" as="document-node()*"
            select="document(concat($wordFolderUrl, 'numbering.xml'))"/>
        <xsl:variable name="relationships" as="document-node()*"
            select="document(concat($wordFolderUrl,
                    '_rels/document.xml.rels'))"/>
        <xsl:variable name="styles" as="document-node()*"
            select="document(concat($wordFolderUrl, 'styles.xml'))"/>
        <body>
            <xsl:apply-templates select="$document/w:document/w:body/node()">
                <xsl:with-param name="numbering" tunnel="yes"
                    select="$numbering"/>
                <xsl:with-param name="relationships" tunnel="yes"
                    select="$relationships"/>
                <xsl:with-param name="styles" tunnel="yes"
                    select="$styles"/>
            </xsl:apply-templates>
            <xsl:apply-templates mode="CAT_LIST" select="$catLists"/>
        </body>
    </xsl:template>
    <xsl:template match="/xhtml:html">
        <nota:documents>
            <nota:document url="{document-uri(/)}">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:copy-of select="xhtml:head"/>
                    <body>
                        <xsl:copy-of select="xhtml:body/@*|xhtml:body/node()"/>
                        <xsl:choose>
                            <xsl:when test="$IMPORT_TO_CONCAT">
                                <xsl:for-each select="$FIRST_PASS">
                                    <section epub:type="chapter bodymatter">
                                        <xsl:apply-templates
                                            mode="SECOND_PASS"/>
                                    </section>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates mode="SECOND_PASS"
                                    select="$FIRST_PASS"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </body>
                </xsl:copy>
            </nota:document>
        </nota:documents>
    </xsl:template>
    <xsl:template match="/opf:package">
        <xsl:variable name="documents" as="element()*">
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
                        <body epub:type="chapter bodymatter">
                            <xsl:apply-templates mode="SECOND_PASS"
                                select="@*|xhtml:p[not(
                                        preceding-sibling::nota:hd)]|nota:hd
                                        [@depth = 1]"/>
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
    <xsl:template match="w:br">
        <xsl:param name="properties" as="node()*"/>
        <xsl:call-template name="DOCX.FORMATTING.CONVERT">
            <xsl:with-param name="content" as="node()">
                <br/>
            </xsl:with-param>
            <xsl:with-param name="properties" as="node()*"
                select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="w:p">
        <xsl:param name="numbering" as="document-node()*" tunnel="yes"/>
        <xsl:param name="styles" as="document-node()*" tunnel="yes"/>
        <xsl:variable name="styleName" as="xs:string*"
            select="w:pPr/w:pStyle/@w:val"/>
        <xsl:variable name="styleItem" as="node()*"
            select="$styles/w:styles/w:style[@w:styleId eq $styleName]"/>
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
            select="w:rPr/(w:b|w:i|w:vertAlign)"/>
        <xsl:apply-templates select="w:t|w:br">
            <xsl:with-param name="properties" as="node()*"
                select="$properties"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="w:t">
        <xsl:param name="properties" as="node()*"/>
        <xsl:call-template name="DOCX.FORMATTING.CONVERT">
            <xsl:with-param name="content" as="node()" select="text()"/>
            <xsl:with-param name="properties" as="node()*"
                select="$properties"/>
        </xsl:call-template>
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
        <xsl:variable name="content" as="node()+">
            <xsl:element name="{concat('h', $depth)}">
                <xsl:apply-templates mode="SECOND_PASS"
                    select="@*|node()"/>
            </xsl:element>
            <xsl:apply-templates mode="SECOND_PASS"
                select="following-sibling::* except
                        following-sibling::nota:hd[1]/
                        (self::nota:hd|following-sibling::*)"/>
            <xsl:apply-templates mode="NEST_SECTIONS"
                select="following-sibling::nota:hd[@depth &gt; $depth] except
                        following-sibling::nota:hd[@depth &lt;= $depth][1]/
                        (self::nota:hd|following-sibling::*)"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$depth = 1">
                <xsl:copy-of select="$content"/>
            </xsl:when>
            <xsl:otherwise>
                <section>
                    <xsl:copy-of select="$content"/>
                </section>
            </xsl:otherwise>
        </xsl:choose>
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
    <xsl:template name="DOCX.FORMATTING.CONVERT">
        <xsl:param name="content" as="node()" select="."/>
        <xsl:param name="properties" as="node()*"/>
        <xsl:variable name="property" as="node()?" select="$properties[1]"/>
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
                <xsl:call-template name="DOCX.FORMATTING.CONVERT">
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
        <xsl:variable name="nextRow" as="element(w:tr)?"
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
        <xsl:variable name="firstFollowingSibling" as="node()?"
            select="$element/following-sibling::node()[1]"/>
        <xsl:sequence
            select="if ($firstFollowingSibling/name() eq $elementName)
                    then $element|nota:expand-inline($firstFollowingSibling)
                    else $element"/>
    </xsl:function>
    <xsl:template mode="CAT_LIST" match="/dtbook">
    	<xsl:choose>
            <xsl:when
                test="book/frontmatter/doctitle/matches(., '^Top 10')">
                <xsl:apply-templates mode="CAT_LIST.CONVERT_TO_LIST"
                    select="book/frontmatter/level/div"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each-group group-by="nota:get-classification(.)"
                    select="book/frontmatter/level/div">
                    <nota:hd depth="2">
                        <xsl:value-of select="current-grouping-key()"/>
                    </nota:hd>
                    <xsl:apply-templates mode="CAT_LIST"
                        select="current-group()"/>
                </xsl:for-each-group>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="CAT_LIST" match="text()|@*">
        <xsl:copy/>
    </xsl:template>
    <xsl:template mode="CAT_LIST" match="*">
        <xsl:element name="{local-name()}"
            namespace="http://www.w3.org/1999/xhtml">
            <xsl:apply-templates mode="CAT_LIST" select="node()|@*"/>	
        </xsl:element>
    </xsl:template>
    <xsl:template mode="CAT_LIST" match="@id"/>
    <xsl:template mode="CAT_LIST" match="span[@class eq 'typedescription']">
        <xsl:variable name="id" as="xs:string?"
            select="following-sibling::*[1]/self::span
                    [@class eq 'masternummer']/text()"/>
        <span class="typedescription">
            <xsl:choose>
                <xsl:when test="$id">
                    <a href="{concat('http://www.e17.dk/bog/', $id)}"
                        class="link">
                        <xsl:value-of select="concat(., $id)"/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </span>
    </xsl:template>
    <xsl:template mode="CAT_LIST" match="span[@class = ('OEE', 'OEL', 'OEP')]">
        <xsl:variable name="id" as="xs:string"
            select="replace(text(), '.+?(\d+)$', '$1')"/>
        <span>
            <xsl:copy-of select="@class"/>
            <a href="{concat('http://www.e17.dk/bog/', $id)}" class="link">
                <xsl:value-of select="."/>
            </a>
        </span>
    </xsl:template>
    <xsl:template mode="CAT_LIST"
        match="span[@class eq 'masternummer'][nota:follows-type(.)]"/>
    <xsl:template mode="CAT_LIST" match="span[@class eq 'playingtime']">
        <xsl:variable name="hoursString" as="xs:string"
            select="replace(text(), '^Spilletid: (\d+).*?$', '$1')"/>
        <xsl:variable name="minutesString" as="xs:string"
            select="replace(text(), '^.*?(\d+) minutter\. $', '$1')"/>
        <xsl:variable name="hours" as="xs:integer"
            select="if (matches($hoursString, '^[0-9]+$'))
                    then xs:integer($hoursString)
                    else -1"/>
        <xsl:variable name="minutes" as="xs:integer"
            select="if (matches($minutesString, '^[0-9]+$'))
                    then xs:integer($minutesString)
                    else -1"/>
        <span class="playingtime">
            <xsl:value-of
                select="if ($hours gt -1 and $minutes gt -1) then
                        concat('Spilletid: ', $hours, if ($hours eq 1) then
                        ' time, ' else ' timer, ', $minutes, if ($minutes eq 1)
                        then ' minut.' else ' minutter.')
                        else text()"/>
        </span>
    </xsl:template>
    <xsl:template mode="CAT_LIST.CONVERT_TO_LIST"
        match="div[@class eq 'katalogpost']">
        <li>
            <xsl:apply-templates mode="CAT_LIST" select="p/node()"/>
        </li>
    </xsl:template>
    <xsl:function name="nota:get-classification" as="xs:string">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="if ($n/p/span[@class eq 'DK5']/matches(text(), '^99'))
                    then 'Erindringer og biografier'
                    else if ($n/p/span[@class eq 'DK5']/matches(text(),
                    '^8[2-5,7-8]')) then 'Skønlitteratur'
                    else if ($n/p/span[@class eq 'DK5']) then 'Faglitteratur'
                    else 'Skønlitteratur'"/>
    </xsl:function>
    <xsl:function name="nota:follows-type" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="$n/preceding-sibling::*[1]/self::span/@class eq
                    'typedescription'"/>
    </xsl:function>
</xsl:stylesheet>