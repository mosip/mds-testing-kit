package io.mosip.mds.validator;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.DeviceInfoMinimal;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDeviceInfoResponseValidator extends Validator {
	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	public ValidValueDeviceInfoResponseValidator() {
		super("ValidValueDeviceInfoResponseValidator", "Valid Value Device Info Response Validator");
	}
	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();

		if(Objects.isNull(response))
		{
			errors.add("Response is empty");
			return errors;
		}


		DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
		if(Objects.isNull(deviceInfoResponse))
		{
			errors.add("DeviceInfo response is empty");
			return errors;
		}

		//Check for device status
		if(!deviceInfoResponse.deviceStatus.equals(CommonConstant.READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.BUSY)
				&& !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
		{
			errors.add("Device info response device status is invalid");
			return errors;
		}
		//Check for device certification
		if(!deviceInfoResponse.certification.equals(CommonConstant.L0) && !deviceInfoResponse.certification.equals(CommonConstant.L1))
		{
			errors.add("Device info response certification is invalid");
			return errors;
		}
		//Check for device sub id
		for(Integer subid:deviceInfoResponse.deviceSubId)
		{
			if(subid != null)
				if( subid < 0 || subid >3)
				{
					errors.add("Device info response deviceSubId - "+ subid +" is invalid");
					return errors;
				}
		}

		// TODO Check for env (if not registered conditions check need to do)
		//deviceInfo.env - "None" if not registered. If registered, 
		//then send the registered enviornment "Staging" | "Developer" | "Pre-Production" | "Production".

		if(!deviceInfoResponse.env.equals(CommonConstant.NONE) && !deviceInfoResponse.env.equals(CommonConstant.STAGING) && !deviceInfoResponse.env.equals(CommonConstant.DEVELOPER)
				&& !deviceInfoResponse.env.equals(CommonConstant.PRE_PRODUCTION) && !deviceInfoResponse.env.equals(CommonConstant.PRODUCTION))
		{
			errors.add("Device info response env is invalid");
			return errors;
		}

		//Check for purpose
		if(!deviceInfoResponse.purpose.equals(CommonConstant.AUTH) && !deviceInfoResponse.purpose.equals(CommonConstant.REGISTRATION))
		{
			errors.add("Device info response purpose is invalid");
			return errors;
		}

		//TODO check array of spec versions
		//TODO validate errors

		//TODO Check for digital id
		errors=validateDigitalId(deviceInfoResponse,errors);

		return errors;
	}

	private List<String> validateDigitalId(DeviceInfoResponse deviceInfoResponse,List<String> errors) {
		//		deviceInfo.digitalId - As defined under the digital id section. 
		//		The digital id will be unsigned if the device is L0 and the the status of the device is "Not Registered".

		CommonValidator commonValidator=new CommonValidator();
		if(deviceInfoResponse.certification.equals(CommonConstant.L0) && deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
			errors = commonValidator.validateDecodedUnSignedDigitalID(deviceInfoResponse.digitalId);
		else
			errors = commonValidator.validateDecodedSignedDigitalID(deviceInfoResponse.digitalId);
		return errors;
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
