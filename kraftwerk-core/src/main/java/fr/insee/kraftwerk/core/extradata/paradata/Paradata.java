package fr.insee.kraftwerk.core.extradata.paradata;

import java.nio.file.Path;
import java.util.List;

public class Paradata {
	private Path filepath;

	private List<ParaDataUE> listParadataUE;

	public Paradata() {
	}

	public Paradata(Path filepath) {
		this.filepath = filepath;
	}

	public Path getFilepath() {
		return this.filepath;
	}

	public void setFilepath(Path filepath) {
		this.filepath = filepath;
	}

	public List<ParaDataUE> getListParadataUE() {
		return this.listParadataUE;
	}

	public ParaDataUE getParadataUE(String identifier) {
		return this.listParadataUE.stream()
				.filter(paradataUEToSearch -> identifier.equals(paradataUEToSearch.getIdentifier()))
				.findAny().orElse(null);
	}

	public void setListParadataUE(List<ParaDataUE> listParadataUE) {
		this.listParadataUE = listParadataUE;
	}
}
