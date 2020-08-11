package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDeviceInfoResponseValidator extends Validator {

	private static ObjectMapper mapper;

	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();

	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	public ValidValueDeviceInfoResponseValidator() {
		super("ValidValueDeviceInfoResponseValidator", "Valid Value Device Info Response Validator");
	}
	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) {
		List<Validation> validations = new ArrayList<>();

		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",response.toString());	
		if(Objects.nonNull(response))
		{
			DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
			validation = commonValidator.setFieldExpected("response.getMdsDecodedResponse()","Expected whole divice info decoded Jsone Response",response.getMdsDecodedResponse().toString());		
			if(Objects.nonNull(deviceInfoResponse))
			{
				//Check for device status
				validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceStatus","\"Ready\" | \"Busy\" | \"Not Ready\" | \"Not Registered\"",deviceInfoResponse.deviceStatus);
				if(!deviceInfoResponse.deviceStatus.equals(CommonConstant.READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.BUSY)
						&& !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
				{
					commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceStatus,"Device info response device status is invalid",CommonConstant.FAILED);
				}
				validations.add(validation);
				//Check for device certification
				validation = commonValidator.setFieldExpected("deviceInfoResponse.certification","\"L0\", \"L1\"",deviceInfoResponse.certification);
				if(!deviceInfoResponse.certification.equals(CommonConstant.L0) && !deviceInfoResponse.certification.equals(CommonConstant.L1))
				{
					commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.certification,"Device info response certification is invalid",CommonConstant.FAILED);
				}
				validations.add(validation);
				//Check for device sub id
				for(Integer subid:deviceInfoResponse.deviceSubId)
				{
					validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceSubId","[0,1,2,3]",subid.toString());
					if(subid != null)
						if( subid < 0 || subid >3)
						{
							commonValidator.setFoundMessageStatus(validation,subid.toString(),"Device info response deviceSubId - "+ subid +" is invalid",CommonConstant.FAILED);
						}
					validations.add(validation);
				}

				// TODO Check for env (if not registered conditions check need to do)
				//deviceInfo.env - "None" if not registered. If registered, 
				//then send the registered enviornment "Staging" | "Developer" | "Pre-Production" | "Production".

				validation = commonValidator.setFieldExpected("deviceInfoResponse.env","\"Staging\" | \"Developer\" | \"Pre-Production\" | \"Production\" | \"None\"",deviceInfoResponse.env);
				if(!deviceInfoResponse.env.equals(CommonConstant.NONE) && !deviceInfoResponse.env.equals(CommonConstant.STAGING) && !deviceInfoResponse.env.equals(CommonConstant.DEVELOPER)
						&& !deviceInfoResponse.env.equals(CommonConstant.PRE_PRODUCTION) && !deviceInfoResponse.env.equals(CommonConstant.PRODUCTION))
				{
					commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.env,"Device info response env is invalid",CommonConstant.FAILED);
				}
				validations.add(validation);

				//Check for purpose
				validation = commonValidator.setFieldExpected("deviceInfoResponse.purpose"," \"Auth\" or \"Registration\" or empty in case the status is \"Not Registered\"",deviceInfoResponse.purpose);
				if((deviceInfoResponse.deviceStatus != CommonConstant.NOT_REGISTERED) && (!deviceInfoResponse.purpose.equals(CommonConstant.AUTH) && !deviceInfoResponse.purpose.equals(CommonConstant.REGISTRATION)))
				{
					commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.purpose,"Device info response purpose is invalid",CommonConstant.FAILED);
				}
				validations.add(validation);

				//TODO check array of spec versions
				//TODO validate errors

				//TODO Check for digital id
				validations=validateDigitalId(deviceInfoResponse,validations);
				return validations;
			}
			else{
				commonValidator.setFoundMessageStatus(validation,"Found Divice info Decoded is null","DeviceInfo response is empty",CommonConstant.FAILED);
			}
			validations.add(validation);
		}else{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	private List<Validation> validateDigitalId(DeviceInfoResponse deviceInfoResponse,List<Validation> validations) {
		//		deviceInfo.digitalId - As defined under the digital id section. 
		//		The digital id will be unsigned if the device is L0 and the the status of the device is "Not Registered".

		CommonValidator commonValidator=new CommonValidator();
		if(deviceInfoResponse.certification.equals(CommonConstant.L0) && deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
			validations = commonValidator.validateDecodedUnSignedDigitalID(deviceInfoResponse.digitalId);
		else
			validations = commonValidator.validateDecodedSignedDigitalID(deviceInfoResponse.digitalId);
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
