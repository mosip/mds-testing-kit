package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.validator.CommonConstant;

public class AlwaysPassValidator extends Validator {

	public AlwaysPassValidator()
	{
		super("AlwaysPassValidator", "Always succeeding validator");
	}

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) {
		List<Validation> validations = new ArrayList<>();
		Validation validation = new Validation();
		validation.setStatus(CommonConstant.SUCCESS);
		validations.add(validation );
		return validations;
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