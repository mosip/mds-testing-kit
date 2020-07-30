package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.service.MDS_0_9_5_ResponseProcessor;

public class MandatoryCaptureResponseValidator extends Validator {
	public MandatoryCaptureResponseValidator()
	{
		super("MandatoryCaptureResponseValidator", "Mandatory Capture Response validator");
	}
	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();
		// Check for Biometrics block
		if(Objects.isNull(response))
		{
			errors.add("Response is empty");
			return errors;
		}
		CaptureResponse cr = (CaptureResponse) response.getMdsDecodedResponse();
		if(Objects.isNull(cr))
		{
			errors.add("Capture Response is empty");
			return errors;
		}
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
					errors.add("Capture response does not contain data block in biometrics");
					return errors;
				}
				// Check for specVersion
				if(bb.specVersion == null || bb.specVersion.isEmpty())
				{
					errors.add("Capture response biometrics does not contain specVersion");
					return errors;
				}
				// Check for hash element
				if(bb.hash == null || bb.hash.isEmpty())
				{
					errors.add("Capture response biometrics does not contain hash value");
					return errors;
				}
				// Check for sessionKey
				if(bb.sessionKey == null || bb.sessionKey.isEmpty())
				{
					errors.add("Capture response biometrics does not contain sessionKey value");
					return errors;
				}
				// Check for Thumb Print
				if(bb.thumbprint == null || bb.thumbprint.isEmpty())
				{
					errors.add("Capture response biometrics does not contain thumbprint");
					return errors;
				}
				// Check for Decoded biometrics data
				if(bb.dataDecoded == null)
				{
					errors.add("Capture response biometrics does not contain dataDecoded");
					return errors;
				}
				else {
					errors = validateDataDecoded(bb.dataDecoded,errors);
					return errors;
				}
			}else {
				errors.add("Capture response does not contain biometrics values");
				return errors;
			}
		}
		return errors;
	}

	private List<String> validateDataDecoded(CaptureBiometricData dataDecoded, List<String> errors) {

		// Check for bioType in Decoded biometrics data
		if(dataDecoded.bioType == null || dataDecoded.bioType.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain bioType");
			return errors;
		}

		// TODO Check for bioSubType in Decoded biometrics data
		//check may be empty for face
		if(dataDecoded.bioType == CommonConstant.FINGER || dataDecoded.bioType == CommonConstant.IRIS)

			if( dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty())
			{
				errors.add("Capture response biometrics dataDecoded does not contain bioSubType");
			}

		// Check for bioValue in Decoded biometrics data
		if(dataDecoded.bioValue == null || dataDecoded.bioValue.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain bioValue");
			return errors;
		}

		// Check for deviceCode in Decoded biometrics data
		if(dataDecoded.deviceCode == null || dataDecoded.deviceCode.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain deviceCode");
			return errors;
		}

		// Check for deviceServiceVersion in Decoded biometrics data
		if(dataDecoded.deviceServiceVersion == null || dataDecoded.deviceServiceVersion.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain deviceServiceVersion");
			return errors;
		}

		// Check for digitalId in Decoded biometrics data
		if(dataDecoded.digitalId == null || dataDecoded.digitalId.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain digitalId");
			return errors;
		}

		// Check for domainUri in Decoded biometrics data
		if(dataDecoded.domainUri == null || dataDecoded.domainUri.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain domainUri");
			return errors;
		}

		// Check for env in Decoded biometrics data
		if(dataDecoded.env == null || dataDecoded.env.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain env");
			return errors;
		}

		// Check for purpose in Decoded biometrics data
		if(dataDecoded.purpose == null || dataDecoded.purpose.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain purpose");
			return errors;
		}

		// TODO Check for qualityScore in Decoded biometrics data
		// TODO Check for requestedScore in Decoded biometrics data

		// Check for timestamp in Decoded biometrics data
		if(dataDecoded.timestamp == null || dataDecoded.timestamp.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain timestamp");
			return errors;
		}

		// Check for transactionId in Decoded biometrics data
		if(dataDecoded.transactionId == null || dataDecoded.transactionId.isEmpty())
		{
			errors.add("Capture response biometrics dataDecoded does not contain transactionId");
			return errors;
		}

		return errors;
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