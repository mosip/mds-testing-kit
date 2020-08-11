package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDiscoverResponseValidator extends Validator {

	public ValidValueDiscoverResponseValidator() {
		super("ValidValueDiscoverResponseValidator", "Valid Value Discover Response Validator");
	}

	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) {
		List<Validation> validations=new ArrayList<Validation>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",response.toString());		
		if(Objects.nonNull(response))
		{
			validation = commonValidator.setFieldExpected("mdsDecodedResponse","Expected whole discover decoded Jsone Response",response.getMdsDecodedResponse().toString());		
			DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
			if(Objects.isNull(discoverResponse))
			{
				commonValidator.setFoundMessageStatus(validation,"Found Discover Decoded is null","Discover response is empty",CommonConstant.FAILED);
			}
			validations.add(validation);
			//Check for device status
			validation = commonValidator.setFieldExpected("discoverResponse.deviceStatus","Ready\" | \"Busy\" | \"Not Ready\" | \"Not Registered",discoverResponse.deviceStatus);		
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
				validation = commonValidator.setFieldExpected("discoverResponse.deviceSubId","[0/1/2/3]",discoverResponse.deviceSubId.toString());		
				if(subid != null)
					if(subid < 0 && subid >3)
					{
						commonValidator.setFoundMessageStatus(validation,discoverResponse.deviceSubId.toString(),"Device discover response deviceSubId - "+ subid +" is invalid",CommonConstant.FAILED);
					}
				validations.add(validation);
			}

			//Check for purpose
			validation = commonValidator.setFieldExpected("discoverResponse.purpose","\"Auth\" | \"Registration\"",discoverResponse.purpose);
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

	private List<Validation> validateDigitalId(DiscoverResponse discoverResponse, List<Validation> validations) {
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.

		CommonValidator commonValidator=new CommonValidator();
		validations = commonValidator.validateDecodedUnSignedDigitalID(discoverResponse.digitalId);
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
