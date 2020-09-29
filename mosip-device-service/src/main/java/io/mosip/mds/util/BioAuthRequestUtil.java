package io.mosip.mds.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

import com.squareup.okhttp.*;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;

import io.mosip.mds.authentication.dto.AuthRequestDTO;
import io.mosip.mds.authentication.dto.AuthTypeDTO;

import io.mosip.mds.authentication.dto.CryptomanagerRequestDto;
import io.mosip.mds.authentication.dto.EncryptionRequestDto;
import io.mosip.mds.authentication.dto.EncryptionResponseDto;
import io.mosip.mds.authentication.dto.RequestDTO;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.entitiy.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BioAuthRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(BioAuthRequestUtil.class);
    private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";
    private static final String VERSION = "1.0";
    private static final String ASYMMETRIC_ALGORITHM_NAME = "RSA";
    private static final String SSL = "SSL";
    private static final String UIN = "uin";
    private static final String TRANSACTION_ID = "1234567890";
    private static final String MOSIP_IDENTITY_AUTH = "mosip.identity.auth";
    private static final String APPLICATIONID = "IDA";
    private static final String REFERENCEID = "PARTNER";

    private ObjectMapper mapper = new ObjectMapper();
    private static RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Autowired
    public CryptoUtility cryptoUtil;

    public String authenticateResponse(ValidateResponseRequestDto response) throws Exception {
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        AuthTypeDTO authTypeDTO = new AuthTypeDTO();
        authTypeDTO.setBio(true);  // Set Auth Type - set bio true always
        authTypeDTO.setOtp(false);  // Set Auth Type - set otp to false always
        authRequestDTO.setRequestedAuth(authTypeDTO);

        authRequestDTO.setIndividualIdType(UIN); //Set Individual Id type uin or VID
        authRequestDTO.setIndividualId(env.getProperty("auth.request.uin"));

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setTimestamp(getUTCCurrentDateTimeISOString());
        Map<String, Object> identityBlock = mapper.convertValue(requestDTO, Map.class);
        if(Objects.nonNull(response))
            identityBlock.put("biometrics", mapper.readValue(response.mdsResponse, Map.class).get("biometrics"));

        EncryptionRequestDto encryptionRequestDto = new EncryptionRequestDto();
        encryptionRequestDto.setIdentityRequest(identityBlock);
        EncryptionResponseDto encryptionResponseDto = null;

        String authToken = getAuthToken();
        try {
            encryptionResponseDto = kernelEncrypt(encryptionRequestDto, authToken);
        } catch (Exception e) {
            logger.error("Error during encrypting auth request", e);
        }
        // Set request block
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
        return doAuthRequest(authToken, authRequestMap);
    }


    private String doAuthRequest(String authToken, Map<String, Object> authRequestMap) {
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, mapper.writeValueAsString(authRequestMap));
            Request request = new Request.Builder()
                    .header("cookie", "Authorization="+authToken)
                    .url(String.format(env.getProperty("ida.auth.url"), env.getProperty("auth.request.misplicense.key"),
                            env.getProperty("auth.request.partnerid"), env.getProperty("auth.request.partnerapi.key")))
                    .post(body)
                    .build();

            Response idaResponse = client.newCall(request).execute();
            return idaResponse.body().string();

        } catch (IOException e) {
            logger.error("Failed to fetch cert from IDA auth server", e);
        }
        return "Failed to get auth response !";
    }

    private EncryptionResponseDto kernelEncrypt(EncryptionRequestDto encryptionRequestDto, String authToken)
            throws Exception {
        EncryptionResponseDto encryptionResponseDto = new EncryptionResponseDto();
        String identityBlock = mapper.writeValueAsString(encryptionRequestDto.getIdentityRequest());
        SecretKey secretKey = cryptoUtil.genSecKey();

        byte[] encryptedIdentityBlock = cryptoUtil.symmetricEncrypt(identityBlock.getBytes(), secretKey);
        encryptionResponseDto.setEncryptedIdentity(Base64.encodeBase64URLSafeString(encryptedIdentityBlock));
        PublicKey publicKey = getPublicKey(authToken);
        byte[] encryptedSessionKeyByte = cryptoUtil.asymmetricEncrypt((secretKey.getEncoded()), publicKey);
        encryptionResponseDto.setEncryptedSessionKey(Base64.encodeBase64URLSafeString(encryptedSessionKeyByte));
        byte[] byteArr = cryptoUtil.symmetricEncrypt(
                HMACUtils.digestAsPlainText(HMACUtils.generateHash(identityBlock.getBytes())).getBytes(), secretKey);
        encryptionResponseDto.setRequestHMAC(Base64.encodeBase64URLSafeString(byteArr));
        return encryptionResponseDto;
    }

    /*private RestTemplate createTemplate() throws KeyManagementException, NoSuchAlgorithmException {
        turnOffSslChecking();
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
            public Map<String, String> authParams = new HashMap<>();

            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                    throws IOException {
                String authToken = generateAuthToken(authParams);
                if (authToken != null && !authToken.isEmpty()) {
                    request.getHeaders().set("Cookie", "Authorization=" + authToken);
                }
                return execution.execute(request, body);
            }

            public void setAuthParams() {
                authParams.put("appId", env.getProperty("ida.auth.appid"));
                authParams.put("clientId", env.getProperty("ida.auth.clientid"));
                authParams.put("secretKey", env.getProperty("ida.auth.secretkey"));
            }
        };
        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }

    private static void turnOffSslChecking() throws KeyManagementException, java.security.NoSuchAlgorithmException {
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
    private PublicKey getPublicKey(String data, boolean isInternal)
            throws KeyManagementException, RestClientException, NoSuchAlgorithmException, CertificateException {
        restTemplate = createTemplate();
        CryptomanagerRequestDto request = new CryptomanagerRequestDto();
        request.setApplicationId(APPLICATIONID);
        request.setData(Base64.encodeBase64URLSafeString(data.getBytes(StandardCharsets.UTF_8)));
        request.setReferenceId(REFERENCEID);
        String utcTime = getUTCCurrentDateTimeISOString();
        request.setTimeStamp(utcTime);
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("appId", APPLICATIONID);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(env.getProperty("ida.cert.url"))
                .queryParam("timeStamp", getUTCCurrentDateTimeISOString())
                .queryParam("referenceId", REFERENCEID);
        ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
        String certificate = (String) ((Map<String, Object>) response.getBody().get("response")).get("certificate");

        certificate = trimBeginEnd(certificate);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(java.util.Base64.getDecoder().decode(certificate)));
        return x509Certificate.getPublicKey();
    }

    private String generateAuthToken(Map<String, String> authParams) {
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("clientId", authParams.get("clientId"));
        requestBody.put("secretKey", authParams.get("secretKey"));
        requestBody.put("appId", authParams.get("appId"));
        RequestWrapper<ObjectNode> request = new RequestWrapper<>();
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(requestBody);
        ClientResponse response = WebClient
                .create(env.getProperty("ida.authmanager.url"))
                .post().syncBody(request).exchange().block();
        List<ResponseCookie> list = response.cookies().get("Authorization");
        if (list != null && !list.isEmpty()) {
            ResponseCookie responseCookie = list.get(0);
            return responseCookie.getValue();
        }
        return "";
    }*/

    private PublicKey getPublicKey(String authToken) throws CertificateException {
        String certificate = trimBeginEnd(getCertificate(authToken));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(java.util.Base64.getDecoder().decode(certificate)));
        return x509Certificate.getPublicKey();
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

    private String getCertificate(String authToken) {
        OkHttpClient client = new OkHttpClient();
        try {
            Request idarequest = new Request.Builder()
                    .header("cookie", "Authorization="+authToken)
                    .url(env.getProperty("mosip.ida.cert.url"))
                    .get()
                    .build();

            Response idaResponse = new OkHttpClient().newCall(idarequest).execute();
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
