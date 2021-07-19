package io.mosip.mds.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

@Component
public class DeviceStatusCheckValidator extends Validator {
	public DeviceStatusCheckValidator() {
		super("DeviceStatusCheckValidator", "Device Status Check Validator");   
	}

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response)
			throws JsonProcessingException, IOException {

		List<Validation> validations = new ArrayList<>();

		if(!Objects.isNull(response))
		{
			DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
			if(!Objects.isNull(deviceInfoResponse))
			{
				// Check for deviceStatus block
				validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceStatus","Ready | Busy | Not Ready | Not Registered",deviceInfoResponse.deviceStatus);
				if(deviceInfoResponse.deviceStatus == null || deviceInfoResponse.deviceStatus.isEmpty())
				{
					commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceStatus,"DeviceInfo response does not contain deviceStatus",CommonConstant.FAILED);
				}
				validations.add(validation);

				validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceStatus","Ready | Busy | Not Ready | Not Registered",deviceInfoResponse.deviceStatus);
				if(!deviceInfoResponse.deviceStatus.equals(CommonConstant.READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.BUSY)
						&& !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
				{
					commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceStatus,"Device info response device status is invalid",CommonConstant.FAILED);
				}
				validations.add(validation);
			}
		}
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
