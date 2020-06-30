package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class ValidValueDiscoverResponseValidator extends Validator {

	private static final String NOT_REGISTERED = "Not Registered";
	private static final String NOT_READY = "Not Ready";
	private static final String BUSY = "Busy";
	private static final String READY = "Ready";

	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		DiscoverResponse discoverResponse = response.discoverResponse;

		//Check for device status
		if(discoverResponse.deviceStatus != READY || discoverResponse.deviceStatus != BUSY
				|| discoverResponse.deviceStatus != NOT_READY || discoverResponse.deviceStatus != NOT_REGISTERED)
		{
			errors.add("Device info response device status is invalid");
			return errors;
		}
		//Check for device certification
		if(discoverResponse.certification != "L0" || discoverResponse.certification != "L1" || discoverResponse.certification != "L2" )
		{
			errors.add("Device info response certification is invalid");
			return errors;
		}
		//Check for device sub id
		for(Integer subid:discoverResponse.deviceSubId)
		{
			if(subid < 0 || subid >3)
			{
				errors.add("Device info response deviceSubId - "+ subid +" is invalid");
				return errors;
			}
		}

		//TODO Check for digital id
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.

		
		
		//Check for purpose
		if(discoverResponse.purpose != "Auth" || discoverResponse.purpose != "Registration")
		{
			errors.add("Device info response purpose is invalid");
			return errors;
		}


		//TODO check array of spec versions
		//TODO validate errors
		return errors;
	}


}
