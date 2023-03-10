package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.StringJoiner;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class InseeSampleIdentifier {
  private String rges;
  
  private String numfa;
  
  private String ssech;
  
  private String le;
  
  private String ec;
  
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
  
  public String getLe() {
    return this.le;
  }
  
  public String getEc() {
    return this.ec;
  }

    public String getBs() {
    return this.bs;
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
