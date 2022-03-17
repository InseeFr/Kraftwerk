package fr.insee.kraftwerk.extradata.paradata;

import java.util.List;

public class Paradata {

	private String filepath;
	
	public List<ParaDataUE> listParadataUE;

	public Paradata() {
		super();
	}

	public Paradata(String filepath) {
		super();
		this.filepath = filepath;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public List<ParaDataUE> getListParadataUE() {
		return listParadataUE;
	}

	public ParaDataUE getParadataUE(String identifier) {
		ParaDataUE paradataUE = listParadataUE.stream()
				.filter(paradataUEToSearch -> identifier
						.equals(paradataUEToSearch.getIdentifier()))
				.findAny().orElse(null);
		return paradataUE;
	}

	public void setListParadataUE(List<ParaDataUE> listParadataUE) {
		this.listParadataUE = listParadataUE;
	}
	
}
