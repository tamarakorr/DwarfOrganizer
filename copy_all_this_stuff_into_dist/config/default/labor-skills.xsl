<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : labor-skills.xsl
    Created on : 07 January 2014, 12:14
    Author     : Tamara Orr
    Description:
        Transformation for viewing labor-skills.xml as a table.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- Transformation rules
         syntax recommendation http://www.w3.org/TR/xslt
    -->

    <!-- Constants -->
    <xsl:variable name="skillBGColor" select="'#e3ccb1'"/>
    <xsl:variable name="statBGColor" select="'#f5edda'"/>
    <xsl:variable name="preventBGColor" select="'#f9f5ea'"/>

    <!-- General Document -->
    <xsl:template match="/">
        <html>
            <head>
                <title>labor-skills</title>
            </head>
            <body>
                <xsl:apply-templates select="root"/>
            </body>
        </html>
    </xsl:template>

    <!-- Headings and outer table-->
    <xsl:template match="Skills | SecondarySkills | SocialSkills">
        <xsl:choose>
            <xsl:when test="name() = 'Skills'">
                <h1>Labor Skills</h1>
            </xsl:when>
            <xsl:when test="name() = 'SecondarySkills'">
                <h1>Secondary Skills</h1>
            </xsl:when>
            <xsl:when test="name() = 'SocialSkills'">
                <h1>Social Skills</h1>
            </xsl:when>
            <xsl:otherwise>
                <h1>Unsupported: <xsl:value-of select="name()"/></h1>
            </xsl:otherwise>
        </xsl:choose>

        <table border="1">
            <tr bgcolor="{$skillBGColor}">
                <th style="text-align:center">
                    <xsl:choose>
                        <xsl:when test="name() = 'Skills'">
                            Labor
                        </xsl:when>
                        <xsl:when test="name() = 'SecondarySkills' or name() = 'SocialSkills'">
                            Skill
                        </xsl:when>
                        <xsl:otherwise>
                            Unsupported!
                        </xsl:otherwise>
                    </xsl:choose>
                </th>

                <th style="text-align:center">Stat</th>

                <xsl:if test="name() = 'SocialSkills'">
                    <th style="text-align:center">Prevented By</th>
                </xsl:if>
                <th style="text-align:center">Note</th>
            </tr>

            <xsl:apply-templates select="Skill | SecondarySkill | SocialSkill">
                <xsl:sort select="Name"/>
            </xsl:apply-templates>
	</table>
    </xsl:template>

    <!-- Skill row -->
    <xsl:template match="Skill | SecondarySkill | SocialSkill">
        <tr>
            <th bgcolor="{$skillBGColor}" style="text-align:center">
                <xsl:value-of select="Name"/>
            </th>
            <xsl:apply-templates select="Stats"/>
            <xsl:apply-templates select="PreventedBy"/>
            <xsl:apply-templates select="Note"/>
            <!-- Still color the background if there is no Note -->
            <xsl:if test="not(Note)">
                <td style="vertical-align:text-top;" bgcolor="{$preventBGColor}"/>
            </xsl:if>
        </tr>
    </xsl:template>

    <!-- Stats table -->
    <xsl:template match="Stats">
        <td>
            <table width="100%" border="1">
                <xsl:apply-templates select="Stat">
                    <xsl:sort select="text()"/>
                </xsl:apply-templates>
            </table>
        </td>
    </xsl:template>

    <!-- Stat list item -->
    <xsl:template match="Stat">
        <tr bgcolor="{$statBGColor}">
            <td><xsl:value-of select="current()"/></td>
        </tr>
    </xsl:template>

    <!-- Prevented by -->
    <xsl:template match="PreventedBy">
        <td valign="center" bgcolor="{$preventBGColor}">
            <table width="100%">
                <tr>
                    <td align="center"><xsl:value-of select="Trait"/></td>
                </tr>
                <tr>
                    <td align="center">
                        <xsl:value-of select="Min"/> to <xsl:value-of select="Max"/>
                    </td>
                </tr>
            </table>
        </td>
    </xsl:template>

    <!-- Note -->
    <xsl:template match="Note">
        <td style="vertical-align:text-top;" bgcolor="{$preventBGColor}">
            <xsl:value-of select="current()"/>
        </td>
    </xsl:template>

</xsl:stylesheet>
