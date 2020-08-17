package io.mosip.mds.dto;

import lombok.Data;

@Data
public class Validation {
	String field;
	String expected;
	String found;
	String message;
	String status;
}