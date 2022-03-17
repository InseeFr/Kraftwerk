package fr.insee.kraftwerk.core.parsers;

/**
 * Enumeration class for the different tools from which survey data is collected.
 * There is one data parser parser per entry in this class.
 */
public enum DataFormat {
	XFORMS,
	PAPER,
	LUNATIC_XML,
    LUNATIC_JSON;
}
