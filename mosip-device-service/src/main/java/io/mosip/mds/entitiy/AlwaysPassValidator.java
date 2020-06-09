package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;

public class AlwaysPassValidator extends Validator {

    public AlwaysPassValidator()
    {
        super("AlwaysPassValidator", "Always succeeding validator");
    }

    @Override
    protected List<String> DoValidate(ValidateResponseRequestDto response) {
        List<String> errors = new ArrayList<>();
        return errors;
    }
}