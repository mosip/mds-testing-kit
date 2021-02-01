package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestManagerGetDto {

	@ApiModelProperty(value = "mdsSpecificationVersion", required = true, dataType = "java.lang.String")
	public String mdsSpecificationVersion;
	
	@ApiModelProperty(value = "process", required = true, dataType = "java.lang.String")
	public String process;
	
	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	public String biometricType;
	
	@ApiModelProperty(value = "deviceSubType", required = true, dataType = "java.lang.String")
	public String deviceSubType;
}
