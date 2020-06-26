package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class MandatoryDeviceInfoResponseValidator extends Validator {

	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		DeviceInfoResponse deviceInfoResponse = response.deviceInfoResponse;

		// Check for callbackId block
		if(deviceInfoResponse.callbackId == null || deviceInfoResponse.callbackId.isEmpty())
		{
			errors.add("DeviceInfo response does not contain callbackId");
			return errors;
		}

		// Check for certification block
		if(deviceInfoResponse.certification == null || deviceInfoResponse.certification.isEmpty())
		{
			errors.add("DeviceInfo response does not contain certification");
			return errors;
		}

		// Check for deviceCode block
		if(deviceInfoResponse.deviceCode == null || deviceInfoResponse.deviceCode.isEmpty())
		{
			errors.add("DeviceInfo response does not contain deviceCode");
			return errors;
		}
		// Check for deviceId block
		if(deviceInfoResponse.deviceId == null)
		{
			errors.add("DeviceInfo response does not contain deviceId");
			return errors;
		}

		// Check for deviceStatus block
		if(deviceInfoResponse.deviceStatus == null || deviceInfoResponse.deviceStatus.isEmpty())
		{
			errors.add("DeviceInfo response does not contain deviceStatus");
			return errors;
		}

		// TODO Check for deviceSubId block
		if(deviceInfoResponse.deviceSubId == null || deviceInfoResponse.deviceSubId.length == 0)
		{
			errors.add("DeviceInfo response does not contain deviceSubId");
			return errors;
		}

		// Check for digitalId block
		if(deviceInfoResponse.digitalId == null || deviceInfoResponse.digitalId.isEmpty())
		{
			errors.add("DeviceInfo response does not contain digitalId");
			return errors;
		}

		// Check for env block
		if(deviceInfoResponse.env == null || deviceInfoResponse.env.isEmpty())
		{
			errors.add("DeviceInfo response does not contain env");
			return errors;
		}

		// Check for purpose block
		if(deviceInfoResponse.purpose == null || deviceInfoResponse.purpose.isEmpty())
		{
			errors.add("DeviceInfo response does not contain purpose");
			return errors;
		}

		// Check for serviceVersion block
		if(deviceInfoResponse.serviceVersion == null || deviceInfoResponse.serviceVersion.isEmpty())
		{
			errors.add("DeviceInfo response does not contain serviceVersion");
			return errors;
		}

		// TODO Check for specVersion block
		if(deviceInfoResponse.specVersion == null || deviceInfoResponse.specVersion.length == 0)
		{
			errors.add("DeviceInfo response does not contain specVersion");
			return errors;
		}
		
		return errors;
	}

}
