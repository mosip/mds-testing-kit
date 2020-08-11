package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.validator.CommonConstant;

public class CoinTossValidator extends Validator {

    public CoinTossValidator()
    {
        super("CoinTossValidator", "Randomly succeeding or failing validator");
    }

    @Override
    protected List<Validation> DoValidate(ValidateResponseRequestDto response) {
    	List<Validation> validations = new ArrayList<>();
		if(System.currentTimeMillis() % 2 == 1)
        {
			Validation validation = new Validation();
        	validation.setStatus(CommonConstant.SUCCESS);
        	validation.setMessage("Validation failed due to odd time of run!");
    		validations.add(validation);
    		return validations;
        }
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