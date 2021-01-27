package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

@Component
public class ValidValueDiscoverResponseValidator extends Validator {

	public ValidValueDiscoverResponseValidator() {
		super("ValidValueDiscoverResponseValidator", "Valid Value Discover Response Validator");
	}

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	private ObjectMapper jsonMapper;

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations=new ArrayList<Validation>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",jsonMapper.writeValueAsString(response));		
		if(Objects.nonNull(response))
		{
			validation = commonValidator.setFieldExpected("DecodedResponse","Expected whole discover decoded Jsone Response",jsonMapper.writeValueAsString(response.getMdsDecodedResponse()));		
			DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
			if(Objects.isNull(discoverResponse))
			{
				commonValidator.setFoundMessageStatus(validation,"Found Discover Decoded is null","Discover response is empty",CommonConstant.FAILED);
			}
			validations.add(validation);

			//check Device status 
			validations = validateDeviceStatus(response,discoverResponse,validations);

			//Check for device status
			validation = commonValidator.setFieldExpected("discoverResponse.deviceStatus","Ready | Busy | Not Ready | Not Registered",discoverResponse.deviceStatus);		
			if(!discoverResponse.deviceStatus.equals(CommonConstant.READY) && !discoverResponse.deviceStatus.equals(CommonConstant.BUSY)
					&& !discoverResponse.deviceStatus.equals(CommonConstant.NOT_READY) && !discoverResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
			{
				commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceStatus,"device status is not matching",CommonConstant.FAILED);
			}
			validations.add(validation);
			//Check for device certification
			validation = commonValidator.setFieldExpected("discoverResponse.certification","L0 | L1 | L2",discoverResponse.certification);		
			if(!discoverResponse.certification.equals(CommonConstant.L0)  && !discoverResponse.certification.equals(CommonConstant.L1) && !discoverResponse.certification.equals(CommonConstant.L2))
			{
				commonValidator.setFoundMessageStatus(validation,discoverResponse.certification,"Device discover response certification is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);
			//Check for device sub id
			for(Integer subid:discoverResponse.deviceSubId)
			{
				validation = commonValidator.setFieldExpected("discoverResponse.deviceSubId","[0/1/2/3]",Arrays.toString(discoverResponse.deviceSubId));		
				if(subid != null)
					if(subid < 0 && subid >3)
					{
						commonValidator.setFoundMessageStatus(validation,Arrays.toString(discoverResponse.deviceSubId),"Device discover response deviceSubId - "+ subid +" is invalid",CommonConstant.FAILED);
					}
				validations.add(validation);
			}

			//Check for purpose
			validation = commonValidator.setFieldExpected("discoverResponse.purpose","Auth | Registration",discoverResponse.purpose);
			if(!discoverResponse.purpose.equals(CommonConstant.AUTH) && !discoverResponse.purpose.equals(CommonConstant.REGISTRATION))
			{
				commonValidator.setFoundMessageStatus(validation,discoverResponse.purpose,"Device discover response purpose is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);

			//TODO Check for digital id
			//digitalId - Digital ID as per the Digital ID definition but it will not be signed.
			validations=validateDigitalId(discoverResponse,validations);

			//TODO check array of spec versions
			//TODO validate errors
		}
		else
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	private List<Validation> validateDeviceStatus(ValidateResponseRequestDto response, DiscoverResponse discoverResponse, List<Validation> validations) {
		
				validation = commonValidator.setFieldExpected("Device Type",response.getTestManagerDto().biometricType,discoverResponse.digitalIdDecoded.type);	

				if((!"Biometric Device".equals(response.getTestManagerDto().biometricType))
						&& (!response.getTestManagerDto().biometricType.equals(discoverResponse.digitalIdDecoded.type))) {
					commonValidator.setFoundMessageStatus(validation,discoverResponse.digitalIdDecoded.type,"Response from different device is returning",CommonConstant.FAILED);
				}
				validations.add(validation);
			
		return validations;
	}

	private List<Validation> validateDigitalId(DiscoverResponse discoverResponse, List<Validation> validations) {
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.
		validations = commonValidator.validateDecodedUnSignedDigitalID(discoverResponse.digitalId,validations);
		return validations;
	}

	@Override
	protected boolean checkVersionSupport(String version) {
		//TODO
		if(version.equals("0.9.5"))
			return true;

		return false;
	}
	@Override
	protected String supportedVersion() {
		// TODO return type of mds spec version supported
		return "0.9.5";
	}
}
