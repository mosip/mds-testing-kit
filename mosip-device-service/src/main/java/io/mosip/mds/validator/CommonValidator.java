package io.mosip.mds.validator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.util.SecurityUtil;

public class CommonValidator extends Validator {
	//2020-07-07T01:18:58.804+05:30
	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
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
			errors.add("(Invalid Digital Id) Error interpreting digital id: " + dex.getMessage());		
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
			errors.add("(Invalid Digital Id) Error interpreting digital id: " + dex.getMessage());		
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


		if(!decodedDigitalIdPayload.type.equals(CommonConstant.FINGER) && !decodedDigitalIdPayload.type.equals(CommonConstant.IRIS) 
				&& !decodedDigitalIdPayload.type.equals(CommonConstant.FACE))
		{
			errors.add("Response DigitalId type is invalid");
			return errors;
		}else {

			//Check for bioSubType
			errors = validateDeviceSubType(errors, decodedDigitalIdPayload);
			if(!ObjectUtils.isEmpty(errors))
				return errors;
		}

		//		errors=validateTimeStamp(decodedDigitalIdPayload.dateTime.toString(),errors);

		return errors;
	}

	private List<String> validateDeviceSubType(List<String> errors, DigitalId decodedDigitalIdPayload) {
		if(decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.FINGER) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.SLAP) 
				&& !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.SINGLE) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.TOUCHLESS))
		{
			errors.add("Response DigitalId DeviceSubType is invalid for Finger");
		}

		if(decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.FACE) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.FULL_FACE))
		{
			errors.add("Response DigitalId DeviceSubType is invalid for Face");
		}

		if(decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.IRIS) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.DOUBLE) 
				&& !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.SINGLE))
		{
			errors.add("Response DigitalId DeviceSubType is invalid for Iris");
		}
		return errors;
	}

	//TODO check header validation as per spec
	private List<String> mandatoryParamDigitalIdHeader(List<String> errors){

		return errors;
	}
	//Date and Time Validation
	public static List<String> validateTimeStamp(String dateString,List<String> errors) {
		if (Objects.isNull(dateString)) {
			errors.add("TimeStamp is empty");

		}
		try {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN);
			//simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
			System.out.println(simpleDateFormat.parse(dateString));

		} catch (Exception e) {
			errors.add("TimeStamp formatte is invalid as per ISO Date formate");
		}
		return errors;
	}
}

