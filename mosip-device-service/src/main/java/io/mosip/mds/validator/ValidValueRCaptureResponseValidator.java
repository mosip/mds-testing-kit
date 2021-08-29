package io.mosip.mds.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometric;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.RegistrationCaptureRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest.RegistrationCaptureBioRequest;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;
@Component
public class ValidValueRCaptureResponseValidator extends Validator{

	private List<String> bioSubTypeFingerList=new ArrayList<String>();
	private List<String> bioSubTypeIrisList=new ArrayList<String>();

	@Autowired
	private ObjectMapper jsonMapper;

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	public ValidValueRCaptureResponseValidator() {
		super("ValidValueRCaptureResponseValidator", "Valid Value Registration Capture Response Validator");
	}

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws IOException {
		List<Validation> validations = new ArrayList<>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",CommonConstant.DATA);		
		if(Objects.nonNull(response))
		{
			validations.add(validation);

			validation = commonValidator.setFieldExpected("DecodedResponse","Expected whole RCapture decoded Jsone Response",CommonConstant.DATA);
			// Check for Biometrics block
			validations.add(validation);

			CaptureResponse registrationCaptureResponse = (CaptureResponse) response.getMdsDecodedResponse();
			if(Objects.nonNull(registrationCaptureResponse))
			{
				validation = commonValidator.setFieldExpected("registrationCaptureResponse.biometrics","Expected Array of biometric data",CommonConstant.DATA);
				if(registrationCaptureResponse.biometrics == null || registrationCaptureResponse.biometrics.length == 0)
				{
					commonValidator.setFoundMessageStatus(validation,CommonConstant.DATA,"RCapture response does not contain biometrics block",CommonConstant.FAILED);
				}
				validations.add(validation);

				validations=checkdeviceError(validations,response);

				validations=validateBiometricsType(validations,response);

				for(CaptureBiometric bb:registrationCaptureResponse.biometrics)
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
				commonValidator.setFoundMessageStatus(validation,"Found RegistrationCapture Decoded is null","RegistrationCapture response is empty",CommonConstant.FAILED);
				validations.add(validation);
			}

		}
		else
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}
	private List<Validation> checkdeviceError(List<Validation> validations, ValidateResponseRequestDto response) throws JsonParseException, JsonMappingException, IOException {
		RegistrationCaptureRequest rcaptureRequest = (RegistrationCaptureRequest) (jsonMapper.readValue(response.getMdsDecodedRequest(), RegistrationCaptureRequest.class));
		CaptureResponse registrationCaptureResponse = (CaptureResponse) response.getMdsDecodedResponse();

		int index=-1;
		for(RegistrationCaptureBioRequest bio:rcaptureRequest.bio) {
			if(bio.type.equals(CommonConstant.FINGER)) {
				if(bio.deviceSubId.equals("1") || bio.deviceSubId.equals("2")) {
					if(bio.exception.length == 4) {
						if(registrationCaptureResponse.biometrics[index] != null){
							validation = commonValidator.setFieldExpected("correct error code for invalid finger slap count","correcr error code 109 should be returned",registrationCaptureResponse.biometrics[index].error.errorCode);											
							if(!registrationCaptureResponse.biometrics[index].error.errorCode.equals(109)) {
								commonValidator.setFoundMessageStatus(validation,registrationCaptureResponse.biometrics[index].error.errorCode,"invalid error code returned",CommonConstant.FAILED);																								
							}
							validations.add(validation);
						}
					}
				}else if(bio.deviceSubId.equals("3")) {
					if(bio.exception.length == 2) {
						if(registrationCaptureResponse.biometrics[index] != null){
							validation = commonValidator.setFieldExpected("correct error code for invalid thumb count","correcr error code 109 should be returned",registrationCaptureResponse.biometrics[index].error.errorCode);								
							if(!registrationCaptureResponse.biometrics[index].error.errorCode.equals(109)) {
								commonValidator.setFoundMessageStatus(validation,registrationCaptureResponse.biometrics[index].error.errorCode,"invalid error code returned",CommonConstant.FAILED);																								
							}
							validations.add(validation);
						}
					}
				}
			}
			if(bio.type.equals(CommonConstant.IRIS)) {

				if(bio.deviceSubId.equals("1") || bio.deviceSubId.equals("2")) {
					if(bio.exception.length == 1) {
						if(registrationCaptureResponse.biometrics[index] != null){
							if(!registrationCaptureResponse.biometrics[index].error.errorCode.equals(109)) {
								if(registrationCaptureResponse.biometrics[index] != null){
									validation = commonValidator.setFieldExpected("correct error code for invalid iris count","correcr error code 109 should be returned",registrationCaptureResponse.biometrics[index].error.errorCode);								
									if(!registrationCaptureResponse.biometrics[index].error.errorCode.equals(109)) {
										commonValidator.setFoundMessageStatus(validation,registrationCaptureResponse.biometrics[index].error.errorCode,"invalid error code returned",CommonConstant.FAILED);																								
									}
									validations.add(validation);
								}													
							}
						}
					}
				}else if(bio.deviceSubId.equals("3")) {
					if(bio.exception.length == 2) {
						if(registrationCaptureResponse.biometrics[index] != null){
							if(!registrationCaptureResponse.biometrics[index].error.errorCode.equals(109)) {
								if(registrationCaptureResponse.biometrics[index] != null){
									validation = commonValidator.setFieldExpected("correct error code for invalid iris count","correcr error code 109 should be returned",registrationCaptureResponse.biometrics[index].error.errorCode);								
									if(!registrationCaptureResponse.biometrics[index].error.errorCode.equals(109)) {
										commonValidator.setFoundMessageStatus(validation,registrationCaptureResponse.biometrics[index].error.errorCode,"invalid error code returned",CommonConstant.FAILED);																								
									}
									validations.add(validation);
								}						
							}
						}
					}
				}
			}
		}
		return validations;
	}

	private List<Validation> validateActualValueDatadecoded(List<Validation> validations, CaptureBiometricData dataDecoded) {
		// Check for bioType elements
		//		validation = commonValidator.setFieldExpected("dataDecoded.bioType","Finger | Iris| Face",dataDecoded.bioType);		
		//		if(!dataDecoded.bioType.equals(CommonConstant.FINGER) && !dataDecoded.bioType.equals(CommonConstant.IRIS) && !dataDecoded.bioType.equals(CommonConstant.FACE))
		//		{
		//			commonValidator.setFoundMessageStatus(validation,dataDecoded.bioType,"Registration Capture response biometrics-dataDecoded bioType is invalid",CommonConstant.FAILED);
		//			validations.add(validation);
		//
		//		}
		//		else
		//		{
		//			validations.add(validation);
		//			//Check for bioSubType
		//			validations = validateBioSubType(validations, dataDecoded);
		//		}


		//TODO Check for digitalId dataDecoded.digitalId
		validations=validateDigitalId(dataDecoded, validations);

		//Check for purpose elements
		validation = commonValidator.setFieldExpected("dataDecoded.purpose"," Auth or Registration",dataDecoded.purpose);
		if(!dataDecoded.purpose.equals(CommonConstant.AUTH) && !dataDecoded.purpose.equals(CommonConstant.REGISTRATION) )
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.purpose,"Registration Capture response biometrics-dataDecoded purpose is invalid",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	private List<Validation> validateBioSubType(List<Validation> validations, CaptureBiometricData dataDecoded) {
		if(bioSubTypeFingerList.size() != 0)		
			switch(dataDecoded.bioType) {
			// Check for bioSubType of Finger elements
			case CommonConstant.FINGER:
				validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","For Finger: [Left IndexFinger, Left MiddleFinger, "
						+ "Left RingFinger, Left LittleFinger, Left Thumb, Right IndexFinger,"
						+ " Right MiddleFinger, Right RingFinger, Right LittleFinger, Right Thumb, UNKNOWN] ",dataDecoded.bioSubType);		
				if(!bioSubTypeFingerList.contains(dataDecoded.bioSubType))
				{
					commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Registration Capture response bioSubType is invalid for Finger",CommonConstant.FAILED);
				}
				validations.add(validation);
				break;

			case CommonConstant.IRIS:
				// Check for bioSubType of Iris elements
				validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","[Left, Right, UNKNOWN]",dataDecoded.bioSubType);
				if(!bioSubTypeIrisList.contains(dataDecoded.bioSubType))
				{
					commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Registration Capture response bioSubType is invalid for Iris",CommonConstant.FAILED);
				}
				validations.add(validation);
				break;
			case CommonConstant.FACE:	
				// Check for bioSubType of Face elements
				validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","No bioSubType",dataDecoded.bioSubType);
				if(!(dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty()))
				{
					commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Registration Capture response bioSubType for Face should be empty",CommonConstant.FAILED);
				}
				validations.add(validation);
			}
		return validations;
	}

	private List<Validation> validateDigitalId(CaptureBiometricData dataDecoded,List<Validation> validations) {
		validations = commonValidator.validateDecodedSignedDigitalID(dataDecoded.digitalId,validations);
		return validations;
	}
	public List<Validation> validateBiometricsType(List<Validation> validations,
			ValidateResponseRequestDto response) throws JsonParseException, JsonMappingException, IOException {
		//check for L0 device only
		int bioIndex = 0;
		RegistrationCaptureRequest rcaptureRequest = (RegistrationCaptureRequest) (jsonMapper.readValue(response.getMdsDecodedRequest(), RegistrationCaptureRequest.class));
		CaptureResponse registrationCaptureResponse = (CaptureResponse) response.getMdsDecodedResponse();
		CaptureBiometric[] biometrics = registrationCaptureResponse.getBiometrics();
		if(response.getDeviceInfo().certification.equals(CommonConstant.L0)) {
			for(RegistrationCaptureBioRequest request:rcaptureRequest.bio) {
				validation = commonValidator.setFieldExpected("dataDecoded.bioType",response.getTestManagerDto().getBiometricType(),biometrics[bioIndex].dataDecoded.bioType);				
				if(!biometrics[bioIndex].dataDecoded.bioType.equals(response.getTestManagerDto().getBiometricType()) &&
						!biometrics[bioIndex].dataDecoded.bioType.equals(request.type)) {
					commonValidator.setFoundMessageStatus(validation,biometrics[bioIndex].dataDecoded.bioType,"invalid biometrics type returned",CommonConstant.FAILED);					
				}
				validations.add(validation);

				if(biometrics[bioIndex].dataDecoded.bioType.equals(CommonConstant.FINGER)) {
					if(request.deviceSubId.equals("0") && response.getTestManagerDto().deviceSubType.equals(CommonConstant.SINGLE)) {
						bioSubTypeFingerList=getBioSubTypeFinger();
					}else if(request.deviceSubId.equals("1")) {
						bioSubTypeFingerList=getBioSubTypeFingerLeftSlap();
					}else if(request.deviceSubId.equals("2")) {
						bioSubTypeFingerList=getBioSubTypeFingerRightSlap();						
					}else if(request.deviceSubId.equals("3")) {
						bioSubTypeFingerList=getBioSubTypeFingerThumb();						
					}else {
						validation = commonValidator.setFieldExpected("module in the scanner","deviceInfo.deviceSubId should be present","invalid");				
						commonValidator.setFoundMessageStatus(validation,"invalid","Right/Left/Thumb for finger should be mentioned through deviceSubId",CommonConstant.FAILED);																	
						validations.add(validation);
					}
					validation = commonValidator.setFieldExpected("captured biometrics count","correct requested finger",String.valueOf(4-request.exception.length));				
					if(!(Integer.parseInt(request.count) <=4- request.exception.length && Integer.parseInt(request.count)>0)) {
						commonValidator.setFoundMessageStatus(validation,String.valueOf(request.count),"biometrics count is invalid for requested finger",CommonConstant.FAILED);											
					}
					validations.add(validation);
				}
				else if(biometrics[bioIndex].dataDecoded.bioType.equals(CommonConstant.IRIS)) {
					if(request.deviceSubId.equals("0") && response.getTestManagerDto().deviceSubType.equals(CommonConstant.SINGLE)) {
						bioSubTypeIrisList.add(CommonConstant.LEFT);
						bioSubTypeIrisList.add(CommonConstant.RIGHT);						
					}else if(request.deviceSubId.equals("1")) {
						bioSubTypeIrisList.add(CommonConstant.LEFT);
					}else if(request.deviceSubId.equals("2")) {
						bioSubTypeIrisList.add(CommonConstant.RIGHT);						
					}else if(request.deviceSubId.equals("3")) {
						bioSubTypeIrisList.add(CommonConstant.LEFT);
						bioSubTypeIrisList.add(CommonConstant.RIGHT);						
					}else {
						validation = commonValidator.setFieldExpected("module in the scanner","deviceInfo.deviceSubId should be present","invalid");				
						commonValidator.setFoundMessageStatus(validation,"invalid","Right/Left for Iris should be mentioned through deviceSubId",CommonConstant.FAILED);																	
						validations.add(validation);
					}
					validation = commonValidator.setFieldExpected("captured biometrics count","for Iris count between 0-2",String.valueOf(request.count));				
					if(!(Integer.parseInt(request.count) <=2 && Integer.parseInt(request.count) >0)) {
						commonValidator.setFoundMessageStatus(validation,String.valueOf(request.count),"biometrics count is invalid for iris",CommonConstant.FAILED);											
					}
					validations.add(validation);
				}
				else if(biometrics[bioIndex].dataDecoded.bioType.equals(CommonConstant.FACE)) {
					validation = commonValidator.setFieldExpected("captured biometrics count","for face count between 0-1",String.valueOf(request.count));				
					if(!(Integer.parseInt(request.count)  <=1 && Integer.parseInt(request.count) >=0)) {
						commonValidator.setFoundMessageStatus(validation,String.valueOf(request.count),"biometrics count is invalid for face",CommonConstant.FAILED);											
					}
					validations.add(validation);

					validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","No bioSubType",biometrics[bioIndex].dataDecoded.bioSubType);
					if(!(biometrics[bioIndex].dataDecoded.bioSubType == null || biometrics[bioIndex].dataDecoded.bioSubType.isEmpty()))
					{
						commonValidator.setFoundMessageStatus(validation,biometrics[bioIndex].dataDecoded.bioSubType,"Registration Capture response bioSubType for Face should be empty",CommonConstant.FAILED);
					}
					validations.add(validation);
				}			

				if((request.bioSubType.length == Integer.parseInt(request.count) ) && (Integer.parseInt(request.count)  == biometrics.length) )
				{
					if(!request.type.equals(CommonConstant.FACE)) {
					//validations = validateBioSubType(validations, biometrics[bioIndex].dataDecoded);
					for(String subType:request.bioSubType) {
						 if(biometrics[bioIndex].dataDecoded.bioType.equals(CommonConstant.FINGER)) {
							
						validation = commonValidator.setFieldExpected("dataDecoded.bioSubType",subType,biometrics[bioIndex].dataDecoded.bioSubType);				
						if(!biometrics[bioIndex].dataDecoded.bioSubType.equals(subType)
								&& !bioSubTypeFingerList.contains(biometrics[bioIndex].dataDecoded.bioSubType)) {
							commonValidator.setFoundMessageStatus(validation,biometrics[bioIndex].dataDecoded.bioSubType,"invalid biometrics SubType returned",CommonConstant.FAILED);											
						}
						validations.add(validation);
						bioIndex++;
						}
						 if(biometrics[bioIndex].dataDecoded.bioType.equals(CommonConstant.IRIS)) {
								
								validation = commonValidator.setFieldExpected("dataDecoded.bioSubType",subType,biometrics[bioIndex].dataDecoded.bioSubType);				
								if(!biometrics[bioIndex].dataDecoded.bioSubType.equals(subType)
										&& !bioSubTypeIrisList.contains(biometrics[bioIndex].dataDecoded.bioSubType)) {
									commonValidator.setFoundMessageStatus(validation,biometrics[bioIndex].dataDecoded.bioSubType,"invalid biometrics SubType returned",CommonConstant.FAILED);											
								}
								validations.add(validation);
								bioIndex++;
								}
					}
					}

					for(int i=0;i<Integer.parseInt(request.count) ;i++) {
						validation = commonValidator.setFieldExpected("exception biometrics",Arrays.toString(request.bioSubType),request.bioSubType[i]);					
						if(Arrays.asList(request.exception).contains(request.bioSubType[i])) {
							commonValidator.setFoundMessageStatus(validation,request.bioSubType[i],"Exception biometrics should not be requested",CommonConstant.FAILED);											
							validations.add(validation);
						}
						validations.add(validation);
					}

				}else if(((request.bioSubType.length == 0) && (Integer.parseInt(request.count)  == biometrics.length)) && request.type.equals(CommonConstant.FACE) ){
					validation = commonValidator.setFieldExpected("dataDecoded data count",String.valueOf(request.count),String.valueOf(biometrics.length));				
					validations.add(validation);
				}else {
					validation = commonValidator.setFieldExpected("dataDecoded data count",String.valueOf(request.count),String.valueOf(request.bioSubType.length));				
					commonValidator.setFoundMessageStatus(validation,String.valueOf(request.bioSubType.length),"invalid biometrics count 'bio.count' v/s expected 'bio.bioSubType' v/s 'biometrics captured'",CommonConstant.FAILED);																
					validations.add(validation);
				}
			}
		}
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
	public List<String> getBioSubTypeFingerThumb() {
		List<String> bioSubTypeFingerList=new ArrayList<String>();
		bioSubTypeFingerList.add(CommonConstant.LEFT_THUMB);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_THUMB);
		return bioSubTypeFingerList;
	}
	public List<String> getBioSubTypeFingerLeftSlap() {
		List<String> bioSubTypeFingerList=new ArrayList<String>();
		bioSubTypeFingerList.add(CommonConstant.LEFT_INDEX_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_MIDDLE_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_RING_FINGER);
		bioSubTypeFingerList.add(CommonConstant.LEFT_LITTLE_FINGER);
		return bioSubTypeFingerList;
	}
	public List<String> getBioSubTypeFingerRightSlap() {
		List<String> bioSubTypeFingerList=new ArrayList<String>();
		bioSubTypeFingerList.add(CommonConstant.RIGHT_INDEX_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_MIDDLE_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_RING_FINGER);
		bioSubTypeFingerList.add(CommonConstant.RIGHT_LITTLE_FINGER);
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
