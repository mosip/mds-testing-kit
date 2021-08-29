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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import io.mosip.mds.dto.DataHeader;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.Validation;
import io.mosip.mds.entitiy.DeviceInfoMinimal;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.util.Intent;
import io.mosip.mds.util.SecurityUtil;

@Component
public class MdsSignatureValidator extends Validator{

	@Autowired
	private ObjectMapper mapper;

	private Validation validation = new Validation();

	@Autowired
	TrustValidation trustValidation;

	@Autowired
	private CommonValidator commonValidator;

	@Autowired
	private SecurityUtil securityUtil;
	
	{
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
		validation = commonValidator.setFieldExpected("response","Expected whole Jsone Response",CommonConstant.DATA);		
		if(Objects.nonNull(response))
		{
			validations.add(validation);
			if(response.getIntent().equals(Intent.DeviceInfo)) {
				validations = validateDeviceInfoSignature(response, validations,response.getIntent());
			}else if(response.getIntent().equals(Intent.Discover)) {
				validations=validateDiscoverDigitalId(response, validations);
			}
			else if(response.getIntent().equals(Intent.Capture) || response.getIntent().equals(Intent.RegistrationCapture)) {
				validations=validateCaptureSignatureTampered(response, validations,response.getIntent());
			}
		}else {
			commonValidator.setFoundMessageStatus(validation,"Expected response is null","Response is empty",CommonConstant.FAILED);
			validations.add(validation);			
		}
		return validations;
	}

	private List<Validation> validateDiscoverDigitalId(ValidateResponseRequestDto response, List<Validation> validations) throws JsonProcessingException {
		//digitalId - Digital ID as per the Digital ID definition but it will not be signed.
		DiscoverResponse discoverResponse = (DiscoverResponse) response.getMdsDecodedResponse();
		validation = commonValidator.setFieldExpected("response.getDecodedResponse()","Expected whole not 'Null' discover decoded Jsone Response",mapper.writeValueAsString(discoverResponse));
		if(Objects.isNull(discoverResponse))
		{
			commonValidator.setFoundMessageStatus(validation,"Found Discover Decoded is null","Discover response is empty",CommonConstant.FAILED);
			validations.add(validation);
		}else {
			validations = validateUnSignedDigitalID(discoverResponse.digitalId,validations);
		}
		return validations;
	}

	private List<Validation> validateDeviceInfoSignature(ValidateResponseRequestDto response, List<Validation> validations, Intent intent) {
		try {
			validation = commonValidator.setFieldExpected("response.getResponse()","JWT Signed ,Array of Device info Details",response.getMdsResponse());
			DeviceInfoMinimal[]	deviceInfos = (DeviceInfoMinimal[])(mapper.readValue(response.getMdsResponse().getBytes(), DeviceInfoMinimal[].class));
			validations.add(validation);

			// validate digitalId for signature
			DeviceInfoResponse deviceInfoResponse = (DeviceInfoResponse) response.getMdsDecodedResponse();
			if(deviceInfoResponse.certification.equals(CommonConstant.L0) && deviceInfoResponse.deviceStatus.equals(CommonConstant.NOT_REGISTERED))
				validations = validateUnSignedDigitalID(deviceInfoResponse.digitalId,validations);
			else
				validations = validateSignedDigitalID(deviceInfoResponse.digitalId,validations,intent,deviceInfoResponse.certification);

			
			for(DeviceInfoMinimal deviceInfoMinimal:deviceInfos)
			{
				try {
					validation = commonValidator.setFieldExpected("signeddeviceInfoMinimal.deviceInfo","signature validity",deviceInfoMinimal.deviceInfo);				
					validations = validateSignatureValidity(deviceInfoMinimal.deviceInfo,validations,validation);
					String [] parts = deviceInfoMinimal.deviceInfo.split("\\.");
					validation = commonValidator.setFieldExpected("JWT Signed deviceInfoMinimal.deviceInfo","Expected Signed deviceInfoMinimal.deviceInfo with header,payload,signature",deviceInfoMinimal.deviceInfo);
					if(parts.length != 3) {
						commonValidator.setFoundMessageStatus(validation,"Found deviceInfoMinimal.deviceInfo is not valid signed response","Missing header|payload|signature in deviceInfoMinimal.deviceInfo",CommonConstant.FAILED);					
						validations.add(validation);
					}
					else {
						DeviceInfoResponse info = (DeviceInfoResponse)mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), DeviceInfoResponse.class);
						if(info.deviceStatus.equalsIgnoreCase("Not Registered"))
							info.digitalIdDecoded = (DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(info.digitalId), DigitalId.class));
						else
							info.digitalIdDecoded = (DigitalId) (mapper.readValue(securityUtil.getPayload(info.digitalId), DigitalId.class));
					
						
						String type = null;
						//						info.getDigitalIdDecoded().type;
//						 mapper.readValue(securityUtil.getPayload(deviceInfoMinima), DeviceInfoResponse.class)
						if(info.getDigitalIdDecoded().getType().equalsIgnoreCase(response.getTestManagerDto().biometricType)) {
							validations=mandatoryParamDataHeader(parts[0],validations);
							validations=validValueDataHeader(parts[0],validations,intent);	
						}
					}
					if(!validateSignature(deviceInfoMinimal.deviceInfo)) {

						validation = commonValidator.setFieldExpected("JWT Signed device info (Signature Validation)","Expected Signed device info data with header,payload,signature",deviceInfoMinimal.deviceInfo);					
						commonValidator.setFoundMessageStatus(validation,deviceInfoMinimal.deviceInfo,"SbiResponse device info signature verification failed",CommonConstant.FAILED);
						validations.add(validation);
					}else {
						validation = commonValidator.setFieldExpected("JWT Signed device info (Signature Validation)","Expected Signed deviceinfo data with header,payload,signature",deviceInfoMinimal.deviceInfo);					
						validations.add(validation);	
					}

				} catch (CertificateException | JoseException e) {
					validation = commonValidator.setFieldExpected("deviceInfoMinimal.deviceInfo","Valid JWT signed Device info Details",deviceInfoMinimal.deviceInfo);				
					commonValidator.setFoundMessageStatus(validation,deviceInfoMinimal.deviceInfo,"Interuption while validating DiviceInfo Signature->"+e.getMessage(),CommonConstant.FAILED);
					validations.add(validation);
				}
			}
		} catch (Exception exception) {
			commonValidator.setFoundMessageStatus(validation,response.getMdsResponse(),"Error parsing request input" + exception.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}

		return validations;
	}

	private List<Validation> validateCaptureSignatureTampered(ValidateResponseRequestDto response, List<Validation> validations, Intent intent) {
		CaptureResponse mdsResponse = null;
		if(Objects.nonNull(response))
		{
			validation = commonValidator.setFieldExpected("response.sbiResponse","Expected JWT format sbiResponse",CommonConstant.DATA);
			try {
				mdsResponse = (CaptureResponse) (mapper.readValue(response.mdsResponse.getBytes(), CaptureResponse.class));
				validations.add(validation);
			}
			catch (IOException e)
			{
				commonValidator.setFoundMessageStatus(validation,CommonConstant.DATA,"Error parsing to response input" + e.getMessage(),CommonConstant.FAILED);
				validations.add(validation);
			}

			for (CaptureResponse.CaptureBiometric biometric : mdsResponse.biometrics) {
				validation = commonValidator.setFieldExpected("biometric","biometric details",biometric.toString());
				if (biometric.getData() != null) {
					String [] parts = biometric.getData().split("\\.");
					validation = commonValidator.setFieldExpected("JWT Signed biometric.getData().size()","Expected Signed biometric data with header,payload,signature",CommonConstant.DATA);
					if(parts.length != 3) {
						commonValidator.setFoundMessageStatus(validation,"Found biometric.getData() is not valid signed response","Missing header|payload|signature in data block",CommonConstant.FAILED);
					}
					validations.add(validation);
					validations=mandatoryParamDataHeader(parts[0],validations);
					validations=validValueDataHeader(parts[0],validations,intent);

					validation = commonValidator.setFieldExpected("signed biometric.getData()","signature validity",CommonConstant.DATA);
					validations = validateSignatureValidity(biometric.getData(),validations,validation);

					try {
						if(!validateSignature(biometric.getData())) {
							validation = commonValidator.setFieldExpected("JWT Signed biometric.getData() (Signature Validation)","Expected Signed biometric data with header,payload,signature",CommonConstant.DATA);					
							commonValidator.setFoundMessageStatus(validation,biometric.toString(),"SbiResponse signature verification failed",CommonConstant.FAILED);
							validations.add(validation);
						}else {
							validation = commonValidator.setFieldExpected("JWT Signed biometric.getData() (Signature Validation)","Expected Signed biometric data with header,payload,signature",CommonConstant.DATA);					
							validations.add(validation);	
						}
					} catch (CertificateException | JoseException e) {
						commonValidator.setFoundMessageStatus(validation,"Excetption while validating signature","sbiResponse with Invalid Signature" + e,CommonConstant.FAILED);
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
							validations = validateSignedDigitalID(dataDecoded.digitalId,validations,intent,response.getDeviceInfo().certification);
							validation = commonValidator.setFieldExpected("signed dataDecoded.digitalId","signature validity",CommonConstant.DATA);				
							validations = validateSignatureValidity(dataDecoded.digitalId,validations,validation);
							//							return validations;
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

	public List<Validation> validateSignedDigitalID(String digitalId,List<Validation> validations,Intent intent, String certification) {
		//List<Validation> validations= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		validation = commonValidator.setFieldExpected("digitalId","Expected Signed digitalId with header,payload,signature",CommonConstant.DATA);
		if(parts.length != 3) {
			commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid signed digitalId","Missing header|payload|signature in digitalId",CommonConstant.FAILED);
		}
		validations.add(validation);
		validations=mandatoryParamDigitalIdHeader(parts[0],validations);
		validations=validValueDigitalIdHeader(parts[0],validations,intent,certification);
		try {
			if(!validateSignature(digitalId)) {
				validation = commonValidator.setFieldExpected("JWT Signed digital ID (Signature Validation)","Expected Signed digital ID data with header,payload,signature",CommonConstant.DATA);					
				commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid signed digitalId","digitalId signature verification failed",CommonConstant.FAILED);
				validations.add(validation);
			}else {
				validation = commonValidator.setFieldExpected("JWT Signed digital ID (Signature Validation)","Expected Signed digital ID data with header,payload,signature",CommonConstant.DATA);					
				validations.add(validation);	
			}
		} catch (CertificateException | JoseException e) {
			commonValidator.setFoundMessageStatus(validation,"Exception while processing","Interuption while validating digitalId Signature->"+e.getMessage(),CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
	}

	private List<Validation> validValueDigitalIdHeader(String header, List<Validation> validations, Intent intent, String certification) {
		try {
			validation = commonValidator.setFieldExpected("validValue DigitalIdHeader check header","Expected Proper header",CommonConstant.DATA);
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));
			validations.add(validation);			
			validation = commonValidator.setFieldExpected("validValue DigitalIdHeader check decodedHeader.alg","RS256",decodedHeader.alg);
			if(!decodedHeader.alg.equals("RS256"))
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.alg,"Response DigitalId alg block in header is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("validValue DigitalIdHeader check decodedHeader.typ","JWT",decodedHeader.typ);
			if(!decodedHeader.typ.equals("JWT"))
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.typ,"Response DigitalId typ block in header is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);

			if(intent.equals(Intent.DeviceInfo) || intent.equals(Intent.RegistrationCapture)) {
				validation = commonValidator.setFieldExpected("Trust Root Validation for DigitalId","Succes Response","");			
				if(certification.equals(CommonConstant.L0)) {
				trustValidation.trustRootValidation(decodedHeader.x5c.get(0), validation,CommonConstant.DEVICE);
				validations.add(validation);
				}
				else if(certification.equals(CommonConstant.L1)) {
					trustValidation.trustRootValidation(decodedHeader.x5c.get(0), validation,CommonConstant.FTM);
					validations.add(validation);
				}
			}
		} catch (Exception e) {
			validation = commonValidator.setFieldExpected("validValue DigitalIdHeader check header","complete header",CommonConstant.DATA);
			commonValidator.setFoundMessageStatus(validation,CommonConstant.DATA,"validValueDigitalIdHeader: (Invalid Digital Id) Error interpreting header",CommonConstant.FAILED);
			validations.add(validation); 
		}
		return validations;
	}

	private List<Validation> validValueDataHeader(String header, List<Validation> validations, Intent intent) {
		try {
			validation = commonValidator.setFieldExpected("validValue check header","Expected Proper header",CommonConstant.DATA);
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));
			validations.add(validation);			
			validation = commonValidator.setFieldExpected("validValue check decodedHeader.alg","RS256",decodedHeader.alg);
			if(!decodedHeader.alg.equals("RS256"))
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.alg,"Response DigitalId alg block in header is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("validValue check decodedHeader.typ","JWT",decodedHeader.typ);
			if(!decodedHeader.typ.equals("JWT"))
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.typ,"Response DigitalId typ block in header is invalid",CommonConstant.FAILED);
			}
			validations.add(validation);

			if(intent.equals(Intent.DeviceInfo) || intent.equals(Intent.RegistrationCapture)) {
				validation = commonValidator.setFieldExpected("Trust Root Validation","Succes Response","");			

				trustValidation.trustRootValidation(decodedHeader.x5c.get(0), validation,CommonConstant.DEVICE);
				validations.add(validation);
			}

		} catch (Exception e) {
			validation = commonValidator.setFieldExpected("validValue check header","complete header",CommonConstant.DATA);
			commonValidator.setFoundMessageStatus(validation,CommonConstant.DATA,"validValueDataHeader: (Invalid Digital Id) Error interpreting header",CommonConstant.FAILED);
			validations.add(validation); 
		}
		return validations;
	}

	public List<Validation> validateUnSignedDigitalID(String digitalId,List<Validation> validations) {
		//List<Validation> validations= new ArrayList<>();
		String [] parts = digitalId.split("\\.");
		validation = commonValidator.setFieldExpected("digitalId","Expected UnSigned digitalId with payload only",CommonConstant.DATA);
		if(parts.length != 1) {
			commonValidator.setFoundMessageStatus(validation,"Found digitalId is not valid unsigned digitalId","digitalId formate is Invalid",CommonConstant.FAILED);
		}
		validations.add(validation);
		return validations;
	}

	//check header validation as per spec fir digitalID
	private List<Validation> mandatoryParamDigitalIdHeader(String header, List<Validation> validations){
		try {
			validation = commonValidator.setFieldExpected("Mandatory DigitalIdHeader check header","Expected Proper header",CommonConstant.DATA);
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));
			validations.add(validation);			
			validation = commonValidator.setFieldExpected("Mandatory DigitalIdHeader check decodedHeader.alg","RS256",decodedHeader.alg);
			if(decodedHeader.alg == null || decodedHeader.alg.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.alg,"Response DigitalId does not contain alg block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("Mandatory DigitalIdHeader check decodedHeader.typ","JWT",decodedHeader.typ);
			if(decodedHeader.typ == null || decodedHeader.typ.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.typ,"Response DigitalId does not contain typ block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("Mandatory DigitalIdHeader check decodedHeader.x5c","Certificate of the FTM chip",CommonConstant.DATA);
			if(decodedHeader.x5c == null || decodedHeader.x5c.size()==0)
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.x5c.toString(),"Response DigitalId does not contain x5c block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
		} catch (Exception e) {
			validation = commonValidator.setFieldExpected("Mandatory DigitalIdHeader check header","Expected Proper header",CommonConstant.DATA);
			commonValidator.setFoundMessageStatus(validation,CommonConstant.DATA,"(Invalid Digital Id) Error interpreting header",CommonConstant.FAILED);
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

	public List<Validation> validateSignatureValidity(String signature,List<Validation> validations,Validation validation) {

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
		}
		catch (CertificateNotYetValidException e) {
			commonValidator.setFoundMessageStatus(validation,signature," CertificateNotYetValidException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
		} catch (JoseException e) {
			commonValidator.setFoundMessageStatus(validation,signature," JoseException - " + "with Message - "+e.getMessage(),CommonConstant.FAILED);
		}
		validations.add(validation);

		return validations;
	}

	//TODO check header validation as per spec
	private List<Validation> mandatoryParamDataHeader(String header, List<Validation> validations){
		try {
			validation = commonValidator.setFieldExpected("Mandatory check header","Expected Proper header",CommonConstant.DATA);
			DataHeader decodedHeader = (DataHeader) (mapper.readValue(Base64.getUrlDecoder().decode(header),
					DataHeader.class));
			validations.add(validation);			
			validation = commonValidator.setFieldExpected("Mandatory check decodedHeader.alg","RS256",decodedHeader.alg);
			if(decodedHeader.alg == null || decodedHeader.alg.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.alg,"Response Data does not contain alg block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("Mandatory check decodedHeader.typ","JWT",decodedHeader.typ);
			if(decodedHeader.typ == null || decodedHeader.typ.isEmpty())
			{
				commonValidator.setFoundMessageStatus(validation,decodedHeader.typ,"Response data does not contain typ block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
			validation = commonValidator.setFieldExpected("Mandatory check decodedHeader.x5c","Certificate of the FTM chip",CommonConstant.DATA);
			if(decodedHeader.x5c == null || decodedHeader.x5c.size()==0)
			{
				commonValidator.setFoundMessageStatus(validation,CommonConstant.DATA,"Response Data does not contain x5c block in header",CommonConstant.FAILED);
			}
			validations.add(validation);
		} catch (Exception e) {
			validation = commonValidator.setFieldExpected("Mandatory check header","Expected Proper header",header);
			commonValidator.setFoundMessageStatus(validation,header,"(Invalid Data) Error interpreting header",CommonConstant.FAILED);
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
