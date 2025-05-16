package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ReportingDataClosingCause {
    ClosingCauseValue closingCauseValue;
    LocalDateTime closingCauseDate;
}
