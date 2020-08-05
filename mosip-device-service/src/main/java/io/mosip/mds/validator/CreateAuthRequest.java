package io.mosip.mds.validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.authentication.demo.dto.AuthRequestDTO;
import io.mosip.authentication.demo.dto.AuthTypeDTO;
import io.mosip.authentication.demo.dto.CryptoUtility;
import io.mosip.authentication.demo.dto.CryptomanagerRequestDto;
import io.mosip.authentication.demo.dto.EncryptionRequestDto;
import io.mosip.authentication.demo.dto.EncryptionResponseDto;
import io.mosip.authentication.demo.dto.RequestDTO;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.entitiy.Store;

@Component
public class CreateAuthRequest {

	private static final String VERSION = "1.0";

	private static final String MOSIP_IDENTITY_AUTH = "mosip.identity.auth";

	private static final String TRANSACTION_ID = "1234567890";

	private static final String UIN = "UIN";

	private static final String ASYMMETRIC_ALGORITHM_NAME = "RSA";
	
	ObjectMapper mapper = new ObjectMapper();

	private static final String SSL = "SSL";

	public CryptoUtility cryptoUtil=new CryptoUtility();

	TestExtnDto testExtnDto;
	
	private static Boolean areTestsLoaded = false;

	private static HashMap<String, TestExtnDto> allTests = new HashMap<>();

	private static void loadTests()
	{
		if(!areTestsLoaded)
		{
			TestExtnDto[] tests = Store.getTestDefinitions();
			if(tests != null)
			{
				for(TestExtnDto test:tests)
				{
					allTests.put(test.testId, test);
				}
			}
		}
		areTestsLoaded = true;
	}

	static{
		loadTests();
	}
	public Object authenticateResponse(ValidateResponseRequestDto response) throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		testExtnDto = allTests.get(response.testId);

		// Set Auth Type
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();

		//set bio true always
		authTypeDTO.setBio(true);

		//set bio false always
		authTypeDTO.setOtp(false);

		authRequestDTO.setRequestedAuth(authTypeDTO);

		// TODO set UIN or ref get from test json
		authRequestDTO.setIndividualId(testExtnDto.uinNumber);

		// TODO Set Individual Id type uin or VID
		authRequestDTO.setIndividualIdType(UIN);

		RequestDTO requestDTO = new RequestDTO();
		//TODO setting from kernal
		requestDTO.setTimestamp(getUTCCurrentDateTimeISOString());

		Map<String, Object> identityBlock = mapper.convertValue(requestDTO, Map.class);

		// TODO if bio type true always true
		identityBlock.put("biometrics", mapper.readValue(response.mdsResponse, Map.class).get("biometrics"));

		System.out.println("******* Request before encryption ************ \n\n");
		System.out.println(mapper.writeValueAsString(identityBlock));
		EncryptionRequestDto encryptionRequestDto = new EncryptionRequestDto();
		encryptionRequestDto.setIdentityRequest(identityBlock);
		EncryptionResponseDto kernelEncrypt = null;
		try {
			kernelEncrypt = kernelEncrypt(encryptionRequestDto, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set request block
		authRequestDTO.setRequest(requestDTO);

		//TODO set transaction id 
		authRequestDTO.setTransactionID(TRANSACTION_ID);
		authRequestDTO.setRequestTime(getUTCCurrentDateTimeISOString());
		authRequestDTO.setConsentObtained(true);

		// TODO confirm id env.getProperty("authRequestId", "mosip.identity.auth")
		authRequestDTO.setId(MOSIP_IDENTITY_AUTH);

		authRequestDTO.setVersion(VERSION);

		Map<String, Object> authRequestMap = mapper.convertValue(authRequestDTO, Map.class);

		authRequestMap.replace("request", kernelEncrypt.getEncryptedIdentity());
		authRequestMap.replace("requestSessionKey", kernelEncrypt.getEncryptedSessionKey());
		authRequestMap.replace("requestHMAC", kernelEncrypt.getRequestHMAC());
		
		RestTemplate restTemplate = createTemplate();
		HttpEntity<Map> httpEntity = new HttpEntity<>(authRequestMap);
		ResponseEntity<Map> authResponse = null;
		String url = testExtnDto.baseUrl+testExtnDto.authUrl+testExtnDto.mispLicenseKey+"/"+testExtnDto.partnerId+"/"+testExtnDto.partnerApiKey;
		System.out.println("Auth URL: " + url);
		System.out.println("Auth Request : \n" + new ObjectMapper().writeValueAsString(authRequestMap));
		try {
			authResponse = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, Map.class);
			System.out.println("Auth Response : \n" + new ObjectMapper().writeValueAsString(authResponse));
			System.out.println(authResponse.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authResponse.getBody();
	}

	private EncryptionResponseDto kernelEncrypt(EncryptionRequestDto encryptionRequestDto, boolean isInternal)
			throws Exception {
		EncryptionResponseDto encryptionResponseDto = new EncryptionResponseDto();
		String identityBlock = mapper.writeValueAsString(encryptionRequestDto.getIdentityRequest());

		SecretKey secretKey = cryptoUtil.genSecKey();

		byte[] encryptedIdentityBlock = cryptoUtil.symmetricEncrypt(identityBlock.getBytes(), secretKey);
		encryptionResponseDto.setEncryptedIdentity(Base64.encodeBase64URLSafeString(encryptedIdentityBlock));
		String publicKeyStr = getPublicKey(identityBlock, isInternal);
		PublicKey publicKey = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM_NAME)
				.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publicKeyStr)));
		byte[] encryptedSessionKeyByte = cryptoUtil.asymmetricEncrypt((secretKey.getEncoded()), publicKey);
		encryptionResponseDto.setEncryptedSessionKey(Base64.encodeBase64URLSafeString(encryptedSessionKeyByte));
		byte[] byteArr = cryptoUtil.symmetricEncrypt(
				HMACUtils.digestAsPlainText(HMACUtils.generateHash(identityBlock.getBytes())).getBytes(), secretKey);
		encryptionResponseDto.setRequestHMAC(Base64.encodeBase64URLSafeString(byteArr));
		return encryptionResponseDto;
	}

	private RestTemplate createTemplate() throws KeyManagementException, NoSuchAlgorithmException {
		turnOffSslChecking();
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				String authToken = generateAuthToken();
				if (authToken != null && !authToken.isEmpty()) {
					request.getHeaders().set("Cookie", "Authorization=" + authToken);
				}
				return execution.execute(request, body);
			}
		};

		restTemplate.setInterceptors(Collections.singletonList(interceptor));
		return restTemplate;
	}

	public static void turnOffSslChecking() throws KeyManagementException, java.security.NoSuchAlgorithmException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance(SSL);
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String arg1)
				throws CertificateException {
		}
	} };

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getPublicKey(String data, boolean isInternal)
			throws KeyManagementException, RestClientException, NoSuchAlgorithmException {
		RestTemplate restTemplate = createTemplate();

		CryptomanagerRequestDto request = new CryptomanagerRequestDto();
		request.setApplicationId("IDA");
		request.setData(Base64.encodeBase64URLSafeString(data.getBytes(StandardCharsets.UTF_8)));
		String publicKeyId = "PARTNER";
		request.setReferenceId(publicKeyId);
		String utcTime = getUTCCurrentDateTimeISOString();
		request.setTimeStamp(utcTime);
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put("appId", "IDA");

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(testExtnDto.baseUrl+testExtnDto.idaPublicKeyUrl)
				.queryParam("timeStamp", getUTCCurrentDateTimeISOString())
				.queryParam("referenceId", publicKeyId);
		ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
		return (String) ((Map<String, Object>) response.getBody().get("response")).get("publicKey");
	}

	private String generateAuthToken() {
		ObjectNode requestBody = mapper.createObjectNode();

		//TODO check client id and secret key
		requestBody.put("clientId", "mosip-regproc-client");
		requestBody.put("secretKey", "abc123");

		//TODO check app id
		requestBody.put("appId", "regproc");
		RequestWrapper<ObjectNode> request = new RequestWrapper<>();
		request.setRequesttime(DateUtils.getUTCCurrentDateTime());
		request.setRequest(requestBody);
		ClientResponse response = WebClient
				.create(testExtnDto.baseUrl+testExtnDto.idaAuthManagerUrl)
				.post().syncBody(request).exchange().block();
		List<ResponseCookie> list = response.cookies().get("Authorization");
		if (list != null && !list.isEmpty()) {
			ResponseCookie responseCookie = list.get(0);
			return responseCookie.getValue();
		}
		return "";
	}

	public static String getUTCCurrentDateTimeISOString() {
		return DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime());
	}
}
