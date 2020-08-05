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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.DataHeader;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.entitiy.DeviceInfoMinimal;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.util.Intent;

public class MdsSignatureValidator extends Validator{

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public MdsSignatureValidator()
	{
		super("MdsSignatureValidator", "jwt signature Validator");
	}

	@Override
	protected List<String> DoValidate(ValidateResponseRequestDto response) {
		List<String> errors = new ArrayList<>();

		if(response.getIntent().equals(Intent.DeviceInfo)) {
			errors = validateDeviceInfoSignature(response, errors);
		}else if(response.getIntent().equals(Intent.Discover)) {
			errors=validateDiscoverDigitalId(response, errors);
		}
		else if(response.getIntent().equals(Intent.Capture) || response.getIntent().equals(Intent.RegistrationCapture)) {
			errors=validateCaptureSignatureTampered(response, errors);
		}
		return errors;
	}

	private List<String> validateDiscoverDigitalId(ValidateResponseRequestDto response, List<String> errors) {
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.
		DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
		if(Objects.isNull(discoverResponse))
		{
			errors.add("Discover response is empty");
			return errors;
		}else {
		errors = validateUnSignedDigitalID(discoverResponse.digitalId);
		}
		return errors;
	}

	private List<String> validateDeviceInfoSignature(ValidateResponseRequestDto response, List<String> errors) {
		try {
			DeviceInfoMinimal[]	deviceInfos = (DeviceInfoMinimal[])(mapper.readValue(response.getMdsResponse().getBytes(), DeviceInfoMinimal[].class));
			for(DeviceInfoMinimal deviceInfoMinimal:deviceInfos)
			{
				try {
					errors = validateSignatureValidity(deviceInfoMinimal.deviceInfo,errors);
					if(errors.size() != 0) {
						return errors;
					}

					if(!validateSignature(deviceInfoMinimal.deviceInfo)) {
						errors.add("MdsResponse signature verification failed");
						return errors;
					}
				} catch (CertificateException | JoseException | IOException e) {
					errors.add("Interuption while validating DiviceInfo Signature->"+e.getMessage());
					return errors;
				}
			}
		} catch (Exception exception) {
			errors.add("Error parsing request input" + exception.getMessage());
		}

		// validate digitalId for signature
		DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
		if(deviceInfoResponse.certification.equals(CommonConstant.L0) && deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
			errors = validateUnSignedDigitalID(deviceInfoResponse.digitalId);
		else
			errors = validateSignedDigitalID(deviceInfoResponse.digitalId);

		return errors;
	}

	private List<String> validateCaptureSignatureTampered(ValidateResponseRequestDto response, List<String> errors) {
		CaptureResponse mdsResponse = null;
		//

		if(Objects.isNull(response))
		{
			errors.add("Response is empty");
			return errors;
		}

		try {
			mdsResponse = (CaptureResponse) (mapper.readValue(response.mdsResponse.getBytes(), CaptureResponse.class));
		} catch (IOException e) {
			errors.add("Error parsing to response input" + e.getMessage());
		}

		for (CaptureResponse.CaptureBiometric biometric : mdsResponse.biometrics) {

			if (biometric.getData() != null) {
				try {
					if(!validateSignature(biometric.getData())) {
						errors.add("MdsResponse signature verification failed");
						return errors;
					}
				} catch (CertificateException | JoseException | IOException e) {
					errors.add("mdsResponse with Invalid Signature" + e);
					return errors;
					//e.printStackTrace();
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
						errors = validateSignedDigitalID(dataDecoded.digitalId);
						return errors;
					}
				}
			}
		}
		return errors;
	}

	public List<String> validateSignedDigitalID(String digitalId) {
		List<String> errors= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		if(parts.length != 3) {
			errors.add("Missing header|payload|signature in digitalId");				
			return errors; 
		}
		errors=mandatoryParamDigitalIdHeader(parts[0],errors);
		if(errors.size()!=0)return errors;

		try {
			if(!validateSignature(digitalId)) {
				errors.add(" digitalId signature verification failed");
				return errors;
			}
		} catch (CertificateException | JoseException | IOException e) {
			errors.add("Interuption while validating digitalId Signature->"+e.getMessage());
		}
		return errors;
	}

	public List<String> validateUnSignedDigitalID(String digitalId) {
		List<String> errors= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		if(parts.length != 1) {
			errors.add("digitalId formate is Invalid,not a unsigned digitalId");				
			return errors; 
		}
		return errors;
	}

	//TODO check header validation as per spec
	private List<String> mandatoryParamDigitalIdHeader(String header, List<String> errors){
		try {
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));

			if(decodedHeader.alg == null || decodedHeader.alg.isEmpty())
			{
				errors.add("Response DigitalId does not contain alg block in header");
				return errors;
			}
			if(decodedHeader.typ == null || decodedHeader.typ.isEmpty())
			{
				errors.add("Response DigitalId does not contain typ block in header");
				return errors;
			}
			if(decodedHeader.x5c == null || decodedHeader.x5c.size()==0)
			{
				errors.add("Response DigitalId does not contain x5c block in header");
				return errors;
			}
		} catch (Exception e) {
			errors.add("(Invalid Digital Id) Error interpreting digital id");		
		}
		return errors;
	}

	public static boolean validateSignature(String signature) throws JoseException, IOException, CertificateException {

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

	public static List<String> validateSignatureValidity(String signature,List<String> errors) throws JoseException, IOException, CertificateException {
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

			errors.add(" CertificateExpiredException - " + "with Message - "+ e.getMessage() );
			return errors;
		}
		catch (CertificateNotYetValidException e) {
			errors.add(" CertificateNotYetValidException - " + "with Message - "+e.getMessage() );
			return errors;
		}
		return errors;
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
