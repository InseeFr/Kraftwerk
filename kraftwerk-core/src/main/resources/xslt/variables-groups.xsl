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
        <VariablesGroups>
            <xsl:apply-templates select=".//l:VariableGroup"/>
        </VariablesGroups>      
    </xsl:template>
    
    <xsl:template match="l:VariableScheme">
        <variables><xsl:apply-templates/></variables>
    </xsl:template>
    
    <xsl:template match="l:VariableGroup">
        <GroupNames>
            <xsl:for-each select="l:VariableGroupName">
                <xsl:value-of select="r:String"/>
            </xsl:for-each>
           </GroupNames>
        
    </xsl:template>
    
</xsl:stylesheet>