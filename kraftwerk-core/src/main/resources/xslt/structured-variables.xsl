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
                <xsl:if test="l:VariableRepresentation/r:NumericRepresentation or l:VariableRepresentation/r:NumericRepresentationReference">
                    <!-- Structured Variable with min, max and decimals, used just afterwards -->
                    <xsl:variable name="numeric-parameters">
                        <xsl:element name="Parameters">
                            <xsl:choose>
                                <xsl:when test="self::r:NumericRepresentation">
                                    <xsl:element name="Decimal">
                                        <xsl:choose>
                                            <xsl:when test="@decimalPositions">
                                                <xsl:value-of select="@decimalPositions"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="'0'"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:element>
                                    <xsl:element name="Minimum">
                                        <xsl:value-of select="r:NumberRange/r:Low"/>
                                    </xsl:element>
                                    <xsl:element name="Maximum">
                                        <xsl:value-of select="r:NumberRange/r:High"/>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element name="Decimal">
                                        <xsl:choose>
                                            <xsl:when test="descendant::*/@decimalPositions">
                                                <xsl:value-of select="descendant::*[@decimalPositions][1]/@decimalPositions"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="'0'"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:element>
                                    <xsl:element name="Minimum">
                                        <xsl:value-of select="descendant::r:NumberRange[1]/r:Low"/>
                                    </xsl:element>
                                    <xsl:element name="Maximum">
                                        <xsl:value-of select="descendant::r:NumberRange[1]/r:High"/>
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:element>
                    </xsl:variable>
                    <xsl:variable name="minimum-wholepart-length" select="string-length(if (contains($numeric-parameters//Minimum,'.')) then substring-before($numeric-parameters//Minimum,'.') else $numeric-parameters//Minimum)"/>
                    <xsl:variable name="maximum-wholepart-length" select="string-length(if (contains($numeric-parameters//Maximum,'.')) then substring-before($numeric-parameters//Maximum,'.') else $numeric-parameters//Maximum)"/>
                    <xsl:variable name="wholepart-length" select="if ($minimum-wholepart-length &gt; $maximum-wholepart-length) then $minimum-wholepart-length else $maximum-wholepart-length"/>
                            <!-- SAS format for decimal numbers : (wholepart + 1 + decimal) "." (decimal) -->
                            <xsl:choose>
                        <xsl:when test="$numeric-parameters//Decimal != '0'">
                            <xsl:value-of select="concat($wholepart-length+number($numeric-parameters//Decimal)+1,'.',$numeric-parameters//Decimal)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($wholepart-length,'.')"/>
                        </xsl:otherwise>
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

    <!-- QuestionGrid variables -->
            <xsl:if test="$root//d:QuestionScheme/d:QuestionGrid[r:OutParameter/r:ParameterName/r:String=$variable-name]">
                <QGrid><xsl:value-of select="$root//d:QuestionScheme/d:QuestionGrid[r:OutParameter/r:ParameterName/r:String=$variable-name]/d:QuestionGridName/r:String"/></QGrid>
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