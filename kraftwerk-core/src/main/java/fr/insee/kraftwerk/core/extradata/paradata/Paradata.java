package fr.insee.kraftwerk.core.extradata.paradata;

import java.nio.file.Path;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class Paradata {
	@Getter@Setter
	private Path filepath;

	@Getter@Setter
	private List<ParaDataUE> listParadataUE;

	public Paradata(Path filepath) {
		this.filepath = filepath;
	}

	public ParaDataUE getParadataUE(String identifier) {
		return this.listParadataUE.stream()
				.filter(paradataUEToSearch -> identifier.equals(paradataUEToSearch.getIdentifier()))
				.findAny().orElse(null);
	}

}
