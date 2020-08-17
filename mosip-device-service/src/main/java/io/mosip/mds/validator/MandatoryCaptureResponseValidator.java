package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

public class MandatoryCaptureResponseValidator extends Validator {
	public MandatoryCaptureResponseValidator()
	{
		super("MandatoryCaptureResponseValidator", "Mandatory Capture Response validator");
	}
	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();
	ObjectMapper jsonMapper = new ObjectMapper();
	
	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations = new ArrayList<>();
		// Check for Biometrics block
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",jsonMapper.writeValueAsString(response));
		if(Objects.nonNull(response))
		{
			validation = commonValidator.setFieldExpected("mdsDecodedResponse","Expected whole Capture decoded Jsone Response",response.getMdsDecodedResponse().toString());
			CaptureResponse cr = (CaptureResponse) response.getMdsDecodedResponse();
			if(Objects.nonNull(cr))
			{
				validation = commonValidator.setFieldExpected("CaptureResponse.biometrics","Expected Array of biometric data",jsonMapper.writeValueAsString(cr.biometrics));
				if(cr.biometrics == null || cr.biometrics.length == 0)
				{
					commonValidator.setFoundMessageStatus(validation,jsonMapper.writeValueAsString(cr.biometrics),"Capture response does not contain biometrics block",CommonConstant.FAILED);
				}
				validations.add(validation);

				for(CaptureResponse.CaptureBiometric bb:cr.biometrics)
				{
					if(!ObjectUtils.isEmpty(bb)) {
						// Check for data elements
						validation = commonValidator.setFieldExpected("biometrics.data","Expected data block",bb.data);
						if(bb.data == null || bb.data.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.data,"Capture response does not contain data in biometrics",CommonConstant.FAILED);
						}
						validations.add(validation);
						// Check for specVersion
						validation = commonValidator.setFieldExpected("biometrics.specVersion","Expected specVersion block",bb.specVersion);
						if(bb.specVersion == null || bb.specVersion.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.specVersion,"Capture response biometrics does not contain specVersion",CommonConstant.FAILED);
						}
						validations.add(validation);
						// Check for hash element
						validation = commonValidator.setFieldExpected("biometrics.hash","Expected hash block",bb.hash);
						if(bb.hash == null || bb.hash.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.hash,"Capture response biometrics does not contain hash value",CommonConstant.FAILED);
						}
						validations.add(validation);

						// Check for sessionKey
						validation = commonValidator.setFieldExpected("biometrics.sessionKey","Expected sessionKey block",bb.sessionKey);
						if(bb.sessionKey == null || bb.sessionKey.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.sessionKey,"Capture response biometrics does not contain sessionKey value",CommonConstant.FAILED);
						}
						validations.add(validation);
						// Check for Thumb Print
						validation = commonValidator.setFieldExpected("biometrics.thumbprint","Expected thumbprint block",bb.thumbprint);
						if(bb.thumbprint == null || bb.thumbprint.isEmpty())
						{
							commonValidator.setFoundMessageStatus(validation,bb.thumbprint,"Capture response biometrics does not contain thumbprint",CommonConstant.FAILED);
						}
						validations.add(validation);
						// Check for Decoded biometrics data
						validation = commonValidator.setFieldExpected("biometrics.dataDecoded","Expected dataDecoded block",jsonMapper.writeValueAsString(bb.dataDecoded));
						if(bb.dataDecoded == null)
						{
							commonValidator.setFoundMessageStatus(validation,jsonMapper.writeValueAsString(bb.dataDecoded),"Capture response biometrics does not contain dataDecoded",CommonConstant.FAILED);
							validations.add(validation);
						}

						else {
							validations = validateDataDecoded(bb.dataDecoded,validations);
							return validations;
						}
					}else {
						commonValidator.setFoundMessageStatus(validation,"Expected biometrics block","Capture response does not contain biometrics values",CommonConstant.FAILED);
					}
					validations.add(validation);
				}
			}
			else
			{
				commonValidator.setFoundMessageStatus(validation,"Found Capture Decoded is null","Capture response is empty",CommonConstant.FAILED);
			}
			validations.add(validation);
		}else
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
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
				commonValidator.setFoundMessageStatus(validation,dataDecoded.bioSubType,"Capture response biometrics dataDecoded does not contain bioSubType",CommonConstant.FAILED);
			}
			validations.add(validation);
		}
		// Check for bioValue in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.bioValue","encrypted with session key and base64urlencoded biometric data",dataDecoded.bioValue);
		if(dataDecoded.bioValue == null || dataDecoded.bioValue.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.bioValue,"Capture response biometrics dataDecoded does not contain bioValue",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceCode in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.deviceCode","A unique code given by MOSIP after successful registration",dataDecoded.deviceCode);
		if(dataDecoded.deviceCode == null || dataDecoded.deviceCode.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.deviceCode,"Capture response biometrics dataDecoded does not contain deviceCode",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for deviceServiceVersion in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.deviceServiceVersion","expected deviceServiceVersion value",dataDecoded.deviceServiceVersion);
		if(dataDecoded.deviceServiceVersion == null || dataDecoded.deviceServiceVersion.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.deviceServiceVersion,"Capture response biometrics dataDecoded does not contain deviceServiceVersion",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for digitalId in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.digitalId","expected digitalId value",dataDecoded.digitalId);
		if(dataDecoded.digitalId == null || dataDecoded.digitalId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.digitalId,"Capture response biometrics dataDecoded does not contain digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for domainUri in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.domainUri","uri of the auth server",dataDecoded.domainUri);
		if(dataDecoded.domainUri == null || dataDecoded.domainUri.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.domainUri,"Capture response biometrics dataDecoded does not contain domainUri",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for env in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.env","Staging | Developer | Pre-Production | Production",dataDecoded.env);
		if(dataDecoded.env == null || dataDecoded.env.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.env,"Capture response biometrics dataDecoded does not contain env",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for purpose in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.purpose"," Auth or Registration",dataDecoded.purpose);
		if(dataDecoded.purpose == null || dataDecoded.purpose.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.purpose,"Capture response biometrics dataDecoded does not contain purpose",CommonConstant.FAILED);
		}
		validations.add(validation);

		// TODO Check for qualityScore in Decoded biometrics data
		// TODO Check for requestedScore in Decoded biometrics data

		// Check for timestamp in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.timestamp","ISO formate timestamp",dataDecoded.timestamp);
		if(dataDecoded.timestamp == null || dataDecoded.timestamp.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.timestamp,"Capture response biometrics dataDecoded does not contain timestamp",CommonConstant.FAILED);
		}
		validations.add(validation);

		// Check for transactionId in Decoded biometrics data
		validation = commonValidator.setFieldExpected("dataDecoded.transactionId","Unique transaction id",dataDecoded.transactionId);
		if(dataDecoded.transactionId == null || dataDecoded.transactionId.isEmpty())
		{
			commonValidator.setFoundMessageStatus(validation,dataDecoded.transactionId,"Capture response biometrics dataDecoded does not contain transactionId",CommonConstant.FAILED);
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