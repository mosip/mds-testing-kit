package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.util.SecurityUtil;

public class CommonValidator {

	private static final String FACE = "Face";
	private static final String IRIS = "Iris";
	private static final String FINGER = "Finger";
	private static final String ISO_FORMATE = "^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:Z|[+-][01]\\d:[0-5]\\d)$";

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public List<String> validateSignedDigitalID(String digitalId) {
		List<String> errors= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		if(parts.length != 3) {
			errors.add("Digital Id is not signed,Missing header|payload|signature");				
			return errors; 
		}
		try {
			DigitalId decodedDigitalId=(DigitalId) (mapper.readValue(SecurityUtil.getPayload(digitalId),
					DigitalId.class));
			errors=mandatoryParamDigitalIdPayload(decodedDigitalId,errors);
			errors=validValueDigitalIdPayload(decodedDigitalId,errors);

			return errors;
		} 
		catch(Exception dex)
		{
			errors.add("Error interpreting digital id: " + dex.getMessage());		
		}

		return errors;

	}

	public List<String> validateUnSignedDigitalID(String digitalId) {
		List<String> errors= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		if(parts.length != 1) {
			errors.add("Invalid Unsigned Digital Id");				
			return errors; 
		}

		try {
			DigitalId decodedDigitalId=(DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(digitalId),
					DigitalId.class));
			errors=mandatoryParamDigitalIdPayload(decodedDigitalId,errors);
			errors=validValueDigitalIdPayload(decodedDigitalId,errors);

			return errors;
		} 
		catch(Exception dex)
		{
			errors.add("Error interpreting digital id: " + dex.getMessage());		
		}

		return errors;

	}

	//	public List<String> validateDigitalId(String digitalId) {
	//
	//		List<String> errors = new ArrayList<>();
	//		try {
	//			DigitalId decodedDigitalId=(DigitalId) (mapper.readValue(SecurityUtil.getPayload(digitalId),
	//					DigitalId.class));
	//			errors=mandatoryParamDigitalIdPayload(decodedDigitalId,errors);
	//			errors=validValueDigitalIdPayload(decodedDigitalId,errors);
	//
	//			return errors;
	//		} 
	//		catch(Exception dex)
	//		{
	//			errors.add("Error interpreting digital id: " + dex.getMessage());		
	//		}
	//
	//		return errors;
	//	}

	private List<String> mandatoryParamDigitalIdPayload(DigitalId decodedDigitalIdPayload, List<String> errors) {

		//Check for Date Time
		if(decodedDigitalIdPayload.dateTime == null)
		{
			errors.add("Response DigitalId does not contain date and Time");
			return errors;
		}

		//Check for deviceProvider
		if(decodedDigitalIdPayload.deviceProvider == null || decodedDigitalIdPayload.deviceProvider.isEmpty())
		{
			errors.add("Response DigitalId does not contain deviceProvider");
			return errors;
		}

		//Check for deviceProviderId
		if(decodedDigitalIdPayload.deviceProviderId == null || decodedDigitalIdPayload.deviceProviderId.isEmpty())
		{
			errors.add("Response DigitalId does not contain deviceProviderId");
			return errors;
		}

		//Check for type element
		if(decodedDigitalIdPayload.type == null || decodedDigitalIdPayload.type.isEmpty())
		{
			errors.add("Response DigitalId does not contain type block");
			return errors;
		}

		//Check for deviceSubType
		if(decodedDigitalIdPayload.deviceSubType == null || decodedDigitalIdPayload.deviceSubType.isEmpty())
		{
			errors.add("Response DigitalId does not contain deviceSubType");
			return errors;
		}

		//Check for make element
		if(decodedDigitalIdPayload.make == null || decodedDigitalIdPayload.make.isEmpty())
		{
			errors.add("Response DigitalId does not contain make block");
			return errors;
		}

		//Check for model element
		if(decodedDigitalIdPayload.model == null || decodedDigitalIdPayload.model.isEmpty())
		{
			errors.add("Response DigitalId does not contain model block");
			return errors;
		}

		//Check for serialNo element
		if(decodedDigitalIdPayload.serialNo == null || decodedDigitalIdPayload.serialNo.isEmpty())
		{
			errors.add("Response DigitalId does not contain serialNo block");
			return errors;
		}
		return errors;
	}

	private List<String> validValueDigitalIdPayload(DigitalId decodedDigitalIdPayload, List<String> errors) {


		if(decodedDigitalIdPayload.type == FINGER || decodedDigitalIdPayload.type == IRIS 
				|| decodedDigitalIdPayload.type == FACE)
		{
			errors.add("Response DigitalId type is invalid");
			return errors;
		}else {

			//Check for bioSubType
			errors = validateDeviceSubType(errors, decodedDigitalIdPayload);
			if(!ObjectUtils.isEmpty(errors))
				return errors;
		}

		return errors;
	}

	private List<String> validateDeviceSubType(List<String> errors, DigitalId decodedDigitalIdPayload) {
		if(decodedDigitalIdPayload.deviceSubType == FINGER && decodedDigitalIdPayload.deviceSubType != "Slap" 
				&& decodedDigitalIdPayload.deviceSubType != "Single" && decodedDigitalIdPayload.deviceSubType != "Touchless")
		{
			errors.add("Response DigitalId DeviceSubType is invalid for Finger");
		}

		if(decodedDigitalIdPayload.deviceSubType == FACE && decodedDigitalIdPayload.deviceSubType != "Full face")
		{
			errors.add("Response DigitalId DeviceSubType is invalid for Face");
		}

		if(decodedDigitalIdPayload.deviceSubType == IRIS && decodedDigitalIdPayload.deviceSubType != "Double" 
				&& decodedDigitalIdPayload.deviceSubType != "Single" )
		{
			errors.add("Response DigitalId DeviceSubType is invalid for Iris");
		}
		return errors;
	}

	// "2025-01-01T00:00:00+05:30"
	public List<String> validateTimeStamp(String timeStamp,List<String> errors) {
		if(!(timeStamp.matches(ISO_FORMATE)))
		{
			errors.add("Response timeStamp is not in -ISO Format date time with timezone");
		}
		return errors;
	}

	//TODO check header validation as per spec
	private List<String> mandatoryParamDigitalIdHeader(List<String> errors){

		return errors;
	}

}
