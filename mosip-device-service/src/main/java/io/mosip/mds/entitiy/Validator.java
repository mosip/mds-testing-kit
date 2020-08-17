package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.dto.ValidationTestResultDto;
import io.mosip.mds.dto.postresponse.ValidationResult;
import io.mosip.mds.validator.CommonConstant;
import io.mosip.mds.validator.CommonValidator;

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


	ObjectMapper jsonMapper = new ObjectMapper();

	public final ValidationResult Validate(ValidateResponseRequestDto response)
	{
		ValidationResult validationResult = new ValidationResult();
		validationResult.validationName = Name;
		validationResult.validationDescription = Description;
		ValidationStatus status = ValidationStatus.Pending;
		//ValidationTestResultDto validationTestResultDto = =new ;
		ValidationTestResultDto validationTestResult = new ValidationTestResultDto(); 
		List<Validation> validations = new ArrayList<Validation>();
		Validation validationException = new Validation();
		CommonValidator commonValidator=new CommonValidator();

		try{
			validationException=commonValidator.setFieldExpected("response", "Expected Valid whole json response", jsonMapper.writeValueAsString(response));			
			validationTestResult.setDecodedData(jsonMapper.writeValueAsString(response.getMdsDecodedResponse()));
			validations=DoValidate(response);
			validationTestResult.setValidations(validations);
			status = ValidationStatus.Passed;
			for( Validation validation:validations) {
				if(validation.getStatus().equals(CommonConstant.FAILED))
					status=ValidationStatus.Failed;
				//status = (validations == null || validations.size() == 0)?ValidationStatus.Passed:ValidationStatus.Failed;
			}
			// if(status != ValidationStatus.Passed)
			validationResult.validationTestResultDtos.add(validationTestResult);
		}catch (JsonProcessingException ex) {
			status = ValidationStatus.Failed;
			validationException.setStatus(ValidationStatus.Failed.name());
			validationException.setMessage("Failed due to JsonProcessingException -> " + ex.getMessage());
			validations.add(validationException);
			validationTestResult.setValidations(validations);
			validationResult.validationTestResultDtos.add(validationTestResult);
		}catch (NullPointerException ex) {
			status = ValidationStatus.Failed;
			validationException.setStatus(ValidationStatus.Failed.name());
			validationException.setMessage("Failed due to NullPointerException -> " + ex.getMessage());
			validations.add(validationException);
			validationTestResult.setValidations(validations);
			validationResult.validationTestResultDtos.add(validationTestResult);
		}
		catch(Exception ex)
		{
			status = ValidationStatus.Failed;
			validationException.setStatus(ValidationStatus.Failed.name());
			validationException.setMessage("Failed due to Exception-> " + ex.getMessage());
			validations.add(validationException);
			validationTestResult.setValidations(validations);
			validationResult.validationTestResultDtos.add(validationTestResult);			
		}
		validationResult.status = status.name();
		return validationResult;
	}

	protected abstract List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException;

	protected abstract boolean checkVersionSupport(String version);

	protected abstract String supportedVersion();
}