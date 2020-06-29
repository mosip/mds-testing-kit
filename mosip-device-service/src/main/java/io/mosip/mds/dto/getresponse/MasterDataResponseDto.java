package io.mosip.mds.dto.getresponse;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MasterDataResponseDto {
	
	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.util.List")
	public List<BiometricTypeDto> biometricType = new ArrayList<>();
	
	@ApiModelProperty(value = "mdsSpecificationVersion", required = true, dataType = "java.util.List<java.lang.String>")
	public List<String> mdsSpecificationVersion = new ArrayList<>();
	
	@ApiModelProperty(value = "process", required = true, dataType = "java.util.List<java.lang.String>")
	public List<String> process = new ArrayList<>();

}
