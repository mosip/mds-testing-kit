package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

public class MandatoryDiscoverResponseValidator  extends Validator {
	public MandatoryDiscoverResponseValidator() {
		super("MandatoryDiscoverResponseValidator", "Mandatory Discover Response Validator");
	}

	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) {

		List<Validation> validations = new ArrayList<>();		
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",response.toString());		
		if(Objects.isNull(response))
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
		validation = commonValidator.setFieldExpected("mdsDecodedResponse","Expected whole discover decoded Jsone Response",response.getMdsDecodedResponse().toString());
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

		// Check for deviceCode block
		validation = commonValidator.setFieldExpected("discoverResponse.deviceCode","A unique code given by MOSIP after successful registration",discoverResponse.deviceCode);
		if(discoverResponse.deviceCode == null || discoverResponse.deviceCode.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceCode,"Device Discover response does not contain deviceCode",CommonConstant.FAILED);
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
		validation = commonValidator.setFieldExpected("discoverResponse.deviceStatus","\"Ready\" | \"Busy\" | \"Not Ready\" | \"Not Registered\"",discoverResponse.deviceStatus);
		if(discoverResponse.deviceStatus == null || discoverResponse.deviceStatus.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceStatus,"Device Discover response does not contain deviceStatus",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for deviceSubId block
		validation = commonValidator.setFieldExpected("discoverResponse.deviceSubId","[0/1/2/3]",discoverResponse.toString());
		if(discoverResponse.deviceSubId == null || discoverResponse.deviceSubId.length == 0)
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceSubId.toString(),"Device Discover response does not contain deviceSubId",CommonConstant.FAILED);
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
		validation = commonValidator.setFieldExpected("discoverResponse.purpose","\"Auth\" | \"Registration\"",discoverResponse.purpose);
		if(discoverResponse.purpose == null || discoverResponse.purpose.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.purpose,"Device Discover response does not contain purpose",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for specVersion block
		validation = commonValidator.setFieldExpected("discoverResponse.specVersion","Array of supported MDS specification version",discoverResponse.specVersion.toString());
		if(discoverResponse.specVersion == null || discoverResponse.specVersion.length == 0)
		{
			commonValidator.setFoundMessageStatus(validation,discoverResponse.specVersion.toString(),"Device Discover response does not contain specVersion",CommonConstant.FAILED);
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
