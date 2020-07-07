package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDeviceInfoResponseValidator extends Validator {
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
		DeviceInfoResponse deviceInfoResponse = response.deviceInfoResponse;
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

		//---------------------------------------------------
		//TODO Check for digital id
		errors=validateDigitalId(deviceInfoResponse,errors);



		return errors;
	}

	private List<String> validateDigitalId(DeviceInfoResponse deviceInfoResponse,List<String> errors) {
		//		deviceInfo.digitalId - As defined under the digital id section. 
		//		The digital id will be unsigned if the device is L0 and the the status of the device is "Not Registered".

		CommonValidator commonValidator=new CommonValidator();
		if(deviceInfoResponse.certification.equals(CommonConstant.L0) && deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
			errors = commonValidator.validateUnSignedDigitalID(deviceInfoResponse.digitalId);
		else
			errors = commonValidator.validateSignedDigitalID(deviceInfoResponse.digitalId);
		return errors;
	}

}
