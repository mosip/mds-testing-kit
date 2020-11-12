package io.mosip.mds.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;
@Component
public class ValidErrorResponseValidator extends Validator{

	private static final String ERROR_CODE = "109";

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	private ObjectMapper mapper;

	public ValidErrorResponseValidator()
	{
		super("ValidErrorResponseValidator", "Error Response Validator");
	}

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response)
			throws JsonProcessingException, IOException {
		List<Validation> validations = new ArrayList<>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",mapper.writeValueAsString(response));		
		if(Objects.nonNull(response))
		{
			validations.add(validation);
			CaptureResponse mdsResponse = null;
			
			try {
				mdsResponse = (CaptureResponse) (mapper.readValue(response.mdsResponse.getBytes(), CaptureResponse.class));

				if (Objects.nonNull(mdsResponse)) {
				for (CaptureResponse.CaptureBiometric biometric : mdsResponse.biometrics) {
					validation = commonValidator.setFieldExpected("biometric","biometric details",biometric.toString());
					if (biometric.getError() != null) {
						validation = commonValidator.setFieldExpected("biometric.error.errorCode",ERROR_CODE,biometric.error.errorCode);
						if(biometric.getError().getErrorCode().equals(ERROR_CODE)) {
							commonValidator.setFoundMessageStatus(validation,biometric.error.errorCode,"errorCode not Matched",CommonConstant.FAILED);
						}
						validations.add(validation);
					}
					else {
						commonValidator.setFoundMessageStatus(validation,"biometric.getError() in empty","error block is empty",CommonConstant.FAILED);
						validations.add(validation);
					}
				}
				}else {
					commonValidator.setFoundMessageStatus(validation,"response.mdsResponse is empty","error block is empty",CommonConstant.FAILED);
					validations.add(validation);
				}

			}
			catch (IOException e)
			{
				commonValidator.setFoundMessageStatus(validation,response.getMdsResponse(),"Error parsing to response input" + e.getMessage(),CommonConstant.FAILED);
				validations.add(validation);
			}
		}
		else {
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);			
		}

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
