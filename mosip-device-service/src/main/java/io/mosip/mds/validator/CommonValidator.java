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
import io.mosip.mds.dto.Validation;
import io.mosip.mds.util.SecurityUtil;

public class CommonValidator{
	//2020-07-07T01:18:58.804+05:30
	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	Validation validation = new Validation();

	public List<Validation> validateDecodedSignedDigitalID(String digitalId, List<Validation> validations) {
		String [] parts = digitalId.split("\\.");
		validation = setFieldExpected("digitalId","Signed DigitalId",digitalId);	
		if(parts.length == 3) {
			validations.add(validation);
			
			try {
				DigitalId decodedDigitalId=(DigitalId) (mapper.readValue(SecurityUtil.getPayload(digitalId),
						DigitalId.class));
				validations=mandatoryParamDigitalIdPayload(decodedDigitalId,validations);
				validations=validValueDigitalIdPayload(decodedDigitalId,validations);
			} 
			catch(Exception dex)
			{
				setFoundMessageStatus(validation,digitalId,"(Invalid Digital Id) Error interpreting digital id: " + dex.getMessage(),CommonConstant.FAILED);						
				validations.add(validation);
				return validations;
			}
		}else {
			setFoundMessageStatus(validation,digitalId,"Invalid signed base64urlEncoded digitalId" ,CommonConstant.FAILED);						
			validations.add(validation);
		}
		return validations;
	}

	public List<Validation> validateDecodedUnSignedDigitalID(String digitalId) {
		List<Validation> validations= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		validation = setFieldExpected("digitalId","UnSigned DigitalId",digitalId);	
		if(parts.length == 1) {
			try {
				DigitalId decodedDigitalId=(DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(digitalId),
						DigitalId.class));
				validations=mandatoryParamDigitalIdPayload(decodedDigitalId,validations);
				validations=validValueDigitalIdPayload(decodedDigitalId,validations);
				return validations;
			} 
			catch(Exception dex)
			{
				setFoundMessageStatus(validation,digitalId,"(Invalid Digital Id) Error interpreting digital id: " + dex.getMessage(),CommonConstant.FAILED);					
				validations.add(validation);
				return validations;
			}
		}else {
			setFoundMessageStatus(validation,digitalId,"Invalid Unsigned Digital Id",CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	private List<Validation> mandatoryParamDigitalIdPayload(DigitalId decodedDigitalIdPayload, List<Validation> validations) {

		//Check for Date Time
		validation = setFieldExpected("decodedDigitalIdPayload.dateTime","ISO format with timezone",decodedDigitalIdPayload.dateTime.toString());	
		if(decodedDigitalIdPayload.dateTime == null)
		{	
			setFoundMessageStatus(validation,decodedDigitalIdPayload.dateTime.toString(),"Response DigitalId does not contain date and Time",CommonConstant.FAILED);
			validations.add(validation);
		}

		//Check for deviceProvider
		validation = setFieldExpected("decodedDigitalIdPayload.deviceProvider","Device provider name",decodedDigitalIdPayload.deviceProvider);	
		if(decodedDigitalIdPayload.deviceProvider == null || decodedDigitalIdPayload.deviceProvider.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.deviceProvider,"Response DigitalId does not contain deviceProvider",CommonConstant.FAILED);
		}
		validations.add(validation);
		//Check for deviceProviderId
		validation = setFieldExpected("decodedDigitalIdPayload.deviceProviderId","Device provider Id issued by MOSIP adopters",decodedDigitalIdPayload.deviceProviderId);	
		if(decodedDigitalIdPayload.deviceProviderId == null || decodedDigitalIdPayload.deviceProviderId.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.deviceProviderId,"Response DigitalId does not contain deviceProviderId",CommonConstant.FAILED);
		}
		validations.add(validation);
		//Check for type element
		validation = setFieldExpected("decodedDigitalIdPayload.type","[Finger, Iris, Face]",decodedDigitalIdPayload.type);	
		if(decodedDigitalIdPayload.type == null || decodedDigitalIdPayload.type.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.type,"Response DigitalId does not contain type block",CommonConstant.FAILED);
		}
		validations.add(validation);
		//Check for deviceSubType
		validation = setFieldExpected("decodedDigitalIdPayload.deviceSubType","\r\n For Finger - Slap, Single, Touchless \r\n" + 
				"For Iris - Single, Double,\r\n" + 
				"For Face - Full face",decodedDigitalIdPayload.deviceSubType);	
		if(decodedDigitalIdPayload.deviceSubType == null || decodedDigitalIdPayload.deviceSubType.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.deviceSubType,"Response DigitalId does not contain deviceSubType",CommonConstant.FAILED);
		}
		validations.add(validation);
		//Check for make element
		validation = setFieldExpected("decodedDigitalIdPayload.make","Brand name",decodedDigitalIdPayload.make);	
		if(decodedDigitalIdPayload.make == null || decodedDigitalIdPayload.make.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.make,"Response DigitalId does not contain make block",CommonConstant.FAILED);
		}
		validations.add(validation);
		//Check for model element
		validation = setFieldExpected("decodedDigitalIdPayload.model","Model of the device",decodedDigitalIdPayload.model);	
		if(decodedDigitalIdPayload.model == null || decodedDigitalIdPayload.model.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.model,"Response DigitalId does not contain model block",CommonConstant.FAILED);
		}
		validations.add(validation);
		//Check for serialNo element
		validation = setFieldExpected("decodedDigitalIdPayload.serialNo","Serial number of the device",decodedDigitalIdPayload.serialNo);	
		if(decodedDigitalIdPayload.serialNo == null || decodedDigitalIdPayload.serialNo.isEmpty())
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.serialNo,"Response DigitalId does not contain serialNo block",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	private List<Validation> validValueDigitalIdPayload(DigitalId decodedDigitalIdPayload, List<Validation> validations) {
		validation = setFieldExpected("decodedDigitalIdPayload.type","[Finger | Iris | Face]",decodedDigitalIdPayload.type);	
		if(!decodedDigitalIdPayload.type.equals(CommonConstant.FINGER) && !decodedDigitalIdPayload.type.equals(CommonConstant.IRIS) 
				&& !decodedDigitalIdPayload.type.equals(CommonConstant.FACE))
		{
			setFoundMessageStatus(validation,decodedDigitalIdPayload.type,"Response DigitalId type is invalid",CommonConstant.FAILED);
		}else {
			//Check for bioSubType
			validations = validateDeviceSubType(validations, decodedDigitalIdPayload);
			if(!ObjectUtils.isEmpty(validations))
				return validations;
		}
		validations.add(validation);
		//		errors=validateTimeStamp(decodedDigitalIdPayload.dateTime.toString(),errors);
		return validations;
	}

	private List<Validation> validateDeviceSubType(List<Validation> validations, DigitalId decodedDigitalIdPayload) {

		switch(decodedDigitalIdPayload.deviceSubType) {
		case CommonConstant.FACE:
			validation = setFieldExpected("decodedDigitalIdPayload.deviceSubType",
					"For Face - Full face",decodedDigitalIdPayload.deviceSubType);
			if(decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.FACE) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.FULL_FACE))
			{
				setFoundMessageStatus(validation,decodedDigitalIdPayload.deviceSubType,"Response DigitalId DeviceSubType is invalid for Face",CommonConstant.FAILED);
			}
			validations.add(validation);
			break;

		case CommonConstant.FINGER:
			validation = setFieldExpected("decodedDigitalIdPayload.deviceSubType","For Finger - Slap, Single, Touchless",decodedDigitalIdPayload.deviceSubType);	
			if(decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.FINGER) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.SLAP) 
					&& !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.SINGLE) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.TOUCHLESS))
			{
				setFoundMessageStatus(validation,decodedDigitalIdPayload.deviceSubType,"Response DigitalId DeviceSubType is invalid for Finger",CommonConstant.FAILED);
			}
			validations.add(validation);
			break;
		case CommonConstant.IRIS:
			validation = setFieldExpected("decodedDigitalIdPayload.deviceSubType","For Iris - Single, Double",decodedDigitalIdPayload.deviceSubType);
			if(decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.IRIS) && !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.DOUBLE) 
					&& !decodedDigitalIdPayload.deviceSubType.equals(CommonConstant.SINGLE))
			{
				setFoundMessageStatus(validation,decodedDigitalIdPayload.deviceSubType,"Response DigitalId DeviceSubType is invalid for Iris",CommonConstant.FAILED);
			}
			validations.add(validation);
			break;

		}

		return validations;
	}

	//Date and Time Validation
	public List<Validation> validateTimeStamp(String dateString,List<Validation> validations) {
		validation = setFieldExpected("date","ISO Date formate",dateString);
		if (Objects.isNull(dateString)) {
			setFoundMessageStatus(validation,"timeStamp in null","TimeStamp is empty",CommonConstant.FAILED);
		}
		validations.add(validation);

		try {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN);
			System.out.println(simpleDateFormat.parse(dateString));

		} catch (Exception e) {
			setFoundMessageStatus(validation,"TimeStamp formatte is invalid as per ISO Date formate",e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	public void setFoundMessageStatus(Validation validation,String found,String message,String status) {
		validation.setFound(found);
		validation.setMessage(message);
		validation.setStatus(status);
	}
	public Validation setFieldExpected(String field,String expected, String found){
		Validation validation=new Validation();
		validation.setField(field);
		validation.setExpected(expected);
		if(found != null) {
			validation.setFound(found);
		}
		validation.setMessage(CommonConstant.MATCHED);
		validation.setStatus(CommonConstant.SUCCESS);
		return validation;
	}

}

