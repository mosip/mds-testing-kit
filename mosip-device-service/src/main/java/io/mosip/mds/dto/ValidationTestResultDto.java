package io.mosip.mds.dto;

import java.util.List;

import lombok.Data;

@Data
public class ValidationTestResultDto {

	private String decodedData;
	private List<Validation> validations;
	
}
/*
 * 
 * {
	decodedData: {},
	validations: []
}*/
 