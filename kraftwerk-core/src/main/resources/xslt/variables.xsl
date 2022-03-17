<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:g="ddi:group:3_3" xmlns:d="ddi:datacollection:3_3" xmlns:s="ddi:studyunit:3_3"
    xmlns:r="ddi:reusable:3_3" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:a="ddi:archive:3_3"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:l="ddi:logicalproduct:3_3"
    version="2.0"    
    exclude-result-prefixes="xs g d s r xhtml a xs l">

    <xsl:output indent="yes"/>
    
    <xsl:variable name="root" select="."/>
    
    <xsl:template match="/">
        <Variables>
            <xsl:apply-templates/>
        </Variables>      
    </xsl:template>
    
    <xsl:template match="Variable">
       <Variable>
           <Label><xsl:value-of select="Name"/></Label>
           <TypeVariable><xsl:value-of select="Format"/></TypeVariable>
       </Variable>
    </xsl:template>
    
</xsl:stylesheet>