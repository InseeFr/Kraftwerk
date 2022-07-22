package fr.insee.kraftwerk.core.extradata.reportingdata;

public class InseeSampleIdentiers {
  private String rges;
  
  private String numfa;
  
  private String ssech;
  
  private String le;
  
  private String ec;
  
  private String bs;
  
  private String noi;
  
  public InseeSampleIdentiers() {}
  
  public InseeSampleIdentiers(String rges, String numfa, String ssech) {
    this.rges = rges;
    this.numfa = numfa;
    this.ssech = ssech;
  }
  
  public InseeSampleIdentiers(String rges, String numfa, String ssech, String le, String ec, String bs, String noi) {
    this.rges = rges;
    this.numfa = numfa;
    this.ssech = ssech;
    this.le = le;
    this.ec = ec;
    this.bs = bs;
    this.noi = noi;
  }
  
  public String getRges() {
    return this.rges;
  }
  
  public void setRges(String rges) {
    this.rges = rges;
  }
  
  public String getNumfa() {
    return this.numfa;
  }
  
  public void setNumfa(String numfa) {
    this.numfa = numfa;
  }
  
  public String getSsech() {
    return this.ssech;
  }
  
  public void setSsech(String ssech) {
    this.ssech = ssech;
  }
  
  public String getLe() {
    return this.le;
  }
  
  public void setLe(String le) {
    this.le = le;
  }
  
  public String getEc() {
    return this.ec;
  }
  
  public void setEc(String ec) {
    this.ec = ec;
  }
  
  public String getBs() {
    return this.bs;
  }
  
  public void setBs(String bs) {
    this.bs = bs;
  }
  
  public String getNoi() {
    return this.noi;
  }
  
  public void setNoi(String noi) {
    this.noi = noi;
  }
}
