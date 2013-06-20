<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
    <xsl:output method="text" encoding="utf-8" indent="yes"/>

    <!-- strip whitespace from whitespace-only nodes -->
    <xsl:strip-space elements="*"/>

    <!-- create a key for every element in the document using its name -->
    <xsl:key name="names" match="*" use="concat(generate-id(..),'/',name())"/>

    <!-- start with the root element -->
    <xsl:template match="/">
        <!-- first element needs brackets around it as template does not do that -->
        <xsl:text>{ </xsl:text>
        <!-- call the template for elements using one unique name at a time -->
        <xsl:apply-templates select="*[generate-id(.) = generate-id(key('names', concat(generate-id(..),'/',name()))[1])]" >
            <xsl:sort select="name()"/>
        </xsl:apply-templates>
        <xsl:text> }</xsl:text>
    </xsl:template>

    <!-- this template handles elements -->
    <xsl:template match="*">
        <!-- count the number of elements with the same name -->
        <xsl:variable name="kctr" select="count(key('names', concat(generate-id(..),'/',name())))"/>
        <!-- iterate through by sets of elements with same name -->
        <xsl:for-each select="key('names', concat(generate-id(..),'/',name()))">
            <!-- deal with the element name and start of multiple element block -->
            <xsl:choose>
                <xsl:when test="($kctr > 1) and (position() = 1)">
                    <xsl:text>"</xsl:text>
                    <xsl:value-of select="name()"/>
                    <xsl:text>" : [ </xsl:text>
                </xsl:when>
                <xsl:when test="$kctr = 1">
                    <xsl:text>"</xsl:text>
                    <xsl:value-of select="name()"/>
                    <xsl:text>" : </xsl:text>
                </xsl:when>
            </xsl:choose>
            <!-- count number of elements, text nodes and attribute nodes -->
            <xsl:variable name="nctr" select="count(*|text()|@*)"/>
            <xsl:choose>
                <xsl:when test="$nctr = 0">
                    <!-- no contents at all -->
                    <xsl:text>null</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="ctr" select="count(*)"/>
                    <xsl:variable name="tctr" select="count(text())"/>
                    <xsl:variable name="actr" select="count(@*)"/>
                    <!-- there will be contents so start an object -->
                    <xsl:text>{ </xsl:text>
                    <!-- handle attribute nodes -->
                    <xsl:if test="$actr > 0">
                        <xsl:apply-templates select="@*"/>
                        <xsl:if test="($tctr > 0) or ($ctr > 0)">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:if>
                    <!-- call template for child elements one unique name at a time -->
                    <xsl:if test="$ctr > 0">
                        <xsl:apply-templates select="*[generate-id(.) = generate-id(key('names', concat(generate-id(..),'/',name()))[1])]">
                            <xsl:sort select="name()"/>
                        </xsl:apply-templates>
                        <xsl:if test="$tctr > 0">
                            <xsl:text>, </xsl:text>
                        </xsl:if>
                    </xsl:if>
                    <!-- handle text nodes -->
                    <xsl:choose>
                        <xsl:when test="$tctr = 1">
                            <xsl:text>"$" : </xsl:text>
                            <xsl:apply-templates select="text()"/>
                        </xsl:when>
                        <xsl:when test="$tctr > 1">
                            <xsl:text>"$" : [ </xsl:text>
                            <xsl:apply-templates select="text()"/>
                            <xsl:text> ]</xsl:text>
                        </xsl:when>
                    </xsl:choose>
                    <xsl:text> }</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <!-- special processing if we are in multiple element block -->
            <xsl:if test="$kctr > 1">
                <xsl:choose>
                    <xsl:when test="position() = last()">
                        <xsl:text> ]</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>, </xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
        <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <!-- this template handle text nodes -->
    <xsl:template match="text()">
        <xsl:variable name="t" select="." />
        <xsl:choose>
            <!-- test to see if it is a number -->
            <xsl:when test="string(number($t)) != 'NaN'">
                <xsl:value-of select="$t"/>
            </xsl:when>
            <!-- deal with any case booleans -->
            <xsl:when test="translate($t, 'TRUE', 'true') = 'true'">
                <xsl:text>true</xsl:text>
            </xsl:when>
            <xsl:when test="translate($t, 'FALSE', 'false') = 'false'">
                <xsl:text>false</xsl:text>
            </xsl:when>
            <!-- must be text -->
            <xsl:otherwise>
                <xsl:text>"</xsl:text>
                <xsl:value-of select="$t"/>
                <xsl:text>"</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <!-- this template handles attribute nodes -->
    <xsl:template match="@*">
        <!-- attach prefix to attribute names -->
        <xsl:text>"@</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>" : </xsl:text>
        <xsl:variable name="t" select="." />
        <xsl:choose>
            <xsl:when test="string(number($t)) != 'NaN'">
                <xsl:value-of select="$t"/>
            </xsl:when>
            <xsl:when test="translate($t, 'TRUE', 'true') = 'true'">
                <xsl:text>true</xsl:text>
            </xsl:when>
            <xsl:when test="translate($t, 'FALSE', 'false') = 'false'">
                <xsl:text>false</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>"</xsl:text>
                <xsl:value-of select="$t"/>
                <xsl:text>"</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>