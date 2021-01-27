package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

@Component
public class MandatoryDeviceInfoResponseValidator extends Validator {
	public MandatoryDeviceInfoResponseValidator() {
		super("MandatoryDeviceInfoResponseValidator", "Mandatory DeviceInfo Response Validator");   
	}

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	private ObjectMapper jsonMapper;

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations = new ArrayList<>();

		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",jsonMapper.writeValueAsString(response));		
		if(Objects.isNull(response))
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);

		validation = commonValidator.setFieldExpected("response.getDecodedResponse()","Expected whole divice info decoded Jsone Response",jsonMapper.writeValueAsString(response.getMdsDecodedResponse()));		
		DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
		if(Objects.isNull(deviceInfoResponse))
		{
			commonValidator.setFoundMessageStatus(validation,"Found Divice info Decoded is null","DeviceInfo response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		// Check for callbackId block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.callbackId","Base URL to communicate",deviceInfoResponse.callbackId);
		if(deviceInfoResponse.callbackId == null || deviceInfoResponse.callbackId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.callbackId,"Device info response callbackId is empty",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for certification block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.certification","\"L0\", \"L1\" or \"L2\" based on the level of certification.",deviceInfoResponse.certification);
		if(deviceInfoResponse.certification == null || deviceInfoResponse.certification.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.certification,"DeviceInfo response does not contain certification",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceCode block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceCode","A unique code given by MOSIP after successful registration",deviceInfoResponse.deviceCode);
		if(deviceInfoResponse.deviceCode == null || deviceInfoResponse.deviceCode.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceCode,"DeviceInfo response does not contain deviceCode",CommonConstant.FAILED);
		}
		validations.add(validation);
		// Check for deviceId block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceId","Internal ID",deviceInfoResponse.deviceId);
		if(deviceInfoResponse.deviceId == null || deviceInfoResponse.deviceId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceId,"DeviceInfo response does not contain deviceId",CommonConstant.FAILED);
		}
		validations.add(validation);

		//firmware
		validation = commonValidator.setFieldExpected("deviceInfoResponse.firmware","Exact version of the firmware",deviceInfoResponse.firmware);
		if(deviceInfoResponse.firmware == null || deviceInfoResponse.firmware.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.firmware,"DeviceInfo response does not contain firmware",CommonConstant.FAILED);
		}
		validations.add(validation);

		
		// Check for deviceStatus block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceStatus","Ready | Busy | Not Ready | Not Registered",deviceInfoResponse.deviceStatus);
		if(deviceInfoResponse.deviceStatus == null || deviceInfoResponse.deviceStatus.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceStatus,"DeviceInfo response does not contain deviceStatus",CommonConstant.FAILED);
			validations.add(validation);
		}else if(!(!deviceInfoResponse.deviceStatus.equals(CommonConstant.READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.BUSY)
				&& !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_READY) && !deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))){
			validations.add(validation);
			// Check for deviceCode block
			validation = commonValidator.setFieldExpected("discoverResponse.deviceCode","A unique code given by MOSIP after successful registration",deviceInfoResponse.deviceCode);
			if(!deviceInfoResponse.deviceStatus.equals("Not Registered") && (deviceInfoResponse.deviceCode == null || deviceInfoResponse.deviceCode.isEmpty()))
			{
				commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceCode,"Device Discover response does not contain deviceCode",CommonConstant.FAILED);
			}else if(deviceInfoResponse.deviceStatus.equals("Not Registered") && (deviceInfoResponse.deviceCode != null && (!deviceInfoResponse.deviceCode.isEmpty()))) {
				commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.deviceCode,"Device Discover response deviceCode should be empty when deviceStatus is Not Registered",CommonConstant.FAILED);
			}
			validations.add(validation);
		}else {
			validations.add(validation);
		}

		// TODO Check for deviceSubId block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.deviceSubId","[0,1,2,3]",Arrays.toString(deviceInfoResponse.deviceSubId));
		if(deviceInfoResponse.deviceSubId == null || deviceInfoResponse.deviceSubId.length == 0)
		{
			commonValidator.setFoundMessageStatus(validation,Arrays.toString(deviceInfoResponse.deviceSubId),"DeviceInfo response does not contain deviceSubId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for digitalId block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.digitalId","As defined under the digital id section",deviceInfoResponse.digitalId);
		if(deviceInfoResponse.digitalId == null || deviceInfoResponse.digitalId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.digitalId,"DeviceInfo response does not contain digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for env block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.env","Staging | Developer | Pre-Production | Production | None",deviceInfoResponse.env);
		if(deviceInfoResponse.env == null || deviceInfoResponse.env.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.env,"DeviceInfo response does not contain env",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for purpose block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.purpose"," Auth or Registration or empty in case the status is Not Registered",deviceInfoResponse.purpose);
		if( (deviceInfoResponse.deviceStatus != CommonConstant.NOT_REGISTERED) && (deviceInfoResponse.purpose == null || deviceInfoResponse.purpose.isEmpty()))
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.purpose,"DeviceInfo response does not contain purpose",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for serviceVersion block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.serviceVersion","Device service version",deviceInfoResponse.serviceVersion);
		if(deviceInfoResponse.serviceVersion == null || deviceInfoResponse.serviceVersion.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,deviceInfoResponse.serviceVersion,"DeviceInfo response does not contain serviceVersion",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for specVersion block
		validation = commonValidator.setFieldExpected("deviceInfoResponse.specVersion","Array of supported SBI specification version",Arrays.toString(deviceInfoResponse.specVersion));
		if(deviceInfoResponse.specVersion == null || deviceInfoResponse.specVersion.length == 0)
		{
			commonValidator.setFoundMessageStatus(validation,Arrays.toString(deviceInfoResponse.specVersion),"DeviceInfo response does not contain specVersion",CommonConstant.FAILED);
		}
		validations.add(validation);

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
