package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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

    public String getOutcomeSpotting(){
        if (StringUtils.isEmpty(identification)){return null;}
        if ("DESTROY".equals(identification)){return "DESTROY";}
        if ("UNIDENTIFIED".equals(identification)){return "UNIDENTIF";}
        StringBuilder outcomeSpotting = new StringBuilder();
        outcomeSpotting.append(access);
        if ("NOORDINARY".equals(situation)){outcomeSpotting.append("NO");return outcomeSpotting.toString();}
        if ("ABSORBED".equals(situation)){outcomeSpotting.append("ABS");return outcomeSpotting.toString();}
        if ("VACANT".equals(category)){outcomeSpotting.append("VAC");return outcomeSpotting.toString();}
        if ("SECONDARY".equals(category)){outcomeSpotting.append("SEC");return outcomeSpotting.toString();}
        if ("DK".equals(category)){outcomeSpotting.append("DK");}
        if ("PRIMARY".equals(category)){outcomeSpotting.append("PR");}
        if ("OCCASIONAL".equals(category)){outcomeSpotting.append("OCC");}
        if ("IDENTIFIED".equals(occupant)){outcomeSpotting.append("IDENT");}
        if ("UNIDENTIFIED".equals(occupant)){outcomeSpotting.append("UNIDENT");}
        return outcomeSpotting.toString();
    }
}
