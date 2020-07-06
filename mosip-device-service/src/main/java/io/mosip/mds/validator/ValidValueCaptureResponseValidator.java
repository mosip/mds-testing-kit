package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ObjectUtils;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class ValidValueCaptureResponseValidator extends Validator {

	private static final String REGISTRATION = "Registration";
	private static final String AUTH = "Auth";
	private static final String FACE = "Face";
	private static final String IRIS = "Iris";
	private static final String FINGER = "Finger";
	private final List<String> bioSubTypeFingerList= getBioSubTypeFinger();
	private final List<String> bioSubTypeIrisList = getBioSubTypeIris();
	public ValidValueCaptureResponseValidator() {
	     super("ValidValueCaptureResponseValidator", "Valid Value Capture Response Validator");
	}
	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		CaptureResponse cr = response.captureResponse;
		for(CaptureResponse.CaptureBiometric bb:cr.biometrics)
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
		if(!dataDecoded.bioType.equals(FINGER) && !dataDecoded.bioType.equals(IRIS) && !dataDecoded.bioType.equals(FACE))
		{
			errors.add("Capture response biometrics-dataDecoded bioType is invalid");
			return errors;
		}else {

			//Check for bioSubType
			errors = validateBioSubType(errors, dataDecoded);
			if(!ObjectUtils.isEmpty(errors))
				return errors;
		}
		//Check for purpose elements
		if(!dataDecoded.purpose.equals(AUTH) && !dataDecoded.purpose.equals(REGISTRATION) )
		{
			errors.add("Capture response biometrics-dataDecoded purpose is invalid");
			return errors;
		}

		//TODO Check for digitalId dataDecoded.digitalId

		return errors;
	}

	private List<String> validateBioSubType(List<String> errors, CaptureBiometricData dataDecoded) {
		// Check for bioSubType of Finger elements
		if(dataDecoded.bioType.equals(FINGER) &&
				!bioSubTypeFingerList.contains(dataDecoded.bioSubType))
		{
			errors.add("Capture response biometrics bioSubType is invalid for Finger");
			return errors;
		}
		// Check for bioSubType of Iris elements
		if(dataDecoded.bioType.equals(IRIS) &&
				!bioSubTypeIrisList.contains(dataDecoded.bioSubType))
		{
			errors.add("Capture response biometrics bioSubType is invalid for Iris");
			return errors;
		}
		// Check for bioSubType of Face elements
		if(dataDecoded.bioType.equals(FACE) &&
				!(dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty()))
		{
			errors.add("Capture response biometrics bioSubType is invalid for Face");
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


	private List<String> getBioSubTypeIris() {
		List<String> bioSubTypeIrisList = new ArrayList<String>();
		bioSubTypeIrisList.add("Left");
		bioSubTypeIrisList.add("Right");
		bioSubTypeIrisList.add( "UNKNOWN");
		return bioSubTypeIrisList;
	}
	private List<String> getBioSubTypeFinger() {
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
