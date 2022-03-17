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
    
    <xsl:template match="l:Variable" mode="infoVariables">
        <Variable>
            <Name><xsl:value-of select="l:VariableName/r:String"/></Name>
            <Format><xsl:if test="l:VariableRepresentation/r:NumericRepresentation">NUMBER</xsl:if>
                <xsl:if test="l:VariableRepresentation/r:TextRepresentation">STRING</xsl:if>
                <xsl:if test="l:VariableRepresentation/r:CodeRepresentation">STRING</xsl:if>
                <xsl:if test="l:VariableRepresentation/r:DateTimeRepresentation">STRING</xsl:if><!-- DATE -->
                <xsl:if test="l:VariableRepresentation/r:DateTimeRepresentationReference">STRING</xsl:if><!-- DATE -->
            </Format>
            <Size>
                <xsl:if test="l:VariableRepresentation/r:NumericRepresentation">
                    <xsl:value-of select="string-length(l:VariableRepresentation/r:NumericRepresentation/r:NumberRange/r:High)"/>
                </xsl:if>
                <xsl:if test="l:VariableRepresentation/r:TextRepresentation">
                    <xsl:value-of select="l:VariableRepresentation/r:TextRepresentation/@maxLength"/>
                </xsl:if>
            </Size>
        </Variable>
    </xsl:template>
    
    <xsl:template match="l:VariableGroup">
        <Group id="{r:ID}" name="{l:VariableGroupName/r:String}">
            <xsl:for-each select="r:VariableReference">
                    <xsl:variable name="idRef" select="r:ID"/>
                <xsl:apply-templates select="$root//l:Variable[r:ID=$idRef]" mode="infoVariables"/>
            </xsl:for-each>
           </Group>
        
    </xsl:template>
    
</xsl:stylesheet>