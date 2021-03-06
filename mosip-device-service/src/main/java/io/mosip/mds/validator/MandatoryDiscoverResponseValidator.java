package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

@Component
public class MandatoryDiscoverResponseValidator  extends Validator {
	public MandatoryDiscoverResponseValidator() {
		super("MandatoryDiscoverResponseValidator", "Mandatory Discover Response Validator");
	}

	private Validation validation = new Validation();
	
	@Autowired
	private CommonValidator commonValidator;

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {

		List<Validation> validations = new ArrayList<>();		
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",CommonConstant.DATA);		
		if(Objects.isNull(response))
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
		validation = commonValidator.setFieldExpected("DecodedResponse","Expected whole discover decoded Jsone Response",CommonConstant.DATA);
		if(Objects.isNull(discoverResponse))
		{
			commonValidator.setFoundMessageStatus(validation,"Found Discover Decoded is null","Discover response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		// Check for callbackId block
		validation = commonValidator.setFieldExpected("discoverResponse.callbackId","Base URL to reach to the device",discoverResponse.callbackId);
		if(discoverResponse.callbackId == null || discoverResponse.callbackId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.callbackId,"Device Discover response does not contain callbackId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for certification block
		validation = commonValidator.setFieldExpected("discoverResponse.certification","L0 | L1 | L2",discoverResponse.certification);
		if(discoverResponse.certification == null || discoverResponse.certification.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.certification,"Device Discover response does not contain certification",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceId block
		validation = commonValidator.setFieldExpected("discoverResponse.deviceId","Internal ID",discoverResponse.deviceId);
		if(discoverResponse.deviceId == null)
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceId,"Device Discover response does not contain deviceId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceStatus block
		validation = commonValidator.setFieldExpected("discoverResponse.deviceStatus","Ready | Busy | Not Ready | Not Registered",discoverResponse.deviceStatus);
		if(discoverResponse.deviceStatus == null || discoverResponse.deviceStatus.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceStatus,"Device Discover response does not contain deviceStatus",CommonConstant.FAILED);
			validations.add(validation);
		}else if(!(!discoverResponse.deviceStatus.equals(CommonConstant.READY) && !discoverResponse.deviceStatus.equals(CommonConstant.BUSY)
				&& !discoverResponse.deviceStatus.equals(CommonConstant.NOT_READY) && !discoverResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))){
			validations.add(validation);
			// Check for deviceCode block
			validation = commonValidator.setFieldExpected("discoverResponse.deviceCode","A unique code given by MOSIP after successful registration",discoverResponse.deviceCode);
			if(!discoverResponse.deviceStatus.equals("Not Registered") && (discoverResponse.deviceCode == null || discoverResponse.deviceCode.isEmpty()))
			{
				commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceCode,"Device Discover response does not contain deviceCode",CommonConstant.FAILED);
			}else if(discoverResponse.deviceStatus.equals("Not Registered") && (discoverResponse.deviceCode != null && (!discoverResponse.deviceCode.isEmpty()))) {
				commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceCode,"Device Discover response deviceCode should be empty when deviceStatus is Not Registered",CommonConstant.FAILED);
			}
			validations.add(validation);
		}

		// TODO Check for deviceSubId block
		validation = commonValidator.setFieldExpected("discoverResponse.deviceSubId","[0/1/2/3]",Arrays.toString(discoverResponse.deviceSubId));
		if(discoverResponse.deviceSubId == null || discoverResponse.deviceSubId.length == 0)
		{
			commonValidator.setFoundMessageStatus(validation,Arrays.toString(discoverResponse.deviceSubId),"Device Discover response does not contain deviceSubId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for digitalId block
		validation = commonValidator.setFieldExpected("discoverResponse.digitalId","UnSigned DigitalId",discoverResponse.digitalId);
		if(discoverResponse.digitalId == null || discoverResponse.digitalId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.digitalId,"Device Discover response does not contain digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for purpose block
		validation = commonValidator.setFieldExpected("discoverResponse.purpose","Auth | Registration",discoverResponse.purpose);
		if(discoverResponse.purpose == null || discoverResponse.purpose.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.purpose,"Device Discover response does not contain purpose",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for specVersion block
		validation = commonValidator.setFieldExpected("discoverResponse.specVersion","Array of supported SBI specification version",Arrays.toString(discoverResponse.specVersion));
		if(discoverResponse.specVersion == null || discoverResponse.specVersion.length == 0)
		{
			commonValidator.setFoundMessageStatus(validation,Arrays.toString(discoverResponse.specVersion),"Device Discover response does not contain specVersion",CommonConstant.FAILED);
		}
		validations.add(validation);
		
		validation = commonValidator.setFieldExpected("discoverResponse.serviceVersion","Device service version",discoverResponse.serviceVersion);
		if(discoverResponse.serviceVersion == null || discoverResponse.serviceVersion.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.serviceVersion,"Device Discover response does not contain serviceVersion",CommonConstant.FAILED);
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
