package io.mosip.mds.validator;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Validator;

public class AuthRequestResponseValidator extends Validator{

	public AuthRequestResponseValidator()
	{
		super("AuthRequestResponseValidator", "Auth Request Response Validator");
	}

	CreateAuthRequest createAuthRequest = new CreateAuthRequest();
	
	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors=new ArrayList<String>();
		errors = validateAuthResponse(response,errors);

		return errors;
	}

	private List<String> validateAuthResponse(ValidateResponseRequestDto response, List<String> errors) {
		//AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		try {
			 Object authResponse = createAuthRequest.authenticateResponse(response);
			 errors.add(authResponse.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			errors.add(e.getMessage());
		}
//		for(AuthError autherror:authResponseDTO.getErrors()) {
//			if(ObjectUtils.isEmpty(autherror)) {
//				errors.add("Authentication error");
//			}
//		}
		return errors;
	}

	@Override
	protected boolean checkVersionSupport(String version) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected String supportedVersion() {
		// TODO Auto-generated method stub
		return null;
	}

}
