<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" indent="no"/>
    <xsl:param name="OUTPUT_FOLDER_URL" as="xs:string?"/>
    <xsl:param name="PID" as="xs:string"
    	select="//html:html/html:head/html:meta[@name eq 'dc:identifier']/
    			replace(@content, '^dk-nota-', '')"/>
    <xsl:param name="TITLE" as="xs:string"
        select="//html:html/html:head/html:title"/>
    <xsl:template name="HTML_HEAD">
        <xsl:param name="includeStyle" as="xs:boolean" select="true()"/>
        <head>
            <meta charset="UTF-8"/>
            <title><xsl:value-of select="$TITLE"/></title>
            <meta name="dc:identifier" content="{$PID}"/>
            <xsl:if test="$includeStyle">
                <style type="text/css">
                    div.page {
                        position: relative;
                        top: 0px;
                        left: 0px;
                        zoom: 50%;}
                
                    div.area {
                        position: absolute;
                        border: dotted;
                        border-width: 1px;
                        margin: 0px;
                        padding: 0px;}
                    
                    div.page img {
                        position: absolute;
                        top: 0px;
                        left: 0px;
                        z-index: 2;
                        filter: alpha(opacity=70);
                        opacity: 0.7;}
                </style>
            </xsl:if>
        </head>
    </xsl:template>
    <xsl:template name="SMIL_HEAD">
        <xsl:param name="page" as="xs:string?"/>
        <head xmlns="">
            <meta name="dc:format" content="DAISY 2.02"/>
            <meta name="dc:title" content="{$TITLE}"/>
            <meta name="dc:identifier" content="{$PID}"/>
            <meta name="ncc:totalElapsedTime" content="00:00:00"/>
            <meta name="ncc:timeInThisSmil" content="00:00:00"/>
            <layout>
                <region id="txtView"/>
            </layout>
            <meta name="title" content="{$page}"/>
        </head>
    </xsl:template>
    <xsl:variable name="OUTPUT" as="node()+">
        <xsl:for-each-group
        	select="//html:section/html:*[not(self::html:section)]"
        	group-ending-with="html:*[not(self::html:section)][last()]">
            <xsl:variable name="fileName" as="xs:string"
                select="concat('page-', format-number(position(), '0000'))"/>
            <html fileName="{concat($fileName, '.xhtml')}">
                <xsl:call-template name="HTML_HEAD"/>
                <body>
                    <xsl:apply-templates mode="HTML" select="current-group()">
                        <xsl:with-param name="smilFileName" as="xs:string"
                            select="concat($fileName, '.smil')"/>
                    </xsl:apply-templates>
                </body>
            </html>
            <smil xmlns="" fileName="{concat($fileName, '.smil')}">
                <xsl:call-template name="SMIL_HEAD">
                    <xsl:with-param name="page" as="xs:string?"
                        select="current-group()[self::html:h1|self::html:h2|
                                self::html:h3|self::html:h4|self::html:h5|
                                self::html:h6][1]/string-join(text(), ' ')"/>
                </xsl:call-template>
                <body>
                    <seq dur="0.000s">
                        <xsl:apply-templates mode="SMIL"
                            select="current-group()">
                            <xsl:with-param name="htmlFileName" as="xs:string"
                                select="concat($fileName, '.xhtml')"/>
                        </xsl:apply-templates>
                    </seq>
                </body>
            </smil>
        </xsl:for-each-group>
    </xsl:variable>
    <xsl:template match="//html:html">
        <xsl:result-document href="{concat($OUTPUT_FOLDER_URL, 'ncc.html')}">
            <html xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:nordic="http://www.mtm.se/epub/">
                <xsl:call-template name="HTML_HEAD">
                    <xsl:with-param name="includeStyle" as="xs:boolean"
                        select="false()"/>
                </xsl:call-template>
                <body>
                    <xsl:copy-of
                        select="$OUTPUT[self::html:html]/html:body/(html:h1|
                        		html:h2|html:h3|html:h4|html:h5|html:h6)"/>
                </body>
            </html>
        </xsl:result-document>
        <xsl:result-document href="{concat($OUTPUT_FOLDER_URL, $PID, '.mdf')}"
            method="text">
            <xsl:text>[Tags]
1=div.page
2=div.area
3=h1
4=h2
5=h3
6=h4
7=h5
8=h6
9=p

[div.page]
Desc=
Name=div
Class=page
NCC=no
LinkBack=no
Level=0
Nested=yes
ID=yes

[div.area]
Desc=
Name=div
Class=area
NCC=no
LinkBack=no
Level=0
Nested=no
ID=yes

[h1]
Desc=
Name=h1
Class=
NCC=yes
LinkBack=yes
Level=1
Nested=no
ID=yes

[h2]
Desc=
Name=h2
Class=
NCC=yes
LinkBack=yes
Level=2
Nested=no
ID=yes

[h3]
Desc=
Name=h3
Class=
NCC=yes
LinkBack=yes
Level=3
Nested=no
ID=yes

[h4]
Desc=
Name=h4
Class=
NCC=yes
LinkBack=yes
Level=4
Nested=no
ID=yes

[h5]
Desc=
Name=h5
Class=
NCC=yes
LinkBack=yes
Level=5
Nested=no
ID=yes

[h6]
Desc=
Name=h6
Class=
NCC=yes
LinkBack=yes
Level=6
Nested=no
ID=yes

[p]
Desc=
Name=p
Class=
NCC=no
LinkBack=no
Level=0
Nested=no
ID=yes</xsl:text>
        </xsl:result-document>
        <xsl:for-each select="$OUTPUT[self::html:html]">
            <xsl:result-document href="{concat($OUTPUT_FOLDER_URL, @fileName)}">
                <html xmlns:nordic="http://www.mtm.se/epub/"
                    xmlns:epub="http://www.idpf.org/2007/ops">
                    <xsl:copy-of select="@* except @fileName|node()"/>
                </html>
            </xsl:result-document>
        </xsl:for-each>
        <xsl:for-each select="$OUTPUT[self::smil]">
            <xsl:result-document href="{concat($OUTPUT_FOLDER_URL, @fileName)}">
                <smil xmlns="">
                    <xsl:copy-of select="@* except @fileName|node()"/>
                </smil>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>
    <xsl:template mode="HTML" match="@*|node()">
    	<xsl:copy>
    		<xsl:apply-templates mode="HTML" select="@*|node()"/>
    	</xsl:copy>
    </xsl:template>
    <xsl:template mode="HTML" match="html:div">
        <xsl:copy>
        	<xsl:if test="not(@id)">
        		<xsl:attribute name="id" select="generate-id()"/>
        	</xsl:if>
        	<xsl:apply-templates mode="HTML" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="HTML"
        match="html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
        <xsl:param name="smilFileName" as="xs:string?"/>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <a href="{concat($smilFileName, '#', @id)}">
                <xsl:copy-of select="node()"/>
            </a>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="HTML" match="html:img">
    	<xsl:message>
    		<nota:image>
                <xsl:value-of select="@src"/>
    		</nota:image>
    	</xsl:message>
    	<xsl:copy>
    		<xsl:copy-of select="@* except @src"/>
    		<xsl:attribute name="src" select="replace(@src, 'images/', '')"/>
    	</xsl:copy>
    </xsl:template>
    <xsl:template mode="HTML" match="html:link"/>
    <xsl:template mode="SMIL" match="node()"/>
    <xsl:template mode="SMIL"
        match="html:div|html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
        <xsl:param name="htmlFileName" as="xs:string?"/>
        <xsl:variable name="id" as="xs:string" select="generate-id()"/>
        <par xmlns="" endsync="last">
            <text src="{concat($htmlFileName, '#', $id)}" id="{$id}"/>
        </par>
        <xsl:apply-templates mode="SMIL" select="html:div">
            <xsl:with-param name="htmlFileName" as="xs:string?"
                select="$htmlFileName"/>
        </xsl:apply-templates>
    </xsl:template>
</xsl:stylesheet>