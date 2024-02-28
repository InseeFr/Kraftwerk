<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:g="ddi:group:3_3" xmlns:d="ddi:datacollection:3_3" xmlns:s="ddi:studyunit:3_3"
    xmlns:r="ddi:reusable:3_3" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:a="ddi:archive:3_3"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:l="ddi:logicalproduct:3_3"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    version="2.0"    
    exclude-result-prefixes="xs g d s r xhtml a xs l">
    
    <xsl:output indent="yes"/>
    
    <xsl:variable name="root" select="."/>
    
    <xd:doc>
        <xd:desc>Template de racine, on applique les templates de tous les enfants</xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <VariableGroups>
            <xsl:apply-templates select="//g:ResourcePackage/l:VariableScheme/l:VariableGroup"/>
        </VariableGroups>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Les variables sont rassemblées par "Groupes" :</xd:p>
            <xd:p>- un pour les variables de niveau questionnaire</xd:p>
            <xd:p>- un pour chaque boucle ou ensemble de boucles (ex : dans un questionnaire sur les ménages, 
                on peut avoir un groupe contenant un tableau dynamique des individus et une boucle sur les individus majeurs</xd:p>
            <xd:p>- un pour chaque tableau de liens 2 à 2</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="l:VariableGroup">
        <Group id="{r:ID}" name="{l:VariableGroupName/r:String}">
            <xsl:variable name="parent-names" select="$root//g:ResourcePackage/l:VariableScheme/l:VariableGroup[l:VariableGroupReference/r:ID=current()/r:ID]
                                                                                                              /l:VariableGroupName/r:String"/>
            <xsl:if test="$parent-names != ''">
                <xsl:attribute name="parent" select="$parent-names"/>
            </xsl:if>
            <xsl:apply-templates select="$root//g:ResourcePackage/l:VariableScheme/l:Variable[r:ID=current()/r:VariableReference/r:ID]" mode="variablesInfo"/>
        </Group>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Recherche de différentes informations sur une variable donnée :</xd:p>
            <xd:p>- Name : nom métier</xd:p>
            <xd:p>- Format : NUMBER (décimal) ; INTEGER ; STRING (y compris code dans une liste) ; BOOLEAN ; DATE ; UNKNOWN</xd:p>
            <xd:p>- Size : Longueur maximale (format SAS) : 1 pour les Booléens ; 23 pour les dates ; au plus juste pour les nombres et String</xd:p>
            <xd:p>- pour les variables des tableaux :</xd:p>
            <xd:p>  - QGrid : le nom métier de la question</xd:p>
            <xd:p>  - Label : le label de la variable</xd:p>
            <xd:p>- pour les variables des questions à réponse unique :</xd:p>
            <xd:p>  - QuestionItemNae : le nom métier de la question</xd:p>
            <xd:p>- pour les variables au format code dans une liste (QCU) :</xd:p>
            <xd:p>  - Values : contient un élément Value pour chaque code de la liste :</xd:p>
            <xd:p>      - Value : contient l'attribut label (libellé français) et a pour contenu la valeur du code</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="l:Variable" mode="variablesInfo">
        <Variable>
            <xsl:variable name="variable-name" select="current()/l:VariableName/r:String"/>
            
            <Name><xsl:value-of select="$variable-name"/></Name>
            <Format>
                <xsl:choose>
                    <xsl:when test="l:VariableRepresentation/r:NumericRepresentation 
                                  | l:VariableRepresentation/r:NumericRepresentationReference">
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
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation/r:CodeSubsetInformation/r:IncludedCode/r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1'">BOOLEAN</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:CodeRepresentation">STRING</xsl:when>
                    <xsl:when test="l:VariableRepresentation/r:DateTimeRepresentation 
                                  | l:VariableRepresentation/r:DateTimeRepresentationReference">DATE</xsl:when>
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
                <xsl:if test="l:VariableRepresentation/r:DateTimeRepresentation 
                            | l:VariableRepresentation/r:DateTimeRepresentationReference">23</xsl:if>
                <xsl:if test="l:VariableRepresentation/r:CodeRepresentation">
                    <xsl:choose>
                        <xsl:when test="l:VariableRepresentation/r:CodeRepresentation/r:CodeSubsetInformation/r:IncludedCode/r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1'">1</xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="code-id" select="current()/l:VariableRepresentation/r:CodeRepresentation/r:CodeListReference/r:ID"/>
                            <xsl:apply-templates select="$root//g:ResourcePackage/l:CodeListScheme/l:CodeList[r:ID=$code-id]" mode="CodesMaxlength"/>
                        </xsl:otherwise>
                </xsl:choose>
                </xsl:if>
            </Size>
            
            <!-- QuestionGrid variables -->
            <xsl:if test="$root//g:ResourcePackage/d:QuestionScheme/d:QuestionGrid[r:OutParameter/r:ParameterName/r:String=$variable-name]">
                <QGrid>
                    <xsl:value-of select="$root//g:ResourcePackage/d:QuestionScheme/d:QuestionGrid[r:OutParameter/r:ParameterName/r:String=$variable-name]
                                                                                                 /d:QuestionGridName/r:String"/>
                </QGrid>
                <Label><xsl:value-of select="r:Label/r:Content"/></Label>
            </xsl:if>
            <!-- QuestionItem variable -->
            <xsl:if test="$root//g:ResourcePackage/d:QuestionScheme/d:QuestionItem[r:OutParameter/r:ParameterName/r:String=$variable-name]">
                <QuestionItemName>
                    <xsl:value-of select="$root//g:ResourcePackage/d:QuestionScheme/d:QuestionItem[r:OutParameter/r:ParameterName/r:String=$variable-name]
                                                                                                 /d:QuestionItemName/r:String"/>
                </QuestionItemName>
            </xsl:if>
            
            <!-- UCQ variables -->
            <xsl:if test="l:VariableRepresentation/r:CodeRepresentation
                and not(l:VariableRepresentation/r:CodeRepresentation/r:CodeSubsetInformation/r:IncludedCode/r:CodeReference/r:ID='INSEE-COMMUN-CL-Booleen-1')">
                <xsl:variable name="code-id" select="current()/l:VariableRepresentation/r:CodeRepresentation/r:CodeListReference/r:ID"/>
                <Values>
                    <xsl:apply-templates select="$root//g:ResourcePackage/l:CodeListScheme/l:CodeList[r:ID=$code-id]/l:Code" mode="ucqInfo"/>
                </Values>
            </xsl:if>
            
            <xsl:variable name="module-id">
                <xsl:choose>
                    <!-- Collected variable -->
                    <xsl:when test="r:SourceParameterReference">
                        <xsl:apply-templates select="$root//g:ResourcePackage/d:ControlConstructScheme/*[d:ControlConstructReference/r:ID 
                                                                                                        = $root//g:ResourcePackage/d:ControlConstructScheme/d:QuestionConstruct[r:QuestionReference/r:ID 
                                                                                                                                                                               = current()/r:QuestionReference/r:ID]/r:ID]"
                        mode="module"/>
                    </xsl:when>
                    <!-- calculated variable -->
                    <xsl:when test="l:VariableRepresentation/r:ProcessingInstructionReference">
                        <xsl:apply-templates
                            select="$root//g:ResourcePackage/d:ControlConstructScheme/*[r:ID 
                                                                                        = $root//g:ResourcePackage/d:ProcessingInstructionScheme/d:GenerationInstruction[r:ID = current()/l:VariableRepresentation/r:ProcessingInstructionReference/r:ID]
                                                                                                                                                                     /d:ControlConstructReference/r:ID]"
                        mode="module"/>
                    </xsl:when>
                    <!-- external variable -->
                    <xsl:otherwise/>
                </xsl:choose>
            </xsl:variable>
                <xsl:if test="$module-id !=''">
                    <Sequence name="{$root//g:ResourcePackage/d:ControlConstructScheme/d:Sequence[r:ID=$module-id]
                                                                                                /d:ConstructName/r:String[@xml:lang='fr-FR']}">
                        <xsl:value-of select="$root//g:ResourcePackage/d:ControlConstructScheme/d:Sequence[r:ID=$module-id]
                                                                                                         /r:Label/r:Content[@xml:lang='fr-FR']"/>
                    </Sequence>
                </xsl:if>            
        </Variable>
    </xsl:template>

    <xd:doc>
        <xd:desc>Méthode de calcul de la longueur maximale des codes d'une liste
        TODO : prendre en compte les listes de codes qui en référencent d'autres</xd:desc>
    </xd:doc>
    <xsl:template match="l:CodeList" mode="CodesMaxlength">
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
    
    <xd:doc>
        <xd:desc>Détail de la valeur d'un code : libellé en attribut label ; valeur en contenu</xd:desc>
    </xd:doc>
    <xsl:template match="l:Code" mode="ucqInfo">
        <xsl:variable name="value-id" select="r:CategoryReference/r:ID"/>
        <xsl:variable name="value-label" select="$root//g:ResourcePackage/l:CategoryScheme/l:Category[r:ID = $value-id]
                                                                                                    /r:Label/r:Content[@xml:lang='fr-FR']"/>
        <Value label="{normalize-space($value-label)}">
            <xsl:value-of select="r:Value"/>
        </Value>
    </xsl:template>
    
    <xd:doc>
        <xd:desc>template récursif pour obtenir les informations du module dont dépend la variable</xd:desc>
    </xd:doc>
    <xsl:template match="*" mode="module">
        <xsl:choose>
            <xsl:when test="d:TypeOfSequence='module'">
                <xsl:value-of select="r:ID"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$root//g:ResourcePackage/d:ControlConstructScheme/*[d:ControlConstructReference/r:ID = current()/r:ID
                                                                                                or d:ThenConstructReference/r:ID = current()/r:ID]" mode="module"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>