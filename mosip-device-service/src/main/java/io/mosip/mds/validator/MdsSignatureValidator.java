package io.mosip.mds.validator;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.DataHeader;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.DeviceInfoMinimal;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.util.Intent;

@Component
public class MdsSignatureValidator extends Validator{

	@Autowired
	private ObjectMapper mapper;

	private Validation validation = new Validation();

	@Autowired
	private CommonValidator commonValidator;

	//	static {
	//		mapper = new ObjectMapper();
	//		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	//	}

	public MdsSignatureValidator()
	{
		super("MdsSignatureValidator", "jwt signature Validator");
	}

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations = new ArrayList<>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",mapper.writeValueAsString(response));		
		if(Objects.nonNull(response))
		{
			validations.add(validation);
			if(response.getIntent().equals(Intent.DeviceInfo)) {
				validations = validateDeviceInfoSignature(response, validations);
			}else if(response.getIntent().equals(Intent.Discover)) {
				validations=validateDiscoverDigitalId(response, validations);
			}
			else if(response.getIntent().equals(Intent.Capture) || response.getIntent().equals(Intent.RegistrationCapture)) {
				validations=validateCaptureSignatureTampered(response, validations);
			}
		}else {
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);			
		}
		return validations;
	}

	private List<Validation> validateDiscoverDigitalId(ValidateResponseRequestDto response, List<Validation> validations) {
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.
		DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
		validation = commonValidator.setFieldExpected("response.getMdsDecodedResponse()","Expected whole discover decoded Jsone Response",discoverResponse.toString());
		if(Objects.isNull(discoverResponse))
		{
			commonValidator.setFoundMessageStatus(validation,"Found Discover Decoded is null","Discover response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}else {
			validations = validateUnSignedDigitalID(discoverResponse.digitalId,validations);
		}
		validations.add(validation);
		return validations;
	}

	private List<Validation> validateDeviceInfoSignature(ValidateResponseRequestDto response, List<Validation> validations) {
		try {
			validation = commonValidator.setFieldExpected("response.getMdsResponse()","JWT Signed ,Array of Device info Details",response.getMdsResponse());
			DeviceInfoMinimal[]	deviceInfos = (DeviceInfoMinimal[])(mapper.readValue(response.getMdsResponse().getBytes(), DeviceInfoMinimal[].class));
			validations.add(validation);

			for(DeviceInfoMinimal deviceInfoMinimal:deviceInfos)
			{
				validation = commonValidator.setFieldExpected("deviceInfoMinimal.deviceInfo","Valid JWT signed Device info Details",deviceInfoMinimal.deviceInfo);
				try {
					validations = validateSignatureValidity(deviceInfoMinimal.deviceInfo,validations);
					String [] parts = deviceInfoMinimal.deviceInfo.split("\\.");
					validation = commonValidator.setFieldExpected("JWT Signed deviceInfoMinimal.deviceInfo","Expected Signed deviceInfoMinimal.deviceInfo with header,payload,signature",deviceInfoMinimal.deviceInfo);
					if(parts.length != 3) {
						commonValidator.setFoundMessageStatus(validation,"Found deviceInfoMinimal.deviceInfo is not valid signed response","Missing header|payload|signature in deviceInfoMinimal.deviceInfo",CommonConstant.FAILED);					
						validations.add(validation);
						}
					if(!validateSignature(deviceInfoMinimal.deviceInfo)) {

						validation = commonValidator.setFieldExpected("JWT Signed device info","Expected Signed device info data with header,payload,signature",deviceInfoMinimal.deviceInfo);					
						commonValidator.setFoundMessageStatus(validation,deviceInfoMinimal.deviceInfo,"MdsResponse device info signature verification failed",CommonConstant.FAILED);
						validations.add(validation);
					}else {
						validation = commonValidator.setFieldExpected("JWT Signed device info)","Expected Signed deviceinfo data with header,payload,signature",deviceInfoMinimal.deviceInfo);					
						validations.add(validation);	
					}
					
				} catch (CertificateException | JoseException e) {
					commonValidator.setFoundMessageStatus(validation,deviceInfoMinimal.deviceInfo,"Interuption while validating DiviceInfo Signature->"+e.getMessage(),CommonConstant.FAILED);
					validations.add(validation);
					return validations;
				}
			}
		} catch (Exception exception) {
			commonValidator.setFoundMessageStatus(validation,response.getMdsResponse(),"Error parsing request input" + exception.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}

		// validate digitalId for signature
		DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
		if(deviceInfoResponse.certification.equals(CommonConstant.L0) && deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
			validations = validateUnSignedDigitalID(deviceInfoResponse.digitalId,validations);
		else
			validations = validateSignedDigitalID(deviceInfoResponse.digitalId,validations);

		return validations;
	}

	private List<Validation> validateCaptureSignatureTampered(ValidateResponseRequestDto response, List<Validation> validations) {
		CaptureResponse mdsResponse = null;
		if(Objects.nonNull(response))
		{
			validation = commonValidator.setFieldExpected("response.mdsResponse","Expected JWT format mdsResponse",response.mdsResponse);
			try {
				mdsResponse = (CaptureResponse) (mapper.readValue(response.mdsResponse.getBytes(), CaptureResponse.class));
				validations.add(validation);
			}
			catch (IOException e)
			{
				commonValidator.setFoundMessageStatus(validation,response.getMdsResponse(),"Error parsing to response input" + e.getMessage(),CommonConstant.FAILED);
				validations.add(validation);
			}

			for (CaptureResponse.CaptureBiometric biometric : mdsResponse.biometrics) {
				validation = commonValidator.setFieldExpected("biometric","biometric details",biometric.toString());
				if (biometric.getData() != null) {
					String [] parts = biometric.getData().split("\\.");
					validation = commonValidator.setFieldExpected("JWT Signed biometric.getData().size()","Expected Signed biometric data with header,payload,signature",biometric.getData());
					if(parts.length != 3) {
						commonValidator.setFoundMessageStatus(validation,"Found biometric.getData() is not valid signed response","Missing header|payload|signature in data block",CommonConstant.FAILED);
					}
					validations.add(validation);
					try {
						if(!validateSignature(biometric.getData())) {
							validation = commonValidator.setFieldExpected("JWT Signed biometric.getData()","Expected Signed biometric data with header,payload,signature",biometric.getData());					
							commonValidator.setFoundMessageStatus(validation,biometric.toString(),"MdsResponse signature verification failed",CommonConstant.FAILED);
							validations.add(validation);
						}else {
							validation = commonValidator.setFieldExpected("JWT Signed biometric.getData()","Expected Signed biometric data with header,payload,signature",biometric.getData());					
							validations.add(validation);	
						}
					} catch (CertificateException | JoseException e) {
						commonValidator.setFoundMessageStatus(validation,"Excetption while validating signature","mdsResponse with Invalid Signature" + e,CommonConstant.FAILED);
						validations.add(validation);
					}
				}else {
					commonValidator.setFoundMessageStatus(validation,"biometric.getData() in empty","data block is empty",CommonConstant.FAILED);
					validations.add(validation);
				}
			}

			CaptureResponse cr = (CaptureResponse) response.getMdsDecodedResponse();
			if(!(Objects.isNull(cr)))
			{
				if(!(cr.biometrics == null || cr.biometrics.length == 0))
				{
					for(CaptureResponse.CaptureBiometric bb:cr.biometrics)
					{
						CaptureBiometricData dataDecoded = bb.dataDecoded;
						if(Objects.nonNull(dataDecoded)) {
							validations = validateSignedDigitalID(dataDecoded.digitalId,validations);
							validations = validateSignatureValidity(dataDecoded.digitalId,validations);
							return validations;
						}
					}
				}
			}
		}
		else{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	public List<Validation> validateSignedDigitalID(String digitalId,List<Validation> validations) {
		//List<Validation> validations= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		validation = commonValidator.setFieldExpected("digitalId","Expected Signed digitalId with header,payload,signature",digitalId);
		if(parts.length != 3) {
			commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid signed digitalId","Missing header|payload|signature in digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);
		validations=mandatoryParamDigitalIdHeader(parts[0],validations);
		validations=validValueHeader(parts[0],validations);
		try {
			if(!validateSignature(digitalId)) {
				validation = commonValidator.setFieldExpected("JWT Signed digital ID","Expected Signed digital ID data with header,payload,signature",digitalId);					
				commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid signed digitalId","digitalId signature verification failed",CommonConstant.FAILED);
				validations.add(validation);
			}else {
				validation = commonValidator.setFieldExpected("JWT Signed digital ID","Expected Signed digital ID data with header,payload,signature",digitalId);					
				validations.add(validation);	
			}
		} catch (CertificateException | JoseException e) {
			commonValidator.setFoundMessageStatus(validation,"Exception while processing","Interuption while validating digitalId Signature->"+e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	private List<Validation> validValueHeader(String header, List<Validation> validations) {
		try {
			validation = commonValidator.setFieldExpected("header","Expected Proper header",header);
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));
			validations.add(validation);			
			validation = commonValidator.setFieldExpected("decodedHeader.alg","RS256",decodedHeader.alg);
			if(!decodedHeader.alg.equals("RS256"))
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.alg,"Response DigitalId alg block in header is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("decodedHeader.typ","JWT",decodedHeader.typ);
			if(decodedHeader.typ.equals("JWT"))
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.typ,"Response DigitalId typ block in header is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);
		} catch (Exception e) {
			commonValidator.setFoundMessageStatus(validation,header,"(Invalid Digital Id) Error interpreting header",CommonConstant.FAILED);
			validations.add(validation); 
		}
		return validations;
	}

	public List<Validation> validateUnSignedDigitalID(String digitalId,List<Validation> validations) {
		//List<Validation> validations= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		validation = commonValidator.setFieldExpected("digitalId","Expected UnSigned digitalId with payload only",digitalId);
		if(parts.length != 1) {
			commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid unsigned digitalId","digitalId formate is Invalid",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	//TODO check header validation as per spec
	private List<Validation> mandatoryParamDigitalIdHeader(String header, List<Validation> validations){
		try {
			validation = commonValidator.setFieldExpected("header","Expected Proper header",header);
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));
			validations.add(validation);			
			validation = commonValidator.setFieldExpected("decodedHeader.alg","RS256",decodedHeader.alg);
			if(decodedHeader.alg == null || decodedHeader.alg.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.alg,"Response DigitalId does not contain alg block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("decodedHeader.typ","JWT",decodedHeader.typ);
			if(decodedHeader.typ == null || decodedHeader.typ.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.typ,"Response DigitalId does not contain typ block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("decodedHeader.x5c","Certificate of the FTM chip",decodedHeader.x5c.toString());
			if(decodedHeader.x5c == null || decodedHeader.x5c.size()==0)
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.x5c.toString(),"Response DigitalId does not contain x5c block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
		} catch (Exception e) {
			commonValidator.setFoundMessageStatus(validation,header,"(Invalid Digital Id) Error interpreting header",CommonConstant.FAILED);
			validations.add(validation); 
		}
		return validations;
	}

	public boolean validateSignature(String signature) throws JoseException, CertificateExpiredException, CertificateNotYetValidException {
		JsonWebSignature jws = new JsonWebSignature();	
		jws.setCompactSerialization(signature);
		List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
		X509Certificate certificate = certificateChainHeaderValue.get(0);
		certificate.checkValidity();
		PublicKey publicKey = certificate.getPublicKey();
		jws.setKey(publicKey);
		return jws.verifySignature();
		// TODO handle signature with certificate
	}

	public List<Validation> validateSignatureValidity(String signature,List<Validation> validations) {
		validation = commonValidator.setFieldExpected("signature","signature validity",signature);

		try {
			JsonWebSignature jws = new JsonWebSignature();	
			jws.setCompactSerialization(signature);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);
			certificate.checkValidity();
			PublicKey publicKey = certificate.getPublicKey();
			jws.setKey(publicKey);
			// TODO do for proper signature validity
			jws.getLeafCertificateHeaderValue().checkValidity();
		}catch (CertificateExpiredException e) {
			commonValidator.setFoundMessageStatus(validation,signature," CertificateExpiredException - " + "with Message - "+ e.getMessage() ,CommonConstant.FAILED);
			validations.add(validation);
		}
		catch (CertificateNotYetValidException e) {
			commonValidator.setFoundMessageStatus(validation,signature," CertificateNotYetValidException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		} catch (JoseException e) {
			commonValidator.setFoundMessageStatus(validation,signature," JoseException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}
		validations.add(validation);

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
