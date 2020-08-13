package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometric;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

public class MandatoryRCaptureResponseValidator extends Validator {
	public MandatoryRCaptureResponseValidator()
	{
		super("MandatoryRCaptureResponseValidator", "Mandatory Registration Capture Response validator");
	}

	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();
	ObjectMapper jsonMapper = new ObjectMapper();
	
	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations = new ArrayList<>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",jsonMapper.writeValueAsString(response));
		if(Objects.nonNull(response))
		{
			// Check for Biometrics block
			CaptureResponse registrationCaptureResponse = (CaptureResponse) response.getMdsDecodedResponse();
			validation = commonValidator.setFieldExpected("mdsDecodedResponse","Expected whole Capture decoded Jsone Response",response.getMdsDecodedResponse().toString());
			
			if(Objects.nonNull(registrationCaptureResponse))
			{
				validation = commonValidator.setFieldExpected("registrationCaptureResponse.analysisError",
						"analysis Error should be empty",registrationCaptureResponse.analysisError);
				if(!registrationCaptureResponse.analysisError.isEmpty()) {
					commonValidator.setFoundMessageStatus(validation,registrationCaptureResponse.analysisError,registrationCaptureResponse.analysisError,CommonConstant.FAILED);
					validations.add(validation);
					return validations;
				}

				validation = commonValidator.setFieldExpected("registrationCaptureResponse.biometrics","Expected Array of biometric data",registrationCaptureResponse.biometrics.toString());
				if(registrationCaptureResponse.biometrics == null || registrationCaptureResponse.biometrics.length == 0)
				{
					commonValidator.setFoundMessageStatus(validation,registrationCaptureResponse.biometrics.toString(),"RegistrationCapture response does not contain biometrics block",CommonConstant.FAILED);
				}
				validations.add(validation);
				for(CaptureBiometric bb:registrationCaptureResponse.biometrics)
				{
					if(!ObjectUtils.isEmpty(bb)) {

						// Check for data elements
						validation = commonValidator.setFieldExpected("biometrics.data","Expected data block",bb.data);
						if(bb.data == null || bb.data.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.data,"RegistrationCapture response does not contain data in biometrics",CommonConstant.FAILED);
						}
						validations.add(validation);
						// Check for specVersion
						validation = commonValidator.setFieldExpected("biometrics.specVersion","Expected specVersion block",bb.specVersion);
						if(bb.specVersion == null || bb.specVersion.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.specVersion,"RegistrationCapture response biometrics does not contain specVersion",CommonConstant.FAILED);
						}
						validations.add(validation);
						// Check for hash element
						validation = commonValidator.setFieldExpected("biometrics.hash","Expected hash block",bb.hash);
						if(bb.hash == null || bb.hash.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.hash,"RegistrationCapture response biometrics does not contain hash value",CommonConstant.FAILED);
						}
						validations.add(validation);

						// Check for Decoded biometrics data
						validation = commonValidator.setFieldExpected("biometrics.dataDecoded","Expected dataDecoded block",bb.dataDecoded.toString());
						if(bb.dataDecoded == null)
						{
							commonValidator.setFoundMessageStatus(validation,bb.dataDecoded.toString(),"RegistrationCapture response biometrics does not contain dataDecoded",CommonConstant.FAILED);
							validations.add(validation);
						}
						else {
							validations = validateDataDecoded(bb.dataDecoded,validations);
							return validations;
						}
					}else {
						commonValidator.setFoundMessageStatus(validation,"Expected biometrics block","RegistrationCapture response does not contain biometrics values",CommonConstant.FAILED);
					}
					validations.add(validation);
				}
			}
			else
			{
				commonValidator.setFoundMessageStatus(validation,"Found RegistrationCapture Decoded is null","RegistrationCapture response is empty",CommonConstant.FAILED);
				validations.add(validation);
			}
		}else
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	private List<Validation> validateDataDecoded(CaptureBiometricData dataDecoded, List<Validation> validations) {

		// Check for bioType in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.bioType","Finger | Iris| Face",dataDecoded.bioType);		
		if(dataDecoded.bioType == null || dataDecoded.bioType.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.bioType,"RegistrationCapture response biometrics dataDecoded does not contain bioType",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for bioSubType in Decoded biometrics data
		//check may be empty for face
		if(dataDecoded.bioType == CommonConstant.FINGER || dataDecoded.bioType == CommonConstant.IRIS) {
			validation = commonValidator.setFieldExpected("dataDecoded.bioSubType","expected bioSubType value",dataDecoded.bioSubType);		
			if( dataDecoded.bioSubType == null || dataDecoded.bioSubType.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"RegistrationCapture response biometrics dataDecoded does not contain bioSubType",CommonConstant.FAILED);
			}
			validations.add(validation);
		}
		// Check for bioValue in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.bioValue","expected bioValue block",dataDecoded.bioValue);
		if(dataDecoded.bioValue == null || dataDecoded.bioValue.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.bioValue,"RegistrationCapture response biometrics dataDecoded does not contain bioValue",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceCode in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.deviceCode","expected deviceCode value",dataDecoded.deviceCode);
		if(dataDecoded.deviceCode == null || dataDecoded.deviceCode.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.deviceCode,"RegistrationCapture response biometrics dataDecoded does not contain deviceCode",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceServiceVersion in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.deviceServiceVersion","expected deviceServiceVersion value",dataDecoded.deviceServiceVersion);
		if(dataDecoded.deviceServiceVersion == null || dataDecoded.deviceServiceVersion.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.deviceServiceVersion,"RegistrationCapture response biometrics dataDecoded does not contain deviceServiceVersion",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for digitalId in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.digitalId","expected digitalId value",dataDecoded.digitalId);
		if(dataDecoded.digitalId == null || dataDecoded.digitalId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.digitalId,"RegistrationCapture response biometrics dataDecoded does not contain digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for env in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.env","Staging | Developer | Pre-Production | Production",dataDecoded.env);
		if(dataDecoded.env == null || dataDecoded.env.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.env,"RegistrationCapture response biometrics dataDecoded does not contain env",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for purpose in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.purpose"," Auth or Registration",dataDecoded.purpose);
		if(dataDecoded.purpose == null || dataDecoded.purpose.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.purpose,"RegistrationCapture response biometrics dataDecoded does not contain purpose",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for qualityScore in Decoded biometrics data
		// TODO Check for requestedScore in Decoded biometrics data

		// Check for timestamp in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.timestamp","ISO formate timestamp",dataDecoded.timestamp);
		if(dataDecoded.timestamp == null || dataDecoded.timestamp.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.timestamp,"RegistrationCapture response biometrics dataDecoded does not contain timestamp",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for transactionId in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.transactionId","expected transactionId value",dataDecoded.transactionId);
		if(dataDecoded.transactionId == null || dataDecoded.transactionId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.transactionId,"RegistrationCapture response biometrics dataDecoded does not contain transactionId",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
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
