package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.RegistrationCaptureResponse;
import io.mosip.mds.dto.RegistrationCaptureResponse.RegistrationCaptureBiometric;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class ValidValueRCaptureResponseValidator extends Validator{

	private final List<String> bioSubTypeFingerList= getBioSubTypeFinger();
	private final List<String> bioSubTypeIrisList = getBioSubTypeIris();

	public ValidValueRCaptureResponseValidator() {
		super("ValidValueRCaptureResponseValidator", "Valid Value Registration Capture Response Validator");
	}

	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		if(Objects.isNull(response))
		{
			errors.add("Response is empty");
			return errors;
		}
		// Check for Biometrics block
		RegistrationCaptureResponse registrationCaptureResponse = response.registrationCaptureResponse;

		if(Objects.isNull(registrationCaptureResponse))
		{
			errors.add("RegistrationCapture response is empty");
			return errors;
		}
		for(RegistrationCaptureBiometric bb:registrationCaptureResponse.biometrics)
		{
			CaptureBiometricData dataDecoded = bb.dataDecoded;
			errors=validateActualValueDatadecoded(errors, dataDecoded);

			//TODO check for env

			//TODO check time stamp for ISO Format date time with timezone

			//TODO check for requestedScore

			//TODO check for quality score


		}


		return errors;
	}
	private List<String> validateActualValueDatadecoded(List<String> errors, CaptureBiometricData dataDecoded) {
		// Check for bioType elements
		if(!dataDecoded.bioType.equals(CommonConstant.FINGER) && !dataDecoded.bioType.equals(CommonConstant.IRIS) && !dataDecoded.bioType.equals(CommonConstant.FACE))
		{
			errors.add("Registration Capture response biometrics-dataDecoded bioType is invalid");
			return errors;
		}else {

			//Check for bioSubType
			errors = validateBioSubType(errors, dataDecoded);
			if(!ObjectUtils.isEmpty(errors))
				return errors;
		}
		//Check for purpose elements
		if(!dataDecoded.purpose.equals(CommonConstant.AUTH) && !dataDecoded.purpose.equals(CommonConstant.REGISTRATION) )
		{
			errors.add("Registration Capture response biometrics-dataDecoded purpose is invalid");
			return errors;
		}

		//TODO Check for digitalId dataDecoded.digitalId

		return errors;
	}

	private List<String> validateBioSubType(List<String> errors, CaptureBiometricData dataDecoded) {
		// Check for bioSubType of Finger elements
		if(dataDecoded.bioType.equals(CommonConstant.FINGER) &&
				!bioSubTypeFingerList.contains(dataDecoded.bioSubType))
		{
			errors.add("Registration Capture response bioSubType is invalid for Finger");
			return errors;
		}
		// Check for bioSubType of Iris elements
		if(dataDecoded.bioType.equals(CommonConstant.IRIS) &&
				!bioSubTypeIrisList.contains(dataDecoded.bioSubType))
		{
			errors.add("Registration Capture response bioSubType is invalid for Iris");
			return errors;
		}
		// Check for bioSubType of Face elements
		if(dataDecoded.bioType.equals(CommonConstant.FACE) &&
				!(dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty()))
		{
			errors.add("Registration Capture response bioSubType is invalid for Face");
			return errors;
		}

		errors=validateDigitalId(dataDecoded, errors);
		return errors;
	}

	private List<String> validateDigitalId(CaptureBiometricData dataDecoded,List<String> errors) {
		CommonValidator commonValidator=new CommonValidator();
		errors = commonValidator.validateSignedDigitalID(dataDecoded.digitalId);
		return errors;
	}


	public List<String> getBioSubTypeIris() {
		List<String> bioSubTypeIrisList = new ArrayList<String>();
		bioSubTypeIrisList.add("Left");
		bioSubTypeIrisList.add("Right");
		bioSubTypeIrisList.add( "UNKNOWN");
		return bioSubTypeIrisList;
	}
	public List<String> getBioSubTypeFinger() {
		List<String> bioSubTypeFingerList=new ArrayList<String>();
		bioSubTypeFingerList.add("Left IndexFinger");
		bioSubTypeFingerList.add("Left MiddleFinger");
		bioSubTypeFingerList.add("Left RingFinger");
		bioSubTypeFingerList.add("Left LittleFinger");
		bioSubTypeFingerList.add("Left Thumb");
		bioSubTypeFingerList.add("Right IndexFinger");
		bioSubTypeFingerList.add("Right MiddleFinger");
		bioSubTypeFingerList.add("Right RingFinger");
		bioSubTypeFingerList.add("Right LittleFinger");
		bioSubTypeFingerList.add("Right Thumb");
		bioSubTypeFingerList.add("UNKNOWN");

		return bioSubTypeFingerList;
	}

}
