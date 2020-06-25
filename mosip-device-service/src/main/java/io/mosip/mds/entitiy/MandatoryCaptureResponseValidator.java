package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;

public class MandatoryCaptureResponseValidator extends Validator {

    @Override
    protected List<String> DoValidate(ValidateResponseRequestDto response) {
        List<String> errors = new ArrayList<>();
        // Check for Biometrics block
        CaptureResponse cr = response.captureResponse;
        if(cr.biometrics == null || cr.biometrics.length == 0)
        {
            errors.add("Capture response does not contain biometrics block");
            return errors;
        }
        for(CaptureResponse.CaptureBiometric bb:cr.biometrics)
        {
            // Check for data elements
            
            // Check for hash element
        }
        return errors;

    }
    
}