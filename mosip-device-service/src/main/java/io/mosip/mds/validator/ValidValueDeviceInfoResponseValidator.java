package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDeviceInfoResponseValidator extends Validator {
	private static final String L1 = "L1";
	private static final String L0 = "L0";
	private static final String REGISTRATION = "Registration";
	private static final String AUTH = "Auth";
	private static final String PRODUCTION = "Production";
	private static final String PRE_PRODUCTION = "Pre-Production";
	private static final String DEVELOPER = "Developer";
	private static final String STAGING = "Staging";
	private static final String NONE = "None";
	private static final String NOT_REGISTERED = "Not Registered";
	private static final String NOT_READY = "Not Ready";
	private static final String BUSY = "Busy";
	private static final String READY = "Ready";

	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		DeviceInfoResponse deviceInfoResponse = response.deviceInfoResponse;

		//Check for device status
		if(deviceInfoResponse.deviceStatus != READY && deviceInfoResponse.deviceStatus != BUSY
				&& deviceInfoResponse.deviceStatus != NOT_READY && deviceInfoResponse.deviceStatus != NOT_REGISTERED)
		{
			errors.add("Device info response device status is invalid");
			return errors;
		}
		//Check for device certification
		if(deviceInfoResponse.certification != L0 && deviceInfoResponse.certification != L1)
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

		if(deviceInfoResponse.env != NONE && deviceInfoResponse.env != STAGING && deviceInfoResponse.env != DEVELOPER
				&& deviceInfoResponse.env != PRE_PRODUCTION && deviceInfoResponse.env != PRODUCTION)
		{
			errors.add("Device info response env is invalid");
			return errors;
		}

		//Check for purpose
		if(deviceInfoResponse.purpose != AUTH && deviceInfoResponse.purpose != REGISTRATION)
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
		if(deviceInfoResponse.certification == L0 && deviceInfoResponse.deviceStatus == NOT_REGISTERED)
		errors = commonValidator.validateUnSignedDigitalID(deviceInfoResponse.digitalId);
		else
			errors = commonValidator.validateSignedDigitalID(deviceInfoResponse.digitalId);
		return errors;
	}

}
