package io.mosip.mds.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.mds.authentication.dto.AuthRequestDTO;
import io.mosip.mds.authentication.dto.AuthTypeDTO;
import io.mosip.mds.authentication.dto.BioIdentityInfoDTO;
import io.mosip.mds.authentication.dto.EncryptionRequestDto;
import io.mosip.mds.authentication.dto.EncryptionResponseDto;
import io.mosip.mds.authentication.dto.RequestDTO;
import io.mosip.mds.dto.ValidateResponseRequestDto;

@Component
public class BioAuthRequestUtil {

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
    
    private ObjectMapper mapper = new ObjectMapper();
  
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

        authRequestDTO.setDomainUri("default");
        authRequestDTO.setEnv("Developer");
        String authToken = getAuthToken();
        
        X509Certificate certificate = getCertificateFull(authToken);
        String thumbprint = CryptoUtil.encodeBase64(getCertificateThumbprint(certificate));
        
        RequestDTO requestDTO = new RequestDTO();
        
        List<BioIdentityInfoDTO> biometrics=new ArrayList<BioIdentityInfoDTO>();
        BioIdentityInfoDTO bioIdentityInfoDTO=new BioIdentityInfoDTO();
        bioIdentityInfoDTO.setThumbprint(thumbprint);
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
            encryptionResponseDto = kernelEncrypt(encryptionRequestDto, authToken,certificate);
        } catch (Exception e) {
            logger.error("Error during encrypting auth request", e);
        }
        // Set request block
        authRequestDTO.setThumbprint(thumbprint);
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

    private byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
        return DigestUtils.sha256(cert.getEncoded());
    }

    private String doAuthRequest(String authToken, Map<String, Object> authRequestMap) {
        try {
            String reqBodyJson = mapper.writeValueAsString(authRequestMap);
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, reqBodyJson);
            Request request = new Request.Builder()
                    //curl -X POST "https://dev.mosip.net/v1/keymanager/sign" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"data\": \"string\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\"}"
            		.header("signature", getJWTSignedData(CryptoUtil.encodeBase64(reqBodyJson.getBytes("UTF-8"))
                    		, authToken))
            		.header("Authorization", "myconsenttoken")
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

    private EncryptionResponseDto kernelEncrypt(EncryptionRequestDto encryptionRequestDto, String authToken, X509Certificate certificate)
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
    
    private X509Certificate getCertificateFull(String authToken) throws CertificateException {
        String certificate = trimBeginEnd(getCertificate(authToken));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(java.util.Base64.getDecoder().decode(certificate)));
        return x509Certificate;
    }

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

    private String getCertificate(String authToken) {
        OkHttpClient client = new OkHttpClient();
        try {
            Request idarequest = new Request.Builder()
                    .header("cookie", "Authorization="+authToken)
                    .url(env.getProperty("mosip.ida.cert.url"))
                    .get()
                    .build();

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
