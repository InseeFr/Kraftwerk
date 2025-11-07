package fr.insee.kraftwerk.api.batch;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum KraftwerkServiceType {
    MAIN,
    GENESIS,
    FILE_BY_FILE,
    JSON
}
