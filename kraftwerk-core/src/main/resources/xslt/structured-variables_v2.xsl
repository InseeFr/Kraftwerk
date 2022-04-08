<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:g="ddi:group:3_3" xmlns:d="ddi:datacollection:3_3" xmlns:s="ddi:studyunit:3_3"
    xmlns:r="ddi:reusable:3_3" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:a="ddi:archive:3_3"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:l="ddi:logicalproduct:3_3"
    version="2.0"    
    exclude-result-prefixes="xs g d s r xhtml a xs l">
    
    <xsl:output indent="yes"/>
    
    <xsl:variable name="root" select="."/>
    
    <xsl:template match="/">
        <VariableGroups>
            <xsl:apply-templates select=".//l:VariableGroup"/>
        </VariableGroups>
    </xsl:template>
    
    <!--<xsl:template match="table:table[@table:name='Listes de codes']/table:table-row[1]">
        <xsl:copy-of select="."/>
        <xsl:apply-templates select="l:CodeListScheme/l:CodeList[r:ID = l:VariableRepresentation/r:CodeRepresentation/r:CodeListReference/r:ID]"/>
    </xsl:template>-->
    
    
    
    <!--<xsl:template match="l:VariableScheme">
        <variables><xsl:apply-templates/></variables>
    </xsl:template>-->
    
    <xsl:template match="l:Code" mode="ucqInfo">
        <xsl:variable name="value-id" select="r:CategoryReference/r:ID"/>
        <xsl:variable name="value-label" select="$root//l:CategoryScheme/l:Category[r:ID = $value-id]/r:Label/r:Content[@xml:lang='fr-FR']"/>
        <Value label="{normalize-space($value-label)}"><xsl:value-of select="r:Value"/></Value>
    </xsl:template>
    <xsl:template match="l:CodeList" mode="ucqInfoLength">
        <xsl:variable name="max-length">
            <xsl:for-each select="l:Code/string-length(r:Value)">
                <xsl:sort data-type="number" order="descending" />
                <xsl:if test="position()=1">
                    <xsl:value-of select="." />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:value-of select="max($max-length)"/>
    </xsl:template>
    
    <xsl:template match="l:Variable/l:VariableRepresentation/r:NumericRepresentation" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="../../l:VariableName/r:String"/>
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>
                <xsl:choose>
                    <xsl:when test="descendant::*/@decimalPositions">
                        <xsl:choose>
                            <xsl:when test="descendant::*[@decimalPositions][1]/@decimalPositions!='0'">NUMBER</xsl:when>
                            <xsl:otherwise>INTEGER</xsl:otherwise>
                        </xsl:choose>  
                    </xsl:when>
                    <xsl:otherwise>NUMBER</xsl:otherwise>                              
                </xsl:choose>
            </Format>
            <Size>
                <xsl:value-of select="string-length(r:NumberRange/r:High)"/>
                <xsl:choose>
                    <xsl:when test="@decimalPositions!='0'">.<xsl:value-of select="@decimalPositions"/></xsl:when>
            </xsl:choose>   
            </Size>
        </Variable>
    </xsl:template>
    <xsl:template match="l:Variable/l:VariableRepresentation/r:NumericRepresentationReference" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="../../l:VariableName/r:String"/>
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>
                <xsl:choose>
                    <xsl:when test="descendant::*/@decimalPositions">
                        <xsl:choose>
                            <xsl:when test="descendant::*[descendant::*/@decimalPositions][1]/@decimalPositions!='0'">NUMBER</xsl:when>
                            <xsl:otherwise>INTEGER</xsl:otherwise>
                        </xsl:choose>  
                    </xsl:when>
                    <xsl:otherwise>NUMBER</xsl:otherwise>                              
                </xsl:choose>
            </Format>
            <Size>
                <xsl:value-of select="string-length(r:NumberRange/r:High)"/>
                <xsl:choose>
                    <xsl:when test="descendant::*/@decimalPositions!='0'">.<xsl:value-of select="descendant::*/@decimalPositions"/></xsl:when>
                </xsl:choose>   
            </Size>
        </Variable>
    </xsl:template>
    <xsl:template match="l:Variable/l:VariableRepresentation/r:TextRepresentation" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="../../l:VariableName/r:String"/>
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>STRING</Format>
            <Size>
                <xsl:value-of select="@maxLength"/>
            </Size>
        </Variable>
        
    </xsl:template>
    <xsl:template match="l:Variable/l:VariableRepresentation/r:CodeRepresentation" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="../../l:VariableName/r:String"/>
            <Name><xsl:value-of select="$variable-name"/></Name>
            <xsl:choose>
                <xsl:when test="descendant::r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1'"><Format>BOOLEAN</Format><Size>1</Size></xsl:when>
                <!-- UCQ variables -->
                <xsl:otherwise><Format>STRING</Format><xsl:variable name="code-id" select="r:ID"/>
                    <Values>
                        <xsl:apply-templates select="$root//l:CodeListScheme/l:CodeList[r:ID=$code-id]/l:Code" mode="ucqInfo"/>
                    </Values></xsl:otherwise>
            </xsl:choose>
           
            
        </Variable>
        
    </xsl:template>
    <xsl:template match="l:Variable/l:VariableRepresentation/r:DateTimeRepresentation" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="../../l:VariableName/r:String"/>
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>DATE</Format>
            <Size>23</Size>
        </Variable>
        
    </xsl:template>
    <xsl:template match="l:Variable/l:VariableRepresentation/r:DateTimeRepresentationReference" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="../../l:VariableName/r:String"/>
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>DATE</Format>
            <Size>23</Size>
        </Variable>
        
    </xsl:template>
    
    <xsl:template match="l:VariableGroup">
        <Group id="{r:ID}" name="{l:VariableGroupName/r:String}">
            <xsl:variable name="parent-names" select="$root//l:VariableGroup[l:VariableGroupReference/r:ID=current()/r:ID]/l:VariableGroupName/r:String"/>
            <xsl:if test="$parent-names != ''">
                <xsl:attribute name="parent" select="$parent-names"/>
            </xsl:if>
            <xsl:apply-templates select="$root//l:Variable/l:VariableRepresentation/r:NumericRepresentation" mode="variablesInfo"/>
            <xsl:apply-templates select="$root//l:Variable/l:VariableRepresentation/r:NumericRepresentationReference" mode="variablesInfo"/>
            <xsl:apply-templates select="$root//l:Variable/l:VariableRepresentation/r:CodeRepresentation" mode="variablesInfo"/>
            <xsl:apply-templates select="$root//l:Variable/l:VariableRepresentation/r:TextRepresentation" mode="variablesInfo"/>
            <xsl:apply-templates select="$root//l:Variable/l:VariableRepresentation/r:DateTimeRepresentation" mode="variablesInfo"/>
            <xsl:apply-templates select="$root//l:Variable/l:VariableRepresentation/r:DateTimeRepresentationReference" mode="variablesInfo"/>
        </Group>
    </xsl:template>
    
</xsl:stylesheet>