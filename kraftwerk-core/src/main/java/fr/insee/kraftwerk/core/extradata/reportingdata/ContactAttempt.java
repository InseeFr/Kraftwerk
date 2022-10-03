package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactAttempt {
  private String status;
  private long timestamp;
  
}
