package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

public class AuthRequestResponseValidator extends Validator{

	public AuthRequestResponseValidator()
	{
		super("AuthRequestResponseValidator", "Auth Request Response Validator");
	}

	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();
	CreateAuthRequest createAuthRequest = new CreateAuthRequest();
	ObjectMapper jsonMapper = new ObjectMapper();
	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations= new ArrayList<>();
		validations = validateAuthResponse(response,validations);
		return validations;
	}

	private List<Validation> validateAuthResponse(ValidateResponseRequestDto response, List<Validation> validations) throws JsonProcessingException {
		//AuthResponseDTO authResponseDTO = new AuthResponseDTO();

		validation = commonValidator.setFieldExpected("response","Response Field",jsonMapper.writeValueAsString(response));

		if(Objects.nonNull(response))
		{
			validations.add(validation);
			try {
				validation = commonValidator.setFieldExpected("authResponse","authResponse Field",null);				

				Object authResponse = createAuthRequest.authenticateResponse(response);
				validation = commonValidator.setFieldExpected("authResponse","authResponse Field",jsonMapper.writeValueAsString(authResponse));				
				commonValidator.setFoundMessageStatus(validation,jsonMapper.writeValueAsString(authResponse),"Response from AUTH",CommonConstant.SUCCESS);

			} catch (Exception e) {
				commonValidator.setFoundMessageStatus(validation,"Exception while processing authentication",e.getMessage(),CommonConstant.FAILED);
				validations.add(validation);
				return validations;
			}
			validations.add(validation);
		}
		else
		{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	@Override
	protected boolean checkVersionSupport(String version) {
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
