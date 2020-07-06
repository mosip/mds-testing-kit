package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDiscoverResponseValidator extends Validator {

	private static final String L2 = "L2";
	private static final String L1 = "L1";
	private static final String L0 = "L0";
	private static final String REGISTRATION = "Registration";
	private static final String AUTH = "Auth";
	private static final String NOT_REGISTERED = "Not Registered";
	private static final String NOT_READY = "Not Ready";
	private static final String BUSY = "Busy";
	private static final String READY = "Ready";
	public ValidValueDiscoverResponseValidator() {
		super("ValidValueDiscoverResponseValidator", "Valid Value Discover Response Validator");
	}
	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		DiscoverResponse discoverResponse = response.discoverResponse;

		//Check for device status
		if(!discoverResponse.deviceStatus.equals(READY) && !discoverResponse.deviceStatus.equals(BUSY)
				&& !discoverResponse.deviceStatus.equals(NOT_READY) && !discoverResponse.deviceStatus.equals(NOT_REGISTERED))
		{
			errors.add("Device discover response device status is invalid");
			return errors;
		}
		//Check for device certification
		if(!discoverResponse.certification.equals(L0)  && !discoverResponse.certification.equals(L1) && !discoverResponse.certification.equals(L2))
		{
			errors.add("Device discover response certification is invalid");
			return errors;
		}
		//Check for device sub id
		for(Integer subid:discoverResponse.deviceSubId)
		{
			if(subid != null)
				if(subid < 0 && subid >3)
				{
					errors.add("Device discover response deviceSubId - "+ subid +" is invalid");
					return errors;
				}
		}

		//Check for purpose
		if(!discoverResponse.purpose.equals(AUTH) && !discoverResponse.purpose.equals(REGISTRATION))
		{
			errors.add("Device discover response purpose is invalid");
			return errors;
		}

		//TODO Check for digital id
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.
		errors=validateDigitalId(discoverResponse,errors);

		//TODO check array of spec versions
		//TODO validate errors
		return errors;
	}

	private List<String> validateDigitalId(DiscoverResponse discoverResponse, List<String> errors) {
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.

		CommonValidator commonValidator=new CommonValidator();
		errors = commonValidator.validateUnSignedDigitalID(discoverResponse.digitalId);
		return errors;
	}
}
