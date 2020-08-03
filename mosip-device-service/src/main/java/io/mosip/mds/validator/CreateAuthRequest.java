package io.mosip.mds.validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Component
public class CreateAuthRequest {

	//TODO set proper url get all detais from test json
	private static final String BASE_URL = "https://extint.technoforte.co.in";

	private static final String IDA_AUTHMANAGER_URL = BASE_URL+"/v1/authmanager/authenticate/clientidsecretkey";

	private static final String IDA_PUBLICKEY_URL = BASE_URL+"/idauthentication/v1/internal/publickey/IDA";

	private static final String URL = BASE_URL+"/idauthentication/v1/auth/UmjbDSra8pzOGd5rVtKekTb9D6VdvOQg4Kmw5TzBdw18mbzzME/748757/9418294";

	private static final String VERSION = "1.0";

	private static final String MOSIP_IDENTITY_AUTH = "mosip.identity.auth";

	private static final String TRANSACTION_ID = "1234567890";

	private static final String UIN = "UIN";

	private static final String UIN_NUMBER = "123456789";

	ObjectMapper mapper = new ObjectMapper();

	private static final String SSL = "SSL";

	@Autowired
	public CryptoUtility cryptoUtil;

	public Object authenticateResponse(String capture) throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();

		// Set Auth Type
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();

		//set bio true always
		authTypeDTO.setBio(true);

		//set bio false always
		authTypeDTO.setOtp(false);

		authRequestDTO.setRequestedAuth(authTypeDTO);

		// TODO set UIN or ref get from test json
		authRequestDTO.setIndividualId(UIN_NUMBER);

		// TODO Set Individual Id type uin or VID
		authRequestDTO.setIndividualIdType(UIN);

		//TODO set transaction id 
		authRequestDTO.setTransactionID(TRANSACTION_ID);
		authRequestDTO.setRequestTime(getUTCCurrentDateTimeISOString());
		authRequestDTO.setConsentObtained(true);

		// TODO confirm id env.getProperty("authRequestId", "mosip.identity.auth")
		authRequestDTO.setId(MOSIP_IDENTITY_AUTH);

		authRequestDTO.setVersion(VERSION);

		Map<String, Object> authRequestMap = mapper.convertValue(authRequestDTO, Map.class);

		RestTemplate restTemplate = createTemplate();
		HttpEntity<Map> httpEntity = new HttpEntity<>(authRequestMap);
		ResponseEntity<Map> authResponse = null;
		String url = URL;
		System.out.println("Auth URL: " + url);
		System.out.println("Auth Request : \n" + new ObjectMapper().writeValueAsString(authRequestMap));
		try {
			authResponse = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, Map.class);
			System.out.println("Auth Response : \n" + new ObjectMapper().writeValueAsString(authResponse));
			System.out.println(authResponse.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			return authResponse.getBody();
		}
		return authResponse.getBody();
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
				.fromUriString(IDA_PUBLICKEY_URL)
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
				.create(IDA_AUTHMANAGER_URL)
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
