package io.mosip.mds.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jws.JsonWebSignature;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.mds.authentication.dto.AuthRequestDTO;
import io.mosip.mds.authentication.dto.AuthTypeDTO;
import io.mosip.mds.authentication.dto.BioIdentityInfoDTO;
import io.mosip.mds.authentication.dto.EncryptionRequestDto;
import io.mosip.mds.authentication.dto.EncryptionResponseDto;
import io.mosip.mds.authentication.dto.RequestDTO;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.entitiy.Store;

@Component
public class BioAuthRequestUtil {

	@Autowired
	private ObjectMapper objMapper;

	private static final Logger logger = LoggerFactory.getLogger(BioAuthRequestUtil.class);
	private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	private static String SIGN_REQ_TEMPLATE = "{\r\n" + 
			"  \"id\": \"string\",\r\n" + 
			"  \"metadata\": {},\r\n" + 
			"  \"request\": {\r\n" + 
			"    \"applicationId\": \"IDA\",\r\n" + 
			"    \"dataToSign\": \"%s\",\r\n" + 
			"    \"includeCertHash\": true,\r\n" + 
			"    \"includeCertificate\": true,\r\n" + 
			"    \"includePayload\": false,\r\n" + 
			"    \"referenceId\": \"SIGN\"\r\n" + 
			"  },\r\n" + 
			"  \"requesttime\": \"%s\",\r\n" + 
			"  \"version\": \"string\"\r\n" + 
			"}";

	private static final String VERSION = "1.0";
	private static final String UIN = "UIN";
	private static final String TRANSACTION_ID = "1234567890";
	private static final String MOSIP_IDENTITY_AUTH = "mosip.identity.auth";
	private static final String SIGN_ALGO = "RS256";
	private static final String KEY_ALIAS = "keyalias";

	private static final String KEY_STORE = "PKCS12";

	private static final char[] TEMP_P12_PWD = "qwerty@123".toCharArray();

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private Environment env;

	@Autowired
	public CryptoUtility cryptoUtil;

	public String authenticateResponse(ValidateResponseRequestDto response, String uin) throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);  // Set Auth Type - set bio true always
		authTypeDTO.setOtp(false);  // Set Auth Type - set otp to false always
		authRequestDTO.setRequestedAuth(authTypeDTO);

		authRequestDTO.setIndividualIdType(UIN); //Set Individual Id type uin or VID
//		authRequestDTO.setIndividualId(env.getProperty("auth.request.uin"));
		authRequestDTO.setIndividualId(uin);

		authRequestDTO.setDomainUri("https://extint1.mosip.net");
		authRequestDTO.setEnv("Developer");
		String authToken = getAuthToken();

		//TODO get cert from reading
//String thumbPrint = toHex (JwtUtility.getCertificateThumbprint(certificate)).replace ("-", "").toUpperCase();
		
		X509Certificate EncryptionCertificate = getCertificateFull(authToken,"PARTNER");
//		String thumbprint = CryptoUtil.encodeBase64(getCertificateThumbprint(EncryptionCertificate));
		String EncryptionThumbprint = toHex (getCertificateThumbprint(EncryptionCertificate)).replace ("-", "").toUpperCase();

		X509Certificate BioEncryptionCertificate = getCertificateFull(authToken,"IDA-FIR");
//		String BioThumbprint = CryptoUtil.encodeBase64(getCertificateThumbprint(BioEncryptionCertificate));
		String BioThumbprint = toHex (getCertificateThumbprint(BioEncryptionCertificate)).replace ("-", "").toUpperCase();
		
		RequestDTO requestDTO = new RequestDTO();

		List<BioIdentityInfoDTO> biometrics=new ArrayList<BioIdentityInfoDTO>();
		BioIdentityInfoDTO bioIdentityInfoDTO=new BioIdentityInfoDTO();
		bioIdentityInfoDTO.setThumbprint(BioThumbprint);
		biometrics.add(bioIdentityInfoDTO);
		requestDTO.setBiometrics(biometrics);

		requestDTO.setTimestamp(getUTCCurrentDateTimeISOString());
		Map<String, Object> identityBlock = mapper.convertValue(requestDTO, Map.class);
		if(Objects.nonNull(response))
			identityBlock.put("biometrics", mapper.readValue(response.mdsResponse, Map.class).get("biometrics"));

		EncryptionRequestDto encryptionRequestDto = new EncryptionRequestDto();
		encryptionRequestDto.setIdentityRequest(identityBlock);
		EncryptionResponseDto encryptionResponseDto = null;

		try {
			encryptionResponseDto = kernelEncrypt(encryptionRequestDto,EncryptionCertificate);
//			encryptionResponseDto = kernelEncrypt1(encryptionRequestDto,EncryptionCertificate);

		} catch (Exception e) {
			logger.error("Error during encrypting auth request", e);
		}
		// Set request block
		authRequestDTO.setThumbprint(EncryptionThumbprint);
		authRequestDTO.setRequest(requestDTO);
		authRequestDTO.setTransactionID(TRANSACTION_ID);
		authRequestDTO.setRequestTime(getUTCCurrentDateTimeISOString());
		authRequestDTO.setConsentObtained(true);
		authRequestDTO.setId(MOSIP_IDENTITY_AUTH);
		authRequestDTO.setVersion(VERSION);
		Map<String, Object> authRequestMap = mapper.convertValue(authRequestDTO, Map.class);
		authRequestMap.replace("request", encryptionResponseDto.getEncryptedIdentity());
		authRequestMap.replace("requestSessionKey", encryptionResponseDto.getEncryptedSessionKey());
		authRequestMap.replace("requestHMAC", encryptionResponseDto.getRequestHMAC());
		return doAuthRequest1(authToken, authRequestMap);
	}

	private byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
		return DigestUtils.sha256(cert.getEncoded());
	}

	public String toHex(byte[] bytes) {
        return Hex.encodeHexString(bytes).toUpperCase();
    }
	
	private String doAuthRequest1(String authToken, Map<String, Object> authRequestMap) throws Exception {

		String reqBodyJson = mapper.writeValueAsString(authRequestMap);
		String reqSignature = sign(reqBodyJson);
		RestTemplate restTemplate = createRestTemplate(authToken);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("signature", reqSignature);
		httpHeaders.add("Authorization", reqSignature);
		httpHeaders.add("Content-Type", "application/json");
		HttpEntity<String> httpEntity = new HttpEntity<>(reqBodyJson, httpHeaders);
		String url = String.format(env.getProperty("ida.auth.url"), env.getProperty("auth.request.misplicense.key"),
				env.getProperty("auth.request.partnerid"), env.getProperty("auth.request.partnerapi.key"));
		Map<String, Object> respMap = new LinkedHashMap<>();

		respMap.put("URL", url);

		Map<String, Object> authReqMap = new LinkedHashMap<>();
		authReqMap.put("body", reqBodyJson);
		authReqMap.put("signature", reqSignature);
		respMap.put("authRequest", authReqMap);
		Map<String, Object> authRespBody = new LinkedHashMap<>();
		Object respBody;
		String respSignature;
		try {
			ResponseEntity<Map> authResponse = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
			respBody = authResponse.getBody();
			List<ServiceError> serviceErrorList = ExceptionUtils.getServiceErrorList(mapper.writeValueAsString(respBody));
			if(serviceErrorList.isEmpty()) {
				respSignature = authResponse.getHeaders().get("response-signature").get(0);
				authRespBody.put("signature", respSignature);
			}
		} catch (RestClientException e) {
			respBody = e;
		}

		authRespBody.put("body", respBody);
		respMap.put("authResponse", authRespBody);
		return respBody.toString();
	}

	public RestTemplate createRestTemplate(String authToken) throws NoSuchAlgorithmException, KeyManagementException {
		Encrypt.turnOffSslChecking();
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				if (authToken != null && !authToken.isEmpty()) {
					request.getHeaders().set("Cookie", "Authorization=" + authToken);
				}
				return execution.execute(request, body);
			}
		};

		restTemplate.setInterceptors(Collections.singletonList(interceptor));
		return restTemplate;
	}

	private String doAuthRequest(String authToken, Map<String, Object> authRequestMap) throws Exception {
		try {
			String reqBodyJson = mapper.writeValueAsString(authRequestMap);
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
			RequestBody body = RequestBody.create(mediaType, reqBodyJson);
			Request request = new Request.Builder()
					//curl -X POST "https://dev.mosip.net/v1/keymanager/sign" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"data\": \"string\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\"}"
					.header("signature", sign(CryptoUtil.encodeBase64(reqBodyJson.getBytes("UTF-8"))
							))
					.header( "Authorization" , authToken)
					.url(String.format(env.getProperty("ida.auth.url"), env.getProperty("auth.request.misplicense.key"),
							env.getProperty("auth.request.partnerid"), env.getProperty("auth.request.partnerapi.key")))
					.post(body)
					.build();
			Response idaResponse = client.newCall(request).execute();
			return idaResponse.body().string();

		} catch (IOException | JSONException e) {
			logger.error("Failed to fetch cert from IDA auth server", e);
		}
		return "Failed to get auth response !";
	}

	private EncryptionResponseDto kernelEncrypt(EncryptionRequestDto encryptionRequestDto, X509Certificate certificate)
			throws Exception {
		EncryptionResponseDto encryptionResponseDto = new EncryptionResponseDto();
		String identityBlock = mapper.writeValueAsString(encryptionRequestDto.getIdentityRequest());

		SecretKey secretKey = cryptoUtil.genSecKey();

		byte[] encryptedIdentityBlock = cryptoUtil.symmetricEncrypt(identityBlock.getBytes(StandardCharsets.UTF_8), secretKey);
		encryptionResponseDto.setEncryptedIdentity(CryptoUtil.encodeBase64(encryptedIdentityBlock));

		PublicKey publicKey = certificate.getPublicKey();
		byte[] encryptedSessionKeyByte = cryptoUtil.asymmetricEncrypt((secretKey.getEncoded()), publicKey);
		encryptionResponseDto.setEncryptedSessionKey(CryptoUtil.encodeBase64(encryptedSessionKeyByte));

		byte[] hashByteArr = cryptoUtil.symmetricEncrypt(
				HMACUtils.digestAsPlainText(HMACUtils.generateHash(identityBlock.getBytes(StandardCharsets.UTF_8))).getBytes(StandardCharsets.UTF_8), secretKey);
		encryptionResponseDto.setRequestHMAC(CryptoUtil.encodeBase64(hashByteArr));

		return encryptionResponseDto;
	}

	private EncryptionResponseDto kernelEncrypt1(EncryptionRequestDto encryptionRequestDto, X509Certificate x509Cert) throws Exception	 {
		String identityBlock = objMapper.writeValueAsString(encryptionRequestDto.getIdentityRequest());
		SecretKey secretKey = cryptoUtil.genSecKey();
		EncryptionResponseDto encryptionResponseDto = new EncryptionResponseDto();
		byte[] encryptedIdentityBlock = cryptoUtil.symmetricEncrypt(identityBlock.getBytes(StandardCharsets.UTF_8),
				secretKey);
		encryptionResponseDto.setEncryptedIdentity(Base64.encodeBase64URLSafeString(encryptedIdentityBlock));
		PublicKey publicKey = x509Cert.getPublicKey();
		byte[] encryptedSessionKeyByte = cryptoUtil.asymmetricEncrypt((secretKey.getEncoded()), publicKey);
		encryptionResponseDto.setEncryptedSessionKey(Base64.encodeBase64URLSafeString(encryptedSessionKeyByte));
		byte[] byteArr = cryptoUtil.symmetricEncrypt(digestAsPlainText(HMACUtils2.generateHash(identityBlock.getBytes(StandardCharsets.UTF_8))).getBytes(),
				secretKey);
		encryptionResponseDto.setRequestHMAC(Base64.encodeBase64URLSafeString(byteArr));
		return encryptionResponseDto;
	}

	public static String digestAsPlainText(byte[] data) {
		return DatatypeConverter.printHexBinary(data).toUpperCase();
	}

	private X509Certificate getCertificateFull(String authToken, String role) throws CertificateException {
		String certificate = trimBeginEnd(getCertificate(authToken,role));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
				new ByteArrayInputStream(java.util.Base64.getDecoder().decode(certificate)));
		return x509Certificate;
	}

	//Depriciated
	private String getJWTSignedData(String data, String authToken) throws IOException, JSONException {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(SIGN_REQ_TEMPLATE,
				data, DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder()
				.header("cookie", "Authorization="+authToken)
				.url(env.getProperty("internal.auth.jwtSign.url"))
				.post(body)
				.build();
		Response response = client.newCall(request).execute();
		if(response.isSuccessful()) {
			JSONObject jsonObject = new JSONObject(response.body().string());
			jsonObject = jsonObject.getJSONObject("response");
			return jsonObject.getString("jwtSignedData");
		}
		return "";
	}

	public String getAuthToken() throws IOException {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(AUTH_REQ_TEMPLATE,
				env.getProperty("ida.auth.appid"),
				env.getProperty("ida.auth.clientid"),
				env.getProperty("ida.auth.secretkey"),
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder()
				.url(env.getProperty("ida.authmanager.url"))
				.post(body)
				.build();

		Response response = client.newCall(request).execute();
		if(response.isSuccessful()) {
			return response.header("authorization");
		}
		return "";
	}

	public String sign(String dataToSign) throws Exception
	{

		JsonWebSignature jwSign = new JsonWebSignature();
		String partnerFilePath = Store.getStorePath() + "keys/rp-partner.p12";
		PrivateKeyEntry keyEntry =  getPrivateKeyEntry(partnerFilePath);

		if (Objects.isNull(keyEntry)) {
			throw new KeyStoreException("Key file not available for partner type: " + "rp");
		}
		PrivateKey privateKey = keyEntry.getPrivateKey();
		X509Certificate x509Certificate = (X509Certificate) keyEntry.getCertificate();
		jwSign.setCertificateChainHeaderValue(new X509Certificate[] { x509Certificate });
		jwSign.setPayload(dataToSign);
		jwSign.setAlgorithmHeaderValue(SIGN_ALGO);
		jwSign.setKey(privateKey);
		jwSign.setDoKeyValidation(false);
		return jwSign.getDetachedContentCompactSerialization();
	}
	public static String STORAGE_PATH = null;

	public static String getStorePath()
	{
		String storePath = STORAGE_PATH == null ? System.getProperty("user.dir") :
			STORAGE_PATH;
		if(!storePath.endsWith(File.separator))
			storePath += File.separator;
		File dataDir = getOrCreateDirectory(storePath + "data/");
		storePath = dataDir.getAbsolutePath();
		if(!storePath.endsWith(File.separator))
			storePath += File.separator;
		return storePath;
	}

	public static File getOrCreateDirectory(String path)
	{
		File f = new File(path);
		if(f.isDirectory())
			return f;
		if(f.exists())
			return null;
		if(f.mkdirs())
			return f;
		return null;
	}

	private PrivateKeyEntry getPrivateKeyEntry(String filePath) throws NoSuchAlgorithmException, UnrecoverableEntryException, 
	KeyStoreException, IOException, CertificateException{
		Path path = Paths.get(filePath);
		if (Files.exists(path)){
			KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
			try(InputStream p12FileStream = new FileInputStream(filePath);) {
				keyStore.load(p12FileStream, TEMP_P12_PWD);
				return (PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, new KeyStore.PasswordProtection(TEMP_P12_PWD));
			}
		}
		return null;
	}

	private String getCertificate(String authToken, String role) {
		OkHttpClient client = new OkHttpClient();
		try {
			Request idarequest;
			if(role.equals("PARTNER")) {
				idarequest = new Request.Builder()
						.header("cookie", "Authorization="+authToken)
						.url(env.getProperty("mosip.ida.cert.url"))
						.get()
						.build();
			}else {
				idarequest = new Request.Builder()
						.header("cookie", "Authorization="+authToken)
						.url(env.getProperty("mosip.ida.bio.cert.url"))
						.get()
						.build();
			}
			Response idaResponse = client.newCall(idarequest).execute();
			if(idaResponse.isSuccessful()) {
				JSONObject jsonObject = new JSONObject(idaResponse.body().string());
				jsonObject = jsonObject.getJSONObject("response");
				return jsonObject.getString("certificate");
			}
		} catch (IOException | JSONException e) {
			logger.error("Failed to fetch cert from IDA auth server", e);
		}
		return null;
	}

	private static String getUTCCurrentDateTimeISOString() {
		return DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime());
	}

	private static String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}
}
