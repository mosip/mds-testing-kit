package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ObjectUtils;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.dto.ValidateResponseRequestDto;

public class MandatoryCaptureResponseValidator extends Validator {

	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		// Check for Biometrics block
		CaptureResponse cr = response.captureResponse;
		if(cr.biometrics == null || cr.biometrics.length == 0)
		{
			errors.add("Capture response does not contain biometrics block");
			return errors;
		}
		for(CaptureResponse.CaptureBiometric bb:cr.biometrics)
		{
			if(!ObjectUtils.isEmpty(bb)) {
				
				// Check for data elements
				if(bb.data == null || bb.data.isEmpty())
				{
					errors.add("Capture response does not contain data in biometrics");
				}
				// Check for specVersion
				if(bb.specVersion == null || bb.specVersion.isEmpty())
				{
					errors.add("Capture response biometrics does not contain specVersion");
				}
				// Check for hash element
				if(bb.hash == null || bb.hash.isEmpty())
				{
					errors.add("Capture response biometrics does not contain hash value");
				}
				// Check for sessionKey
				if(bb.sessionKey == null || bb.sessionKey.isEmpty())
				{
					errors.add("Capture response biometrics does not contain sessionKey value");
				}
				// Check for Thumb Print
				if(bb.thumbprint == null || bb.thumbprint.isEmpty())
				{
					errors.add("Capture response biometrics does not contain thumbprint");
				}
				// Check for Decoded biometrics data
				if(bb.dataDecoded == null)
				{
					errors.add("Capture response biometrics does not contain thumbprint");
				}
				else {
					errors = validateDataDecoded(bb.dataDecoded,errors);
				}
			}else {
				errors.add("Capture response does not contain biometrics values");
				}
		}
		return errors;
	}

	private List<String> validateDataDecoded(CaptureBiometricData dataDecoded, List<String> errors) {

		// Check for bioType in Decoded biometrics data
		if(dataDecoded.bioType == null || dataDecoded.bioType.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain bioType");
		}

		// TODO Check for bioSubType in Decoded biometrics data
		//check may be empty for face
		//		if(dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty())
		//		{
		//			errors.add("Capture response biometrics dataDecoded does not contain bioSubType");
		//		}

		// Check for bioValue in Decoded biometrics data
		if(dataDecoded.bioValue == null || dataDecoded.bioValue.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain bioValue");
		}

		// Check for deviceCode in Decoded biometrics data
		if(dataDecoded.deviceCode == null || dataDecoded.deviceCode.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain deviceCode");
		}

		// Check for deviceServiceVersion in Decoded biometrics data
		if(dataDecoded.deviceServiceVersion == null || dataDecoded.deviceServiceVersion.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain deviceServiceVersion");
		}

		// Check for digitalId in Decoded biometrics data
		if(dataDecoded.digitalId == null || dataDecoded.digitalId.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain digitalId");
		}

		// Check for domainUri in Decoded biometrics data
		if(dataDecoded.domainUri == null || dataDecoded.domainUri.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain domainUri");
		}

		// Check for env in Decoded biometrics data
		if(dataDecoded.env == null || dataDecoded.env.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain env");
		}

		// Check for purpose in Decoded biometrics data
		if(dataDecoded.purpose == null || dataDecoded.purpose.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain purpose");
		}

		// TODO Check for qualityScore in Decoded biometrics data
		// TODO Check for requestedScore in Decoded biometrics data

		// Check for timestamp in Decoded biometrics data
		if(dataDecoded.timestamp == null || dataDecoded.timestamp.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain timestamp");
		}

		// Check for transactionId in Decoded biometrics data
		if(dataDecoded.transactionId == null || dataDecoded.transactionId.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain transactionId");
		}

		return errors;
	}

}