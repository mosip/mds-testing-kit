package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.validator.CommonConstant;

public class AlwaysFailValidator extends Validator {

    public AlwaysFailValidator()
    {
        super("AlwaysFailValidator", "Returns an error every time");
    }


    @Override
    protected List<Validation> DoValidate(ValidateResponseRequestDto response) {
        List<Validation> validations = new ArrayList<>();
        Validation validation = new Validation();
        validation.setStatus(CommonConstant.FAILED);
        validation.setMessage("Validation failed due to error!");
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