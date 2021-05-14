package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestManagerGetDto {

	@ApiModelProperty(value = "sbiSpecificationVersion", required = true, dataType = "java.lang.String")
	public String sbiSpecificationVersion;
	
	@ApiModelProperty(value = "purpose", required = true, dataType = "java.lang.String")
	public String purpose;
	
	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	public String biometricType;
	
	@ApiModelProperty(value = "deviceSubType", required = true, dataType = "java.lang.String")
	public String deviceSubType;
}
