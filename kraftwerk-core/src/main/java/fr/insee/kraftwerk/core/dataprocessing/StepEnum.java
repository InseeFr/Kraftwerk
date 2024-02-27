package fr.insee.kraftwerk.core.dataprocessing;

import lombok.Getter;

@Getter
public enum StepEnum {
    BUILD_BINDINGS(1,"BUILD_BINDINGS"),
    UNIMODAL_PROCESSING(2,"UNIMODAL_PROCESSING"),
    MULTIMODAL_PROCESSING(3,"MULTIMODAL_PROCESSING");

	private int stepNumber;

	private String stepLabel;

    StepEnum(int stepNumber, String stepLabel){
        this.stepNumber=stepNumber;
        this.stepLabel=stepLabel;
    }
}
