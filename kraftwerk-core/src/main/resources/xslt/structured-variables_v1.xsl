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
            <Format>
                <xsl:choose>
                    <xsl:when test="l:VariableRepresentation/r:NumericRepresentation | l:VariableRepresentation/r:NumericRepresentationReference">NUMBER</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:TextRepresentation">STRING</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation//r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1'">BOOLEAN</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation">STRING</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:DateTimeRepresentation | l:VariableRepresentation/r:DateTimeRepresentationReference">DATE</xsl:when>
                    <xsl:otherwise>UNKNOWN</xsl:otherwise>
                </xsl:choose>
            </Format>
            
        </Variable>
    </xsl:template>
    
    <xsl:template match="l:VariableGroup">
        <Group id="{r:ID}" name="{l:VariableGroupName/r:String}">
            <xsl:variable name="parent-names" select="$root//l:VariableGroup[l:VariableGroupReference/r:ID=current()/r:ID]/l:VariableGroupName/r:String"/>
            <xsl:if test="$parent-names != ''">
                <xsl:attribute name="parent" select="$parent-names"/>
            </xsl:if>
            <xsl:apply-templates select="$root//l:Variable[r:ID=current()/r:VariableReference/r:ID]" mode="infoVariables"/>
        </Group>
    </xsl:template>
    
</xsl:stylesheet>