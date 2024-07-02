package fr.insee.kraftwerk.api.batch;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum KraftwerkServiceType {
    MAIN,
    LUNATIC_ONLY,
    GENESIS,
    FILE_BY_FILE;
}
