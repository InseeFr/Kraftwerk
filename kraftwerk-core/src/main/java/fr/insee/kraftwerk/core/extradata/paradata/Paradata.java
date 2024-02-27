package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

@Getter
@NoArgsConstructor
public class Paradata {
	@Setter
	private Path filepath;

	@Setter
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
