package io.mosip.mds.validator;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.bouncycastle.util.io.pem.PemReader;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.DataHeader;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.entitiy.DeviceInfoMinimal;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.util.Intent;

public class MdsSignatureValidator extends Validator{

	private static ObjectMapper mapper;

	Validation validation = new Validation();

	CommonValidator commonValidator = new CommonValidator();
	ObjectMapper jsonMapper = new ObjectMapper();
	
	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public MdsSignatureValidator()
	{
		super("MdsSignatureValidator", "jwt signature Validator");
	}

	@Override
	protected List<Validation> DoValidate(ValidateResponseRequestDto response) throws JsonProcessingException {
		List<Validation> validations = new ArrayList<>();
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",jsonMapper.writeValueAsString(response));		
		if(Objects.nonNull(response))
		{
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
		}
		validations.add(validation);
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
			validations = validateUnSignedDigitalID(discoverResponse.digitalId);
		}
		validations.add(validation);
		return validations;
	}

	private List<Validation> validateDeviceInfoSignature(ValidateResponseRequestDto response, List<Validation> validations) {
		try {
			validation = commonValidator.setFieldExpected("response.getMdsResponse()","JWT Signed ,Array of Device info Details",response.getMdsResponse());

			DeviceInfoMinimal[]	deviceInfos = (DeviceInfoMinimal[])(mapper.readValue(response.getMdsResponse().getBytes(), DeviceInfoMinimal[].class));
			for(DeviceInfoMinimal deviceInfoMinimal:deviceInfos)
			{
				validation = commonValidator.setFieldExpected("deviceInfoMinimal.deviceInfo","Valid JWT signed Device info Details",deviceInfoMinimal.deviceInfo);
				try {
					validations = validateSignatureValidity(deviceInfoMinimal.deviceInfo,validations);
					
					if(!validateSignature(deviceInfoMinimal.deviceInfo)) {
						commonValidator.setFoundMessageStatus(validation,deviceInfoMinimal.deviceInfo,"MdsResponse signature verification failed",CommonConstant.FAILED);
					}
					validations.add(validation);
				} catch (CertificateException | JoseException | IOException e) {
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
			validations = validateUnSignedDigitalID(deviceInfoResponse.digitalId);
		else
			validations = validateSignedDigitalID(deviceInfoResponse.digitalId);

		return validations;
	}

	private List<Validation> validateCaptureSignatureTampered(ValidateResponseRequestDto response, List<Validation> validations) {
		CaptureResponse mdsResponse = null;
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",response.toString());		
		if(Objects.nonNull(response))
		{
			validation = commonValidator.setFieldExpected("response.mdsResponse","Expected JWT format mdsResponse",response.mdsResponse);
			try {
				mdsResponse = (CaptureResponse) (mapper.readValue(response.mdsResponse.getBytes(), CaptureResponse.class));
			}
			catch (IOException e)
			{
				commonValidator.setFoundMessageStatus(validation,response.getMdsResponse(),"Error parsing to response input" + e.getMessage(),CommonConstant.FAILED);
				validations.add(validation);
			}

			for (CaptureResponse.CaptureBiometric biometric : mdsResponse.biometrics) {
				validation = commonValidator.setFieldExpected("biometric","biometric details",biometric.toString());
				if (biometric.getData() != null) {
					try {
						if(!validateSignature(biometric.getData())) {
							commonValidator.setFoundMessageStatus(validation,biometric.toString(),"MdsResponse signature verification failed",CommonConstant.FAILED);
						}
						validations.add(validation);
					} catch (CertificateException | JoseException | IOException e) {
						commonValidator.setFoundMessageStatus(validation,"Excetption while validating signature","mdsResponse with Invalid Signature" + e,CommonConstant.FAILED);
						validations.add(validation);
					}
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
							validations = validateSignedDigitalID(dataDecoded.digitalId);
							return validations;
						}
					}
				}
			}
		}
		else{
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	public List<Validation> validateSignedDigitalID(String digitalId) {
		List<Validation> validations= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		validation = commonValidator.setFieldExpected("digitalId","Expected Signed digitalId with header,payload,signature",digitalId);
		if(parts.length != 3) {
			commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid signed digitalId","Missing header|payload|signature in digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);
		validations=mandatoryParamDigitalIdHeader(parts[0],validations);

		try {
			if(!validateSignature(digitalId)) {
				commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid signed digitalId","digitalId signature verification failed",CommonConstant.FAILED);
			}
			validations.add(validation);
		} catch (CertificateException | JoseException | IOException e) {
			commonValidator.setFoundMessageStatus(validation,"Exception while processing","Interuption while validating digitalId Signature->"+e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	public List<Validation> validateUnSignedDigitalID(String digitalId) {
		List<Validation> validations= new ArrayList<>();
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

	public boolean validateSignature(String signature) throws JoseException, IOException, CertificateException {

		JsonWebSignature jws = new JsonWebSignature();

		FileReader certreader = new FileReader("MosipTestCert.pem");
		PemReader certpemReader = new PemReader(certreader);
		final byte[] certpemContent = certpemReader.readPemObject().getContent();
		certpemReader.close();	   
		EncodedKeySpec certspec = new X509EncodedKeySpec(certpemContent);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certspec.getEncoded()));
		PublicKey  publicKey = certificate.getPublicKey();

		jws.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST,   AlgorithmIdentifiers.RSA_USING_SHA256));
		jws.setCompactSerialization(signature);		    
		jws.setKey(publicKey);

		//  System.out.println("JWS validation >>> " + jws.verifySignature());
		//return jws.verifySignature();

		// TODO handle signature with certificate
		return true;
	}

	public List<Validation> validateSignatureValidity(String signature,List<Validation> validations) throws JoseException, IOException, CertificateException {
		validation = commonValidator.setFieldExpected("signature","proper signature",signature);

		JsonWebSignature jws = new JsonWebSignature();

		FileReader certreader = new FileReader("MosipTestCert.pem");
		PemReader certpemReader = new PemReader(certreader);
		final byte[] certpemContent = certpemReader.readPemObject().getContent();
		certpemReader.close();	   
		EncodedKeySpec certspec = new X509EncodedKeySpec(certpemContent);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certspec.getEncoded()));
		PublicKey  publicKey = certificate.getPublicKey();

		jws.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST,   AlgorithmIdentifiers.RSA_USING_SHA256));
		jws.setCompactSerialization(signature);		    
		jws.setKey(publicKey);

		try {
			// TODO do for proper signature validity
			jws.getLeafCertificateHeaderValue().checkValidity();
		}catch (CertificateExpiredException e) {
			commonValidator.setFoundMessageStatus(validation,signature," CertificateExpiredException - " + "with Message - "+ e.getMessage() ,CommonConstant.FAILED);
			validations.add(validation);
		}
		catch (CertificateNotYetValidException e) {
			commonValidator.setFoundMessageStatus(validation,signature," CertificateNotYetValidException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}
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
