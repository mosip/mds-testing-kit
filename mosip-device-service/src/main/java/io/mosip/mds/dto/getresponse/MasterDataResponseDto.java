package io.mosip.mds.dto.getresponse;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MasterDataResponseDto {
	
	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.util.List")
	public List<BiometricTypeDto> biometricType = new ArrayList<>();
	
	@ApiModelProperty(value = "sbiSpecificationVersion", required = true, dataType = "java.util.List<java.lang.String>")
	public List<String> sbiSpecificationVersion = new ArrayList<>();
	
	@ApiModelProperty(value = "purpose", required = true, dataType = "java.util.List<java.lang.String>")
	public List<String> purpose = new ArrayList<>();

}
