package io.mosip.mds.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestManagerDto {

	public String email;

	@ApiModelProperty(value = "mdsSpecVersion", required = true, dataType = "java.lang.String")
	public String mdsSpecVersion;

	@ApiModelProperty(value = "purpose", required = true, dataType = "java.lang.String")
	public String purpose;

	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	public String biometricType;

	@ApiModelProperty(value = "deviceSubType", required = true, dataType = "java.lang.String")
	public String deviceSubType;
	
	@ApiModelProperty(value = "deviceSubId", required = true, dataType = "java.lang.Integer")
	public int deviceSubId;
	
	@ApiModelProperty(value = "bioCount", required = true, dataType = "java.lang.Integer")
	public int bioCount;
	
	@ApiModelProperty(value = "runName", required = true, dataType = "java.lang.String")
	public String runName;

	public List<String> tests;
	
}
