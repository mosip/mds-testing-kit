package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) {
		List<Validation> validations= new ArrayList<>();
		validations = validateAuthResponse(response,validations);
		return validations;
	}

	private List<Validation> validateAuthResponse(ValidateResponseRequestDto response, List<Validation> validations) {
		//AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		validation = commonValidator.setFieldExpected("response","Response Field",response.toString());
		if(Objects.nonNull(response))
		{
			try {
				Object authResponse = createAuthRequest.authenticateResponse(response);
				commonValidator.setFoundMessageStatus(validation,authResponse.toString(),"Response from AUTH",CommonConstant.SUCCESS);
				
			} catch (Exception e) {
				commonValidator.setFoundMessageStatus(validation,"Exception while processing",e.getMessage(),CommonConstant.FAILED);
				validations.add(validation);
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
