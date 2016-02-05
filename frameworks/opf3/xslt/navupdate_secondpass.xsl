<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="xhtml xs"
    version="2.0">
    <xsl:template match="document">
        <xsl:result-document href="{@URL}">
            <xsl:if test="xhtml:xhtml">
                <xsl:value-of disable-output-escaping="yes"
                    select="'&#xa;&lt;!DOCTYPE html&gt;&#xa;'"/>
            </xsl:if>
            <xsl:sequence select="node()"/>
        </xsl:result-document>
    </xsl:template>
</xsl:stylesheet>