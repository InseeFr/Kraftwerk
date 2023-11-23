package fr.insee.kraftwerk.core.metadata;

import java.net.URL;

public interface DDIReader {
    VariablesMap getVariablesFromDDI(URL ddiUrl) throws Exception;
}
