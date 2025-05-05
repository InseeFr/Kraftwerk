package fr.insee.kraftwerk.api.batch;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum KraftwerkServiceType {
    MAIN,
    LUNATIC_ONLY,
    GENESIS,
    GENESISV2,
    FILE_BY_FILE
}
