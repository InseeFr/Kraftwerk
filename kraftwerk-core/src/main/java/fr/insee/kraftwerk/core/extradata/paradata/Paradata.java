package fr.insee.kraftwerk.core.extradata.paradata;

import java.nio.file.Path;
import java.util.List;

public class Paradata {

	private Path filepath;
	
	public List<ParaDataUE> listParadataUE;

	public Paradata() {
		super();
	}

	public Paradata(Path filepath) {
		super();
		this.filepath = filepath;
	}

	public Path getFilepath() {
		return filepath;
	}

	public void setFilepath(Path filepath) {
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
