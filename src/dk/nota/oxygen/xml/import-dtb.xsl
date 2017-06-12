<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:file="http://expath.org/ns/file"
    xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:zip="http://expath.org/ns/archive"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    extension-element-prefixes="saxon"
    version="2.0">
    <xsl:output method="xml" indent="yes" saxon:indent-spaces="4"/>
    <xsl:strip-space elements="body div imggroup level list table"/>
    <xsl:param name="ALLOWED_CLASSES" as="xs:string*"
        select="'frontcover', 'leftflap', 'note_identifier', 'poem',
                'rearcover', 'render_by_column', 'render_by_row', 'rightflap',
                'roman'"/>
    <xsl:param name="ARCHIVE_URL" as="xs:string"
        select="replace($CONTENT_FOLDER_URL, '^zip:|!.*$', '')"/>
    <xsl:param name="CONTENT_FOLDER_URL" as="xs:string"/>
    <xsl:param name="CREATOR" as="xs:string?"
        select="string-join($DTB_DOCUMENT/dtbook/head/meta[@name eq
                'dc:creator']/@content, '; ')"/>
    <xsl:param name="DATE" as="xs:dateTime"
        select="adjust-dateTime-to-timezone(current-dateTime(),
                xs:dayTimeDuration('PT0H'))"/>
    <xsl:param name="DTB_DOCUMENT" as="document-node()"
        select="document($DTB_URL)"/>
    <xsl:param name="DTB_URL" as="xs:string?"/>
    <xsl:param name="ISBN" as="xs:string?"
        select="$DTB_DOCUMENT/dtbook/head/meta[@name eq 'dc:source']/@content"/>
    <xsl:param name="LANG" as="xs:string?"
        select="$DTB_DOCUMENT/dtbook/book/@lang"/>
    <xsl:param name="PID" as="xs:string"
        select="if ($DTB_DOCUMENT/dtbook/head/meta[@name eq 'dc:identifier'])
                then $DTB_DOCUMENT/dtbook/head/meta[@name eq 'dc:identifier']/
                @content
                else '000000'"/>
    <xsl:param name="TITLE" as="xs:string?"
        select="$DTB_DOCUMENT/dtbook/head/title"/>
    <xsl:variable name="CONTENT_DOCUMENTS" as="element(xhtml:html)*">
        <!-- TODO: Handle edge cases where cover parts are already contained in
        a single level -->
        <xsl:variable name="coverLevels" as="element(level)*"
            select="$DTB_DOCUMENT/dtbook/book/frontmatter/nota:get-cover-levels(
                    level|level1)"/>
        <xsl:for-each
            select="nota:assemble-cover-level($coverLevels)|$DTB_DOCUMENT/
                    dtbook/book/*/((level|level1) except $coverLevels)/
                    nota:expand-parts(.)">
            <xsl:variable name="type" as="xs:string?"
                select="if (@nota:footnoteLevel) then 'footnotes'
                        else if (@nota:rearnoteLevel) then 'rearnotes'
                        else if (@class eq 'index') then 'index'
                        else if (@class eq 'part') then 'part'
                        else if (@class eq 'preface') then 'preface'
                        else if (@nota:placement eq 'bodymatter') then 'chapter'
                        else ''"/>
            <xsl:variable name="fileName" as="xs:string"
                select="concat($PID, '-', format-number(position(), '000'),
                        '-', if ($type) then $type else @nota:placement,
                        '.xhtml')"/>
            <xsl:call-template name="CONTENT_DOCUMENT">
                <xsl:with-param name="epubType" as="xs:string"
                    select="if ($type) then concat($type, ' ', @nota:placement)
                            else @nota:placement"/>
                <xsl:with-param name="fileName" as="xs:string"
                    select="$fileName"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="XHTML_HEAD" as="element(xhtml:head)">
        <head>
            <meta charset="UTF-8"/>
            <title>
                <xsl:value-of select="$TITLE"/>
            </title>
            <meta name="dc:identifier" content="{$PID}"/>
            <meta name="viewport" content="width=device-width"/>
            <link rel="stylesheet" type="text/css" href="css/stylesheet.css"/>
        </head>
    </xsl:variable>
    <xsl:variable name="IMAGES" as="element(nota:image)*">
        <xsl:for-each-group select="$DTB_DOCUMENT//img" group-by="@src">
            <xsl:variable name="imgFileName" as="xs:string"
                select="tokenize(@src, '/')[last()]"/>
            <xsl:variable name="type" as="xs:string"
                select="if (ends-with($imgFileName, '.gif')) then 'gif'
                        else if (ends-with($imgFileName, '.png')) then 'png'
                        else 'jpeg'"/>
            <nota:image href="{concat('images/', $imgFileName)}"
                type="{concat('image/', $type)}">
                <xsl:value-of select="file:read-binary(resolve-uri(@src, $DTB_URL))"/>
            </nota:image>
        </xsl:for-each-group>
    </xsl:variable>
    <xsl:variable name="NAV_DOCUMENT_NCX" as="element(ncx:ncx)">
        <xsl:variable name="headingListItems" as="element(xhtml:li)*"
            select="$NAV_DOCUMENT_XHTML/xhtml:body/xhtml:nav[1]/xhtml:ol/
                    xhtml:li"/>
        <xsl:variable name="pageListItems" as="element(xhtml:li)*"
            select="$NAV_DOCUMENT_XHTML/xhtml:body/xhtml:nav[2]/xhtml:ol/
                    xhtml:li"/>
        <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
            <head>
                <meta name="dtb:uid" content="{$PID}"/>
                <meta name="dtb:depth"
                    content="{max($headingListItems/count(.//xhtml:li)) + 1}"/>
                <meta name="dtb:maxPageNumber"
                    content="{$pageListItems[last()]/xhtml:a/text()}"/>
                <meta name="dtb:totalPageCount"
                    content="{count($pageListItems)}"/>
            </head>
            <docTitle>
                <text>
                    <xsl:value-of select="$TITLE"/>
                </text>
            </docTitle>
            <navMap>
                <navLabel>
                    <text>Indhold</text>
                </navLabel>
                <xsl:apply-templates mode="GENERATE_NAV_NCX_HEADINGS"
                    select="$headingListItems"/>
            </navMap>
            <xsl:if test="//pagenum">
                <pageList>
                    <navLabel>
                        <text>Liste over sider</text>
                    </navLabel>
                    <xsl:apply-templates mode="GENERATE_NAV_NCX_PAGES"
                        select="$pageListItems">
                        <xsl:with-param name="playOrderStart" as="xs:integer"
                            select="count($NAV_DOCUMENT_XHTML/xhtml:body/
                                    xhtml:nav[1]//xhtml:li)"/>
                    </xsl:apply-templates>
                </pageList>
            </xsl:if>
        </ncx>
    </xsl:variable>
    <xsl:variable name="NAV_DOCUMENT_XHTML" as="element(xhtml:html)">
        <html xmlns:epub="http://www.idpf.org/2007/ops"
            epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
            lang="{$LANG}"
            xml:lang="{$LANG}">
            <xsl:copy-of select="$XHTML_HEAD"/>
            <body>
                <nav epub:type="toc">
                    <h1 lang="da" xml:lang="da">Indhold</h1>
                    <ol class="list-style-type-none">
                        <xsl:for-each-group
                            group-starting-with="*[@nota:navDepth = 1]"
                            select="$CONTENT_DOCUMENTS">
                            <xsl:apply-templates mode="GENERATE_NAV_XHTML"
                                select="current-group()[1]/xhtml:body">
                                <xsl:with-param name="chapters"
                                    as="element(xhtml:body)*"
                                    select="current-group()[position() gt 1]/
                                            xhtml:body"/>
                            </xsl:apply-templates>
                        </xsl:for-each-group>
                    </ol>
                </nav>
                <xsl:if test="//pagenum">
                    <nav epub:type="page-list">
                        <h1 lang="da" xml:lang="da">Liste over sider</h1>
                        <ol class="list-style-type-none">
                            <xsl:apply-templates mode="GENERATE_NAV_XHTML"
                                select="$CONTENT_DOCUMENTS//xhtml:*[@epub:type
                                        eq 'pagebreak']"/>
                        </ol>
                    </nav>
                </xsl:if>
            </body>
        </html>
    </xsl:variable>
    <xsl:variable name="OPF_MANIFEST" as="element(opf:manifest)">
        <manifest xmlns="http://www.idpf.org/2007/opf">
            <item id="css" media-type="text/css"
                href="css/stylesheet.css"/>
            <item id="nav" media-type="application/xhtml+xml" 
                properties="nav" href="nav.xhtml"/>
            <item id="nav_ncx" media-type="application/x-dtbncx+xml"
                href="nav.ncx"/>
            <xsl:for-each select="$CONTENT_DOCUMENTS">
                <item id="{concat('document_', position())}"
                    media-type="application/xhtml+xml" href="{@nota:fileName}"/>
            </xsl:for-each>
            <xsl:for-each select="$IMAGES">
                <item id="{concat('image_', position())}" media-type="{@type}"
                    href="{@href}">
                    <xsl:if test="matches(@href, 'cover\.(gif|jpg|jpeg|png)')">
                    	<xsl:attribute name="properties" select="'cover-image'"/>
                    </xsl:if>
                </item>
            </xsl:for-each>
        </manifest>
    </xsl:variable>
    <xsl:variable name="OPF_METADATA" as="element(opf:metadata)">
        <metadata xmlns="http://www.idpf.org/2007/opf">
            <xsl:copy-of select="/opf:package/opf:metadata/@*"/>
            <dc:title>
                <xsl:value-of select="$TITLE"/>
            </dc:title>
            <dc:creator>
                <xsl:value-of select="$CREATOR"/>
            </dc:creator>
            <dc:identifier id="pub-identifier">
                <xsl:value-of select="$PID"/>
            </dc:identifier>
            <dc:language>
                <xsl:value-of select="$LANG"/>
            </dc:language>
            <dc:format>EPUB3</dc:format>
            <dc:publisher>Nota</dc:publisher>
            <xsl:if test="$ISBN">
                <dc:source>
                    <xsl:value-of
                        select="if (matches($ISBN, '^(urn:isbn:)')) then $ISBN
                                else concat('urn:isbn:', $ISBN)"/>
                </dc:source>
            </xsl:if>
            <dc:date>
                <xsl:value-of
                    select="format-dateTime($DATE, '[Y0001]-[M01]-[D01]')"/>
            </dc:date>
            <xsl:variable name="timeString" as="xs:string"
                select="format-dateTime($DATE,
                        '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"/>
            <meta property="dcterms:modified">
                <xsl:value-of select="$timeString"/>
            </meta>
            <meta name="dcterms:modified" content="{$timeString}"/>
            <meta property="nordic:guidelines">2015-1</meta>
            <meta content="2015-1" name="nordic:guidelines"/>
        </metadata>
    </xsl:variable>
    <xsl:variable name="OPF_SPINE" as="element(opf:spine)">
        <spine xmlns="http://www.idpf.org/2007/opf" toc="nav_ncx">
            <xsl:for-each select="$CONTENT_DOCUMENTS">
                <xsl:variable name="isNotLinear" as="xs:boolean"
                    select="tokenize(xhtml:body/@epub:type, '\s+') = ('cover',
                            'footnotes')"/>
                <itemref idref="{concat('document_', position())}">
                    <xsl:if test="$isNotLinear">
                        <xsl:attribute name="linear" select="'no'"/>
                    </xsl:if>
                </itemref>
            </xsl:for-each>
        </spine>
    </xsl:variable>
    <!-- NAMED TEMPLATES -->
    <xsl:template name="CONTENT_DOCUMENT" as="element(xhtml:html)">
        <xsl:param name="epubType" as="xs:string"/>
        <xsl:param name="fileName" as="xs:string"/>
        <html xmlns:epub="http://www.idpf.org/2007/ops"
            xmlns:nordic="http://www.mtm.se/epub/"
            epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
            lang="{if (@lang) then @lang else $LANG}"
            xml:lang="{if (@lang) then @lang else $LANG}"
            nota:fileName="{$fileName}"
            nota:navDepth="{@nota:navDepth}">
            <xsl:copy-of select="$XHTML_HEAD"/>
            <body id="{if (@id) then @id else generate-id(.)}"
                epub:type="{$epubType}">
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="FIGURE" as="element(xhtml:figure)">
        <xsl:param name="attributes" as="attribute()*"/>
        <xsl:param name="contents" as="node()*" select="node()"/>
        <figure class="image">
            <xsl:apply-templates select="$attributes except @class"/>
            <xsl:apply-templates select="$contents[self::img]"/>
            <xsl:apply-templates
                select="$contents[self::prodnote][@class eq 'caption']"/>
            <xsl:apply-templates
                select="$contents[self::prodnote][not(@class eq 'caption')]"/>
        </figure>
    </xsl:template>    <xsl:template name="SKIP_SINGLE_PARAGRAPH" as="node()*">
        <xsl:choose>
            <xsl:when test="count(p) eq 1 and count(*) eq 1">
                <xsl:apply-templates select="p/node()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- Root template -->
    <xsl:template match="/opf:package">
        <xsl:variable name="archive" as="xs:base64Binary"
            select="file:read-binary($ARCHIVE_URL)"/>
        <xsl:variable name="updatedArchive" as="xs:base64Binary"
            select="zip:update($archive, $IMAGES/concat('EPUB/', @href), $IMAGES)"/>
        <xsl:value-of select="file:write-binary($ARCHIVE_URL, $updatedArchive)"/>
        <nota:documents>
            <xsl:for-each select="$CONTENT_DOCUMENTS">
                <nota:document
                    url="{concat($CONTENT_FOLDER_URL, @nota:fileName)}">
                    <xsl:apply-templates mode="SECOND_PASS" select="."/>
                </nota:document>
            </xsl:for-each>
            <nota:document url="{concat($CONTENT_FOLDER_URL, 'nav.ncx')}">
                <xsl:copy-of select="$NAV_DOCUMENT_NCX"/>
            </nota:document>
            <nota:document url="{concat($CONTENT_FOLDER_URL, 'nav.xhtml')}">
                <xsl:apply-templates mode="SECOND_PASS"
                    select="$NAV_DOCUMENT_XHTML"/>
            </nota:document>
            <nota:document url="{concat($CONTENT_FOLDER_URL, 'package.opf')}">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:copy-of select="$OPF_METADATA"/>
                    <xsl:copy-of select="$OPF_MANIFEST"/>
                    <xsl:copy-of select="$OPF_SPINE"/>
                </xsl:copy>
            </nota:document>
        </nota:documents>
    </xsl:template>
    <xsl:template match="text()">
        <xsl:copy/>
    </xsl:template>
    <!-- ATTRIBUTES -->
    <xsl:template match="@*"/>
    <xsl:template match="@class[. = $ALLOWED_CLASSES]">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="@alt|@colspan|@height|@href|@id|@rowspan">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="@lang">
        <xsl:attribute name="lang" select="."/>
        <xsl:attribute name="xml:lang" select="."/>
    </xsl:template>
    <xsl:template match="@lang[. eq 'xx']"/>
    <!-- ELEMENTS -->
    <xsl:template match="*">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="acronym">
        <abbr epub:type="z3998:initialism">
            <xsl:apply-templates select="@*|node()"/>
        </abbr>
    </xsl:template>
    <xsl:template match="div[@class eq 'blockquote']">
        <blockquote>
            <xsl:apply-templates select="@*|node()"/>
        </blockquote>
    </xsl:template>
    <xsl:template match="div[@class eq 'poem']">
        <section epub:type="z3998:verse">
            <xsl:apply-templates select="@*|node()"/>
        </section>
    </xsl:template>
    <xsl:template match="div[@class eq 'stanza']">
        <div class="linegroup">
            <xsl:apply-templates select="@* except @class|node()"/>
        </div>
    </xsl:template>
    <xsl:template match="img">
        <xsl:variable name="imgFileName" as="xs:string"
            select="tokenize(@src, '/')[last()]"/>
        <img src="{concat('images/', $imgFileName)}">
            <xsl:apply-templates select="@*"/>
        </img>
    </xsl:template>
    <xsl:template match="imggroup">
        <xsl:call-template name="FIGURE">
            <xsl:with-param name="attributes" as="attribute()*" select="@*"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="imggroup[count(img) gt 1]">
        <figure class="image-series">
            <xsl:apply-templates select="@* except @class"/>
            <xsl:for-each-group group-starting-with="img" select="*">
                <xsl:variable name="group" as="node()+"
                    select="current-group()"/>
                <xsl:choose>
                    <xsl:when
                        test="count($group) eq 1 and $group[self::prodnote]">
                        <xsl:apply-templates select="$group"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="FIGURE">
                            <xsl:with-param name="contents" as="node()+"
                                select="$group"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>
        </figure>
    </xsl:template>
    <xsl:template match="level|level1|level2|level3|level4|level5|level6">
        <section id="{if (@id) then @id else generate-id(.)}">
            <xsl:apply-templates select="@* except @id|node()"/>
        </section>
    </xsl:template>
    <xsl:template match="levelhd|h1|h2|h3|h4|h5|h6">
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor::level|ancestor::level1|ancestor::level2|
                    ancestor::level3|ancestor::level4|ancestor::level5|
                    ancestor::level6)"/>
        <xsl:element name="{concat('h', $depth)}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="levelhd[normalize-space() eq '']"/>
    <xsl:template match="li|list/hd">
        <li>
            <xsl:apply-templates select="@*|node()"/>
        </li>
    </xsl:template>
    <xsl:template match="lic">
        <span class="lic">
            <xsl:apply-templates select="@* except @class|node()"/>
        </span>
    </xsl:template>
    <xsl:template match="line">
        <p class="line">
            <xsl:apply-templates select="@* except @class|node()"/>
        </p>
    </xsl:template>
    <xsl:template match="linenum">
        <span class="linenum">
            <xsl:apply-templates select="@* except @class|node()"/>
        </span>
    </xsl:template>
    <xsl:template match="list">
        <ul>
            <xsl:if test="@bullet eq 'none'">
                <xsl:attribute name="class"
                    select="'list-style-type-none'"/>
            </xsl:if>
            <xsl:apply-templates
                select="* except pagenum[not(following-sibling::li)]"/>
        </ul>
        <xsl:for-each select="pagenum[not(following-sibling::li)]">
            <xsl:apply-templates select=".">
                <xsl:with-param name="isInBlockContext" as="xs:boolean"
                    select="nota:is-in-block-context(parent::list)"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>
    <xsl:template mode="#default CONVERT_NOTES" match="note">
        <xsl:variable name="classes" as="xs:string*"
            select="tokenize(@class, '\s+')"/>
        <xsl:choose>
            <xsl:when test="$classes = ('endnote', 'footnote', 'rearnote')">
                <xsl:variable name="type" as="xs:string"
                    select="if ($classes = ('endnote', 'rearnote'))
                            then 'rearnote' else 'footnote'"/>
                <li id="{@id}" epub:type="{$type}" class="notebody">
                    <xsl:call-template name="SKIP_SINGLE_PARAGRAPH"/>
                </li>
            </xsl:when>
            <xsl:otherwise>
                <aside id="{@id}" epub:type="note" class="notebody">
                    <xsl:apply-templates/>
                </aside>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- For footnotes and rearnotes: the first note in the sequence
    establishes a list -->
    <!-- TODO: Simplify this mess -->
    <xsl:template match="note[nota:is-foot-or-rearnote(.)]">
        <ol class="list-style-type-none">
            <xsl:for-each
                select="self::note|following-sibling::* except
                        following-sibling::*[not(self::note|self::pagenum
                        [nota:is-followed-by-note(.)])][1]/(self::*|
                        following-sibling::*)">
                <!-- Revert to standard conversion of notes -->
                <xsl:apply-templates mode="CONVERT_NOTES" select="."/>
            </xsl:for-each>
        </ol>
    </xsl:template>
    <xsl:template priority="1"
        match="note[nota:is-foot-or-rearnote(.)][nota:is-preceded-by-note(.)]"/>
    <!-- Note references: convert to anchor elements identified by class -->
    <xsl:template match="noteref">
        <a epub:type="noteref" class="noteref"
            href="{replace(@idref, '^#', '')}">
            <xsl:apply-templates select="@* except @class|node()"/>
        </a>
    </xsl:template>
    <xsl:template
        match="p[@class = ('precedingemptyline', 'precedingseparator')]">
        <hr class="{replace(@class, '^preceding', '')}"/>
        <xsl:next-match/>
    </xsl:template>
    <xsl:template mode="#default CONVERT_NOTES" match="pagenum">
        <xsl:param name="isInBlockContext" as="xs:boolean"
            select="nota:is-in-block-context(.)"/>
        <xsl:element
            name="{if ($isInBlockContext) then 'div' else 'span'}">
            <xsl:copy-of select="@id"/>
            <xsl:attribute name="epub:type" select="'pagebreak'"/>
            <xsl:attribute name="title" select="text()"/>
            <xsl:attribute name="class" select="concat('page-', @page)"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="pagenum[nota:is-followed-by-note(.)]"/>
    <xsl:template match="prodnote">
        <aside epub:type="z3998:production" class="desc">
            <xsl:apply-templates select="@* except @class|node()"/>
        </aside>
    </xsl:template>
    <xsl:template match="prodnote[@class eq 'caption']">
        <figcaption>
            <xsl:call-template name="SKIP_SINGLE_PARAGRAPH"/>
        </figcaption>
    </xsl:template>
    <xsl:template match="sidebar">
        <aside epub:type="sidebar" class="sidebar">
            <xsl:apply-templates select="@* except @class|node()"/>
        </aside>
    </xsl:template>
    <xsl:template match="span[@lang eq 'xx' and count(@* except @id) eq 1]">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="table[not(thead|tbody|tfoot)]">
        <table>
            <xsl:apply-templates select="@*"/>
            <tbody>
                <xsl:apply-templates/>
            </tbody>
        </table>
    </xsl:template>
    <!-- SECOND PASS -->
    <xsl:template mode="SECOND_PASS" match="@*|node()">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates mode="SECOND_PASS" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="@nota:*"/>
    <xsl:template mode="SECOND_PASS" match="xhtml:a[@class eq 'noteref']">
        <xsl:variable name="referencedFileName" as="xs:string?"
            select="$CONTENT_DOCUMENTS//*[@id = current()/@href]/
                    ancestor::xhtml:html/@nota:fileName"/>
        <xsl:if test="not($referencedFileName)">
            <xsl:message
                select="concat('WARNING: Reference &quot;', @href,
                        '&quot; in ', ancestor::xhtml:html/@nota:fileName,
                        ' has no note')"/>
        </xsl:if>
        <xsl:variable name="reference" as="xs:string"
            select="if ($referencedFileName and $referencedFileName ne
                    ancestor::xhtml:html/@nota:fileName)
                    then concat($referencedFileName, '#', @href)
                    else concat('#', @href)"/>
        <a href="{$reference}">
            <xsl:apply-templates mode="SECOND_PASS"
                select="@* except @href|node()"/>
        </a>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="xhtml:*[@class eq 'notebody']">
        <xsl:if test="not($CONTENT_DOCUMENTS//xhtml:a[@href = current()/@id])">
            <xsl:message
                select="concat('WARNING: Note &quot;', @id,
                        '&quot; in ', ancestor::xhtml:html/@nota:fileName,
                        ' has no reference')"/>
        </xsl:if>
        <xsl:next-match/>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="xhtml:li[nota:get-preceding-pages(.)]//node()">
        <xsl:if
            test="nota:is-first-inline-node-in-element(., ancestor::xhtml:li[1])">
            <xsl:for-each
                select="nota:get-preceding-pages(ancestor::xhtml:li[1])">
                <span>
                    <xsl:copy-of select="@*"/>
                </span>
            </xsl:for-each>
        </xsl:if>
        <xsl:next-match/>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="xhtml:ol/xhtml:*[@epub:type eq 'pagebreak']"/>
    <xsl:template mode="SECOND_PASS"
        match="xhtml:ul/xhtml:*[@epub:type eq 'pagebreak']"/>
    <!-- SPECIAL MODE FOR GENERATING NCX NAV DOCUMENT -->
    <xsl:template mode="GENERATE_NAV_NCX_HEADINGS" match="xhtml:li">
        <xsl:variable name="position" as="xs:integer"
            select="count(ancestor-or-self::xhtml:li|preceding::xhtml:li)"/>
        <navPoint xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{concat('navPoint-', $position)}" playOrder="{$position}">
            <navLabel>
                <text>
                    <xsl:value-of select="xhtml:a/text()"/>
                </text>
            </navLabel>
            <content src="{xhtml:a/@href}"/>
            <xsl:if test="xhtml:ol">
                <xsl:apply-templates mode="GENERATE_NAV_NCX_HEADINGS"
                    select="xhtml:ol/xhtml:li"/>
            </xsl:if>
        </navPoint>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_NCX_PAGES" match="xhtml:li">
        <xsl:param name="playOrderStart" as="xs:integer?"/>
        <xsl:variable name="position" as="xs:integer"
            select="count(preceding-sibling::xhtml:li)"/>
        <pageTarget xmlns="http://www.daisy.org/z3986/2005/ncx/" 
            id="{concat('pageTarget-', $position)}" type="{@nota:pageType}"
            playOrder="{$playOrderStart + position()}">
            <navLabel>
                <text>
                    <xsl:value-of select="xhtml:a/text()"/>
                </text>
            </navLabel>
            <content src="{xhtml:a/@href}"/>
        </pageTarget>
    </xsl:template>
    <!-- SPECIAL MODE FOR GENERATING XHTML NAV DOCUMENT -->
    <xsl:template mode="GENERATE_NAV_XHTML" match="xhtml:body|xhtml:section">
        <xsl:param name="chapters" as="element(xhtml:body)*"/>
        <xsl:variable name="id" as="xs:string"
            select="(xhtml:*[matches(local-name(), 'h\d')][1]/@id|@id)[1]"/>
        <xsl:variable name="subentries" as="element()*"
            select="xhtml:section[not(@epub:type = ('z3998:poem',
                    'z3998:verse'))]|$chapters"/>
        <li>
            <a href="{concat(ancestor::xhtml:html/@nota:fileName, '#', $id)}">
                <xsl:value-of
                    select="if (xhtml:*[matches(local-name(), 'h\d')])
                            then xhtml:*[matches(local-name(), 'h\d')][1]/
                            normalize-space(string-join(.//text(), ''))
                            else '[***]'"/>
            </a>
            <xsl:if test="$subentries">
                <ol class="list-style-type-none">
                    <xsl:apply-templates mode="GENERATE_NAV_XHTML"
                        select="$subentries"/>
                </ol>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_XHTML"
        match="xhtml:*[@epub:type eq 'pagebreak']">
        <li nota:pageType="{substring-after(@class, 'page-')}">
            <a href="{concat(ancestor::xhtml:html/@nota:fileName, '#', @id)}">
                <xsl:value-of select="@title"/>
            </a>
        </li>
    </xsl:template>
    <!-- FUNCTIONS -->
    <xsl:function name="nota:assemble-cover-level" as="element(level)?">
        <xsl:param name="levels" as="element()*"/>
        <xsl:variable name="classes" as="xs:string+"
            select="'frontcover', 'leftflap', 'rearcover', 'rightflap'"/>
        <xsl:if test="$levels">
            <level xmlns="" nota:navDepth="1" nota:placement="cover">
                <xsl:for-each-group
                    group-by="tokenize(@class, '\s+')[. = $classes][1]"
                    select="if ($levels[tokenize(@class, '\s+') = $classes])
                            then $levels else $levels/level">
                    <level class="{current-grouping-key()}">
                        <xsl:copy-of
                            select="if (count(current-group()) gt 1)
                                    then current-group()
                                    else current-group()/node()"/>
                    </level>
                </xsl:for-each-group>
            </level>
        </xsl:if>
    </xsl:function>
    <xsl:function name="nota:expand-parts" as="element(level)+">
        <xsl:param name="e" as="element()"/>
        <xsl:variable name="placement" as="xs:string"
            select="replace($e/parent::*/local-name(), '^rear', 'back')"/>
        <xsl:choose>
            <xsl:when test="tokenize($e/@class, '\s+') = 'part'">
                <level xmlns="" nota:navDepth="1" nota:placement="{$placement}">
                    <xsl:copy-of
                        select="$e/(@*|node() except level[tokenize(@class,
                                '\s+') = 'chapter'][1]/(self::level|
                                following-sibling::node()))"/>
                </level>
                <xsl:for-each
                    select="$e/level[tokenize(@class, '\s+') = 'chapter'][1]/
                            (self::level|following-sibling::level)">
                    <level xmlns="" nota:navDepth="2"
                        nota:placement="{$placement}">
                        <xsl:copy-of select="@*|node()"/>
                    </level>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <level xmlns="" nota:navDepth="1" nota:placement="{$placement}">
                    <xsl:copy-of select="$e/(@*|node())"/>
                </level>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    <xsl:function name="nota:get-cover-levels" as="element(level)*">
        <xsl:param name="levels" as="element()*"/>
        <xsl:sequence
            select="$levels[(@class|level/@class)/tokenize(., '\s+') =
                    ('frontcover', 'leftflap', 'rearcover', 'rightflap')]"/>
    </xsl:function>
    
    <xsl:function name="nota:get-preceding-pages" as="element()*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence
            select="$n/(preceding-sibling::xhtml:*[@epub:type eq 'pagebreak']
                    except preceding-sibling::*[not(self::xhtml:*[@epub:type eq
                    'pagebreak'])][1]/(self::*|preceding-sibling::*))"/>
    </xsl:function>
    <xsl:function name="nota:is-first-inline-node-in-element" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:param name="e" as="element()"/>
        <xsl:value-of
            select="not($n/(ancestor::node()|preceding::node())
                    [nota:is-inline(.)] intersect
                    $e//node()[nota:is-inline(.)])"/>
    </xsl:function>
    <xsl:function name="nota:is-followed-by-note" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="$n/exists(following-sibling::*[not(self::pagenum)][1]/
                    self::note[nota:is-foot-or-rearnote(.)])"/>
    </xsl:function>
    <xsl:function name="nota:is-foot-or-rearnote" as="xs:boolean">
        <xsl:param name="e" as="element()"/>
        <xsl:value-of
            select="tokenize($e/@class, '\s+') = ('footnote', 'endnote',
                    'rearnote')"/>
    </xsl:function>
    <xsl:function name="nota:is-inline" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:variable name="inlineExpression" as="xs:string"
            select="'^(a|em|span|strong|sub|sup)$'"/>
        <xsl:value-of
            select="$n/(self::text() or matches(local-name(),
                    $inlineExpression))"/>
    </xsl:function>
    <xsl:function name="nota:is-in-block-context" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="$n/exists(parent::div|parent::imggroup|parent::level|
                    parent::prodnote|parent::sidebar)"/>
    </xsl:function>
    <xsl:function name="nota:is-preceded-by-note" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="$n/exists(preceding-sibling::*[not(self::pagenum)][1]/
                    self::note[nota:is-foot-or-rearnote(.)])"/>
    </xsl:function>
</xsl:stylesheet>