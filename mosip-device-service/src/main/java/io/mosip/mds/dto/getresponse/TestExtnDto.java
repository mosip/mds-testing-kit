package io.mosip.mds.dto.getresponse;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidatorDef;
import io.mosip.mds.entitiy.Validator;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class TestExtnDto {

	@ApiModelProperty(value = "method", required = true, dataType = "java.lang.String")
	public String method;

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	public String testId;
	
	@ApiModelProperty(value = "testDescription", required = true, dataType = "java.lang.String")
	public String testDescription;
	
	@ApiModelProperty(value = "requestGenerator", required = true, dataType = "java.lang.String")
	public String requestGenerator;
		
	public List<UIInput> uiInput;

	public List<String> processes;

	public List<String> biometricTypes;

	public List<String> deviceSubTypes;
	
	public List<String> segments;
	
	public List<String> exceptions;
	
	public int deviceSubId;
	
	public int bioCount;
	
	public String uinNumber;
	
	public String baseUrl;
	
	public String idaAuthManagerUrl;
	
	public String idaPublicKeyUrl;
	
	public String authUrl;
	
	public String mispLicenseKey;
	
	public String partnerId;
	
	public String partnerApiKey;
	
	public List<String> mdsSpecVersions;

	public List<ValidatorDef> validatorDefs;

	public List<Validator> validators;
	public void addValidator( Validator validator) {
		if(validators == null) {
			validators=new ArrayList<Validator>();
		}
		validators.add(validator);
	}

	public int requestedScore;
}
