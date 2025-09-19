package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportingIdentification {
    private static final Set<String> CONFIG_IASCO = Set.of("IASCO", "HOUSEF2F");
    private static final Set<String> CONFIG_PHONE = Set.of("INDTEL", "INTELNOR");
    private static final Set<String> CONFIG_F2F = Set.of("INDF2F", "INF2FNOR");
    private static final String ORDINARY = "ORDINARY";
    private static final String NOORDINARY = "NOORDINARY";
    private String identification;
    private String access;
    private String situation;
    private String category;
    private String occupant;
    private String individualStatus;
    private String interviewerCanProcess;

    public String getOutcomeSpotting(String identificationConfiguration) {
        // Outcome spotting for reporting data from Moog (WEB) is not applicable
        if (identificationConfiguration == null && identification == null) {
            return null;
        }
        // Case of old format of reporting data. No identificationConfiguration, but identification is not null.
        // We apply outcomeForHouseF2F algorithm
        if (identificationConfiguration == null || CONFIG_IASCO.contains(identificationConfiguration)) {
            return outcomeForHouseF2F();
        } else if (CONFIG_PHONE.contains(identificationConfiguration)) {
            return outcomeForPhone();
        } else if (CONFIG_F2F.contains(identificationConfiguration)) {
            return outcomeForIndF2F();
        }
        return null;
    }


    public String outcomeForHouseF2F() {
        if (StringUtils.isEmpty(identification)) {
            return null;
        }
        switch (identification) {
            case "DESTROY" -> {
                return "DESTROY";
            }
            case "UNIDENTIFIED" -> {
                return "UNIDENTIF";
            }
            case "IDENTIFIED" -> {
                switch (access) {
                    case "NACC" -> {
                        return "NACCNO";
                    }
                    case "ACC" -> {
                        return accessCase();
                    }
                    default -> {
                        return null;
                    }
                }
            }
            default -> {
                return null;
            }
        }
    }

    private String accessCase() {
        switch (situation) {
            case "ABSORBED" -> {
                return "ACCABS";
            }
            case NOORDINARY -> {
                return "ACCNO";
            }
            case ORDINARY -> {
                return ordinarySituationCase();
            }
            default -> {
                return null;
            }
        }
    }

    private String ordinarySituationCase() {
        switch (category) {
            case "VACANT" -> {
                return "ACCVAC";
            }
            case "SECONDARY" -> {
                return "ACCSEC";
            }
            case "PRIMARY" -> {
                return primaryCategoryCase();
            }
            default -> {
                return null;
            }
        }
    }

    private String primaryCategoryCase() {
        switch (occupant) {
            case "IDENTIFIED" -> {
                return "ACCPRIDENT";
            }
            case "UNIDENTIFIED" -> {
                return "ACCPRUNIDENT";
            }
            default -> {
                return null;
            }
        }
    }

    private String outcomeForPhone() {
        if (individualStatus == null) return null;
        return switch (individualStatus) {
            case "DCD" -> "INDDCD";
            case "NOIDENT" -> "INDNOIDENT";
            case "NOFIELD" -> "INDNOFIELD";
            case "SAMEADRESS" -> {
                if (ORDINARY.equals(situation)) yield "INDORDSADR";
                yield NOORDINARY.equals(situation) ? "INDNORDSADR" : null;
            }
            case "OTHERADRESS" -> {
                if (ORDINARY.equals(situation)) yield "INDORDOADR";
                yield NOORDINARY.equals(situation) ? "INDNORDOADR" : null;
            }
            default -> null;
        };
    }

    private String outcomeForIndF2F() {
        if (individualStatus == null) return null;
        return switch (individualStatus) {
            case "DCD" -> "INDDCD";
            case "NOIDENT" -> "INDNOIDENT";
            case "NOFIELD" -> "INDNOFIELD";
            case "SAMEADRESS" -> {
                if (ORDINARY.equals(situation)) yield "INDORDSADR";
                yield "-".equals(interviewerCanProcess) ? "INDNORDSADR" : null;
            }
            case "OTHERADRESS" -> {
                if ("NO".equals(interviewerCanProcess)) yield "NOTREAT";
                if ("YES".equals(interviewerCanProcess)) {
                    if (ORDINARY.equals(situation)) yield "INDORDOADR";
                    if (NOORDINARY.equals(situation)) yield "INDNORDOADR";
                }
                yield null;
            }
            default -> null;
        };
    }
}
