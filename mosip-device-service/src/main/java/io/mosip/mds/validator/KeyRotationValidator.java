package io.mosip.mds.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.util.BioAuthRequestUtil;

@Component
public class KeyRotationValidator extends Validator {

	public KeyRotationValidator() {
		super("KeyRotationValidator", "Key Rotation Validator");   
	}

	private Validation validation = new Validation();

	@Autowired
	private Environment env;

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	BioAuthRequestUtil bioAuthRequestUtil;

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response)
			throws JsonProcessingException, IOException {
		List<Validation> validations = new ArrayList<>();
		if(Objects.nonNull(response))
		{
			try {
				String authResponse = bioAuthRequestUtil.authenticateResponse(response,env.getProperty("auth.request.uin"));
				validation = commonValidator.setFieldExpected("Auth Response from IDA","Expected valid certificate result",CommonConstant.DATA);								
				commonValidator.setFoundMessageStatus(validation,authResponse,"Authentication response",CommonConstant.SUCCESS);
				validations.add(validation);
			} catch (Exception e) {
				validation = commonValidator.setFieldExpected("Auth Response from IDA","Exception from Auth server",CommonConstant.DATA);								
				commonValidator.setFoundMessageStatus(validation,e.toString(),"Authentication response",CommonConstant.FAILED);
				validations.add(validation);
			}
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
