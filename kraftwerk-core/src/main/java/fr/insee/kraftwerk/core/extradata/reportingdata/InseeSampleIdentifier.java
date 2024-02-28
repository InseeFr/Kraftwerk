package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.StringJoiner;

@Setter
@NoArgsConstructor
public class InseeSampleIdentifier {
  private String rges;
  
  private String numfa;
  
  private String ssech;
  
  @Getter
  private String le;
  
  @Getter
  private String ec;
  
  @Getter
  private String bs;
  
  private String noi;
  
   public String getRges() {
    return String.format("%02d", Integer.valueOf(Integer.parseInt(rges)));
  }
  
  public String getNumfa() {
    return String.format("%06d", Integer.valueOf(Integer.parseInt(numfa)));
  }
  
  public String getSsech() {
    return String.format("%02d", Integer.valueOf(Integer.parseInt(ssech)));
  }

	public String getNoi() {
    return String.format("%02d", Integer.valueOf(Integer.parseInt(noi)));
  }
  
  //Concatenate values rges!!numfa!!ssech!!le!!ec!!bs!!noi
  public String getIdStatInsee() {
	    StringJoiner joiner = new StringJoiner("");
	    joiner.add(getRges());
	    joiner.add(getNumfa());
	    joiner.add(getSsech());
	    joiner.add(getLe());
	    joiner.add(getEc());
	    joiner.add(getBs());
	    joiner.add(getNoi());
	    return joiner.toString();
  }
  
 
}
