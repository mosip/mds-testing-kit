package io.mosip.mds.dto.getresponse;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BiometricTypeDto {
	
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	public String type;
	
	@ApiModelProperty(value = "deviceSubType", required = true)
	public List<String> deviceSubTypes = new ArrayList<>();


}
