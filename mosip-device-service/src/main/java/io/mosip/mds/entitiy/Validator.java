package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.dto.ValidationTestResultDto;
import io.mosip.mds.dto.postresponse.ValidationResult;
import io.mosip.mds.validator.CommonConstant;

public abstract class Validator {

	public enum ValidationStatus
	{
		Pending,
		Passed,
		Failed,
		InternalException
	};

	public Validator(String Name, String Description)
	{
		this.Name = Name;
		this.Description = Description;
	}

	protected Validator()
	{

	}

	public String Name;

	public String Description;

	public final ValidationResult Validate(ValidateResponseRequestDto response)
	{
		ValidationResult validationResult = new ValidationResult();
		validationResult.validationName = Name;
		validationResult.validationDescription = Description;
		ValidationStatus status = ValidationStatus.Pending;
		//ValidationTestResultDto validationTestResultDto = =new ;
		ValidationTestResultDto validationTestResult = new ValidationTestResultDto();
		validationTestResult.setDecodedData(response.getMdsDecodedResponse().toString());

		try{
			List<Validation> validations=DoValidate(response);
			validationTestResult.setValidations(validations);
			status = ValidationStatus.Passed;
			for( Validation validation:validations) {
				if(validation.getStatus().equals(CommonConstant.FAILED))
					status=ValidationStatus.Failed;
				//status = (validations == null || validations.size() == 0)?ValidationStatus.Passed:ValidationStatus.Failed;
			}
			// if(status != ValidationStatus.Passed)
			validationResult.validationTestResultDtos.add(validationTestResult);
		}
		catch(Exception ex)
		{
			status = ValidationStatus.InternalException;
			List<Validation> validations = new ArrayList<Validation>();
			Validation validation = new Validation();
			validation.setStatus(ValidationStatus.InternalException.name());
			validation.setMessage(ex.getMessage());
			validations.add(validation);
			validationTestResult.setValidations(validations);
			validationResult.validationTestResultDtos.add(validationTestResult);
			return validationResult;
		}
		validationResult.status = status.name();
		return validationResult;
	}

	protected abstract List<Validation> DoValidate(ValidateResponseRequestDto response);

	protected abstract boolean checkVersionSupport(String version);

	protected abstract String supportedVersion();
}