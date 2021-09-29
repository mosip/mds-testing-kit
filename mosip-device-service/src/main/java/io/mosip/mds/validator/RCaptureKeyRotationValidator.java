package io.mosip.mds.validator;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.Validator;

@Component
public class RCaptureKeyRotationValidator extends Validator {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	private MdsSignatureValidator mdsSignatureValidator;

	public RCaptureKeyRotationValidator() {
		super("RCaptureKeyRotationValidator", "RCapture Key Rotation Validator");   
	}

	private Validation validation = new Validation();

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response)
			throws JsonProcessingException, IOException {
		List<Validation> validations = new ArrayList<>();
		if(Objects.nonNull(response))
		{
			CaptureResponse mdsResponse = (CaptureResponse) (mapper.readValue(response.mdsResponse.getBytes(), CaptureResponse.class));
			validation = commonValidator.setFieldExpected("JWT Signature biometric.getData() (Certificate found in JWT Header)","Signature Verification","Signature Verification Certificate Valid");					
			for (CaptureResponse.CaptureBiometric biometric : mdsResponse.biometrics) {
				if (biometric.getData() != null) {
					try {
						if(!mdsSignatureValidator.validateSignature(biometric.getData())) {	
							commonValidator.setFoundMessageStatus(validation,"Signature Verification Certificate Not Valid","JWTSignature Signature Verification certificate not valid",CommonConstant.FAILED);
						}
						validateSignatureValidity(biometric.getData());
					}catch (CertificateExpiredException e) {
						commonValidator.setFoundMessageStatus(validation,"Certificate Not Valid"," CertificateExpiredException - " + "with Message - "+ e.getMessage() ,CommonConstant.FAILED);
					}catch (CertificateNotYetValidException e) {
						commonValidator.setFoundMessageStatus(validation,"Certificate Not Valid"," CertificateNotYetValidException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
					} catch (JoseException e) {
						commonValidator.setFoundMessageStatus(validation,"Certificate Not Valid"," JoseException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
					}
				}else {
					commonValidator.setFoundMessageStatus(validation,"biometric.getData() in empty","data block is empty",CommonConstant.FAILED);
					validations.add(validation);
					return validations;
				}
			}
			validations.add(validation);
		}
		return validations;
	}

	private void validateSignatureValidity(String signature) throws CertificateExpiredException, CertificateNotYetValidException, JoseException {
		JsonWebSignature jws = new JsonWebSignature();	
		jws.setCompactSerialization(signature);
		List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
		X509Certificate certificate = certificateChainHeaderValue.get(0);
		certificate.checkValidity();
		PublicKey publicKey = certificate.getPublicKey();
		jws.setKey(publicKey);
		// TODO do for proper signature validity
		jws.getLeafCertificateHeaderValue().checkValidity();
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
