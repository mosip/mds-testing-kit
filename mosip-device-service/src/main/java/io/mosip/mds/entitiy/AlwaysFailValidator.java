package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;

public class AlwaysFailValidator extends Validator {

    public AlwaysFailValidator()
    {
        super("AlwaysFailValidator", "Returns an error every time");
    }


    @Override
    protected List<String> DoValidate(ValidateResponseRequestDto response) {
        List<String> errors = new ArrayList<>();
        errors.add("Validation failed due to error!");
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