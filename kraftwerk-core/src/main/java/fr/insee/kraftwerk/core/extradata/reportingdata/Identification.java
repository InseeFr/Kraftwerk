package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;
import lombok.Setter;

public class Identification {
    @Getter@Setter
    private String identification;
    @Getter@Setter
    private String access;
    @Getter@Setter
    private String situation;
    @Getter@Setter
    private String category;
    @Getter@Setter
    private String occupant;
}
