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
    private String identification;
    private String access;
    private String situation;
    private String category;
    private String occupant;
    private String individualStatus;
    private String interviewerCanProcess;


    private static final Set<String> CONFIG_IASCO = Set.of("IASCO", "HOUSEF2F");
    private static final Set<String> CONFIG_PHONE = Set.of("INDTEL", "INTELNOR");
    private static final Set<String> CONFIG_F2F = Set.of("INDF2F", "INF2FNOR");
    private static final String ORDINARY = "ORDINARY";
    private static final String NOORDINARY = "NOORDINARY";

    public String getOutcomeSpotting(String identificationConfiguration) {
        if (CONFIG_IASCO.contains(identificationConfiguration)) {
            return outcomeForHouseF2F();
        } else if (CONFIG_PHONE.contains(identificationConfiguration)) {
            return outcomeForPhone();
        } else if (CONFIG_F2F.contains(identificationConfiguration)) {
            return outcomeForFaceToFace();
        }
        return null;
    }
    

    public String outcomeForHouseF2F(){
        if (StringUtils.isEmpty(identification)) {
            return null;
        }
        if ("DESTROY".equals(identification)) {
            return "DESTROY";
        }
        if ("UNIDENTIFIED".equals(identification)) {
            return "UNIDENTIF";
        }
        StringBuilder outcomeSpotting = new StringBuilder();
        outcomeSpotting.append(access);
        if (NOORDINARY.equals(situation)) {
            outcomeSpotting.append("NO");
            return outcomeSpotting.toString();
        }
        if ("ABSORBED".equals(situation)) {
            outcomeSpotting.append("ABS");
            return outcomeSpotting.toString();
        }
        if ("VACANT".equals(category)) {
            outcomeSpotting.append("VAC");
            return outcomeSpotting.toString();
        }
        if ("SECONDARY".equals(category)) {
            outcomeSpotting.append("SEC");
            return outcomeSpotting.toString();
        }
        if ("DK".equals(category)) {
            outcomeSpotting.append("DK");
        }
        if ("PRIMARY".equals(category)) {
            outcomeSpotting.append("PR");
        }
        if ("OCCASIONAL".equals(category)) {
            outcomeSpotting.append("OCC");
        }
        if ("IDENTIFIED".equals(occupant)) {
            outcomeSpotting.append("IDENT");
        }
        if ("UNIDENTIFIED".equals(occupant)) {
            outcomeSpotting.append("UNIDENT");
        }
        return outcomeSpotting.toString();
    }

    private String outcomeForPhone() {
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
                    yield NOORDINARY.equals(situation) ? "INDNORDOADR" : null;}
            default -> null;
        };
    }

    private String outcomeForFaceToFace() {
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
