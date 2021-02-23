package io.mosip.mds.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureRequest;
import io.mosip.mds.dto.CaptureRequest.CaptureBioRequest;
import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometric;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

@Component
public class ValidValueCaptureResponseValidator extends Validator {

	private final List<String> bioSubTypeFingerList= getBioSubTypeFinger();
	private final List<String> bioSubTypeIrisList = getBioSubTypeIris();

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	private ObjectMapper jsonMapper;

	public ValidValueCaptureResponseValidator() {
		super("ValidValueCaptureResponseValidator", "Valid Value Capture Response Validator");
	}

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws IOException {
		List<Validation> validations = new ArrayList<>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",CommonConstant.DATA);		
		if(Objects.nonNull(response))
		{
			validations.add(validation);
			validation = commonValidator.setFieldExpected("DecodedResponse","Expected whole Capture decoded Jsone Response",CommonConstant.DATA);
			CaptureResponse cr = (CaptureResponse) response.getMdsDecodedResponse();
			if(Objects.nonNull(cr))
			{
				validations.add(validation);
				validation = commonValidator.setFieldExpected("CaptureResponse.biometrics","Expected Array of biometric data",CommonConstant.DATA);
				if(cr.biometrics == null || cr.biometrics.length == 0)
				{
					commonValidator.setFoundMessageStatus(validation,cr.biometrics.toString(),"Capture response does not contain biometrics block",CommonConstant.FAILED);
				}
				validations.add(validation);

				validations=validateBiometricsType(validations, cr.biometrics,response);
				for(CaptureResponse.CaptureBiometric bb:cr.biometrics)
				{
					CaptureBiometricData dataDecoded = bb.dataDecoded;
					if(Objects.nonNull(dataDecoded)) {
						validations=validateActualValueDatadecoded(validations, dataDecoded);
						//TODO check for env
						validation = commonValidator.setFieldExpected("dataDecoded.env","Staging | Developer | Pre-Production | Production",dataDecoded.env);
						if( !dataDecoded.env.equals(CommonConstant.STAGING) && !dataDecoded.env.equals(CommonConstant.DEVELOPER)
								&& !dataDecoded.env.equals(CommonConstant.PRE_PRODUCTION) && !dataDecoded.env.equals(CommonConstant.PRODUCTION))
						{
							commonValidator.setFoundMessageStatus(validation,dataDecoded.env,"Capture response biometrics-dataDecoded env is invalid",CommonConstant.FAILED);
						}
						validations.add(validation);

						//TODO check time stamp for ISO Format date time with timezone
						validation = commonValidator.setFieldExpected("dataDecoded.timestamp","ISO Date formate",dataDecoded.timestamp);
						validations=commonValidator.validateTimeStamp(dataDecoded.timestamp,validations,validation);

						//TODO check for requestedScore
						//TODO check for quality score
					}

				}
			}
			else
			{
				commonValidator.setFoundMessageStatus(validation,"Found Capture Decoded is null","Capture response is empty",CommonConstant.FAILED);
				validations.add(validation);
			}

		}else
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}

		return validations;
	}

	public List<Validation> validateBiometricsType(List<Validation> validations,
			CaptureBiometric[] biometrics, ValidateResponseRequestDto response) throws JsonParseException, JsonMappingException, IOException {
		//check for L1 device only
		int bioIndex = 0;
		CaptureRequest captureRequest = (CaptureRequest) (jsonMapper.readValue(response.getMdsDecodedRequest(), CaptureRequest.class));
		if(response.getDeviceInfo().certification.equals(CommonConstant.L1)) {
			for(CaptureBioRequest bio:captureRequest.bio) {
				validation = commonValidator.setFieldExpected("dataDecoded.bioType",response.getTestManagerDto().getBiometricType(),biometrics[bioIndex].dataDecoded.bioType);				
				if(!biometrics[bioIndex].dataDecoded.bioType.equals(response.getTestManagerDto().getBiometricType()) &&
						!biometrics[bioIndex].dataDecoded.bioType.equals(bio.type)) {
					commonValidator.setFoundMessageStatus(validation,biometrics[bioIndex].dataDecoded.bioType,"invalid biometrics type returned",CommonConstant.FAILED);					
				}
				validations.add(validation);
				if(bio.bioSubType.length == bio.count)
				{			
					for(String subType:bio.bioSubType)
					{
						validation = commonValidator.setFieldExpected("dataDecoded.bioSubType",subType,biometrics[bioIndex].dataDecoded.bioSubType);				
						if(!biometrics[bioIndex].dataDecoded.bioSubType.equals(subType)) {
							commonValidator.setFoundMessageStatus(validation,biometrics[bioIndex].dataDecoded.bioSubType,"invalid biometrics SubType returned",CommonConstant.FAILED);											
						}
						validations.add(validation);
						bioIndex++;
					}
				}else {
					validation = commonValidator.setFieldExpected("dataDecoded data count",bio.count.toString(),String.valueOf(bio.bioSubType.length));				
					commonValidator.setFoundMessageStatus(validation,String.valueOf(bio.bioSubType.length),"invalid biometrics count bio.count v/s expected bio.bioSubType",CommonConstant.FAILED);																
					validations.add(validation);
				}
			}
		}
		return validations;
	}

	private List<Validation> validateActualValueDatadecoded(List<Validation> validations, CaptureBiometricData dataDecoded) {
		// Check for bioType elements
		validation = commonValidator.setFieldExpected("dataDecoded.bioType","Finger | Iris| Face",dataDecoded.bioType);		
		if(!dataDecoded.bioType.equals(CommonConstant.FINGER) && !dataDecoded.bioType.equals(CommonConstant.IRIS) && !dataDecoded.bioType.equals(CommonConstant.FACE))
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.bioType,"Capture response biometrics-dataDecoded bioType is invalid",CommonConstant.FAILED);
			validations.add(validation);

		}else {
			validations.add(validation);

			//Check for bioSubType
			validations = validateBioSubType(validations, dataDecoded);

		}

		//TODO Check for digitalId dataDecoded.digitalId
		validations=validateDigitalId(validations,dataDecoded);

		//Check for purpose elements
		validation = commonValidator.setFieldExpected("dataDecoded.purpose"," Auth or Registration",dataDecoded.purpose);
		if(!dataDecoded.purpose.equals(CommonConstant.AUTH) && !dataDecoded.purpose.equals(CommonConstant.REGISTRATION) )
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.purpose,"Capture response biometrics-dataDecoded purpose is invalid",CommonConstant.FAILED);
		}
		validations.add(validation);

		//TODO Check for digitalId dataDecoded.digitalId

		return validations;
	}

	private List<Validation> validateBioSubType(List<Validation> validations, CaptureBiometricData dataDecoded) {
		switch(dataDecoded.bioType) {
		// Check for bioSubType of Finger elements
		case CommonConstant.FINGER:
			validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","For Finger: [Left IndexFinger, Left MiddleFinger, "
					+ "Left RingFinger, Left LittleFinger, Left Thumb, Right IndexFinger,"
					+ " Right MiddleFinger, Right RingFinger, Right LittleFinger, Right Thumb, UNKNOWN] ",dataDecoded.bioSubType);		
			if(!bioSubTypeFingerList.contains(dataDecoded.bioSubType))
			{
				commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Capture response bioSubType is invalid for Finger",CommonConstant.FAILED);
			}
			validations.add(validation);
			break;

		case CommonConstant.IRIS:
			// Check for bioSubType of Iris elements
			validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","[Left, Right, UNKNOWN]",dataDecoded.bioSubType);
			if(!bioSubTypeIrisList.contains(dataDecoded.bioSubType))
			{
				commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Capture response bioSubType is invalid for Iris",CommonConstant.FAILED);
			}
			validations.add(validation);
			break;
		case CommonConstant.FACE:	
			// Check for bioSubType of Face elements
			validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","No bioSubType",dataDecoded.bioSubType);
			if(!(dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty()))
			{
				commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Capture response bioSubType for Face should be empty",CommonConstant.FAILED);
			}
			validations.add(validation);
		}
		return validations;
	}

	private List<Validation> validateDigitalId(List<Validation> validations,CaptureBiometricData dataDecoded) {
		validations = commonValidator.validateDecodedSignedDigitalID(dataDecoded.digitalId,validations);
		return validations;
	}

	public List<String> getBioSubTypeIris() {
		List<String> bioSubTypeIrisList = new ArrayList<String>();
		bioSubTypeIrisList.add(CommonConstant.LEFT);
		bioSubTypeIrisList.add(CommonConstant.RIGHT);
		bioSubTypeIrisList.add( CommonConstant.UNKNOWN);
		return bioSubTypeIrisList;
	}
	public List<String> getBioSubTypeFinger() {
		List<String> bioSubTypeFingerList=new ArrayList<String>();
		bioSubTypeFingerList.add(CommonConstant.LEFT_INDEX_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_MIDDLE_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_RING_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_LITTLE_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_THUMB);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_INDEX_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_MIDDLE_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_RING_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_LITTLE_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_THUMB);
		bioSubTypeFingerList.add(CommonConstant.UNKNOWN);

		return bioSubTypeFingerList;
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
