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
    
    <xsl:template match="l:Variable" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="current()/l:VariableName/r:String"/>
            
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>
                <xsl:choose>
                    <xsl:when test="l:VariableRepresentation/r:NumericRepresentation | l:VariableRepresentation/r:NumericRepresentationReference">
                        <xsl:choose>
                            <xsl:when test="descendant::*/@decimalPositions">
                                <xsl:choose>
                                    <xsl:when test="descendant::*[@decimalPositions][1]/@decimalPositions!='0'">NUMBER</xsl:when>
                                    <xsl:otherwise>INTEGER</xsl:otherwise>
                                </xsl:choose>  
                            </xsl:when>
                            <xsl:otherwise>NUMBER</xsl:otherwise>                              
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:TextRepresentation">STRING</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation//r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1'">BOOLEAN</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation">STRING</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:DateTimeRepresentation | l:VariableRepresentation/r:DateTimeRepresentationReference">DATE</xsl:when>
                    <xsl:otherwise>UNKNOWN</xsl:otherwise>
                </xsl:choose>
            </Format>
            <Size>
                <xsl:if test="l:VariableRepresentation/r:NumericRepresentation">
                    <xsl:value-of select="string-length(l:VariableRepresentation/r:NumericRepresentation/r:NumberRange/r:High)"/> <xsl:choose>
                        <xsl:when test="l:VariableRepresentation/r:NumericRepresentation/@decimalPositions!='0'">.<xsl:value-of select="l:VariableRepresentation/r:NumericRepresentation/@decimalPositions"/></xsl:when>
                    </xsl:choose>   
                </xsl:if>
                <xsl:if test="l:VariableRepresentation/r:TextRepresentation">
                    <xsl:value-of select="l:VariableRepresentation/r:TextRepresentation/@maxLength"/>
                </xsl:if>
                <xsl:if test="l:VariableRepresentation/r:DateTimeRepresentation">23</xsl:if>
                <xsl:choose>
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation//r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1'">1</xsl:when>
                    <xsl:otherwise><xsl:if test="l:VariableRepresentation/r:CodeRepresentation"><xsl:variable name="code-id" select="current()/l:VariableRepresentation/r:CodeRepresentation/r:CodeListReference/r:ID"/>
                        <xsl:apply-templates select="$root//l:CodeListScheme/l:CodeList[r:ID=$code-id]" mode="ucqInfoLength"/>
                    </xsl:if></xsl:otherwise>
                </xsl:choose>   
               
                <xsl:if test="l:VariableRepresentation/r:DateTimeRepresentationReference">23</xsl:if>
            </Size>
            
            <!-- MCQ variables -->
            <xsl:if test="$root//d:QuestionScheme/d:QuestionGrid[r:OutParameter/r:ParameterName/r:String=$variable-name]">
                <MCQ><xsl:value-of select="$root//d:QuestionScheme/d:QuestionGrid[r:OutParameter/r:ParameterName/r:String=$variable-name]/d:QuestionGridName/r:String"/></MCQ>
                <Label><xsl:value-of select="r:Label/r:Content"/></Label>
            </xsl:if>
            
            <!-- UCQ variables -->
            <xsl:if test="$root//d:QuestionScheme/d:QuestionItem[r:OutParameter/r:ParameterName/r:String=$variable-name]">
                <QuestionItemName><xsl:value-of select="$root//d:QuestionScheme/d:QuestionItem[r:OutParameter/r:ParameterName/r:String=$variable-name]/d:QuestionItemName/r:String"/></QuestionItemName>
            </xsl:if>
            <xsl:if test="l:VariableRepresentation/r:CodeRepresentation and not(l:VariableRepresentation/r:CodeRepresentation//r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1')">
                <xsl:variable name="code-id" select="current()/l:VariableRepresentation/r:CodeRepresentation/r:CodeListReference/r:ID"/>
                <Values>
                    <xsl:apply-templates select="$root//l:CodeListScheme/l:CodeList[r:ID=$code-id]/l:Code" mode="ucqInfo"/>
                </Values>
            </xsl:if>
        </Variable>
    </xsl:template>
    
    <xsl:template match="l:VariableGroup">
        <Group id="{r:ID}" name="{l:VariableGroupName/r:String}">
            <xsl:variable name="parent-names" select="$root//l:VariableGroup[l:VariableGroupReference/r:ID=current()/r:ID]/l:VariableGroupName/r:String"/>
            <xsl:if test="$parent-names != ''">
                <xsl:attribute name="parent" select="$parent-names"/>
            </xsl:if>
            <xsl:apply-templates select="$root//l:Variable[r:ID=current()/r:VariableReference/r:ID]" mode="variablesInfo"/>
        </Group>
    </xsl:template>
   
    
    
</xsl:stylesheet>