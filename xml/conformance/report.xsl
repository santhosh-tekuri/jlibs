<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>
    <xsl:output method="text"/>
    <xsl:template match="TestResults">
Total  : <xsl:value-of select="@testsRun"/>
Passed : <xsl:value-of select="@testsPassed"/>
Failed : <xsl:value-of select="@testsRun - @testsPassed"/>
    </xsl:template>
</xsl:stylesheet>