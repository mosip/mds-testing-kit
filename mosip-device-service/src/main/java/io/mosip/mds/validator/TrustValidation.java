package io.mosip.mds.validator;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.mds.dto.DeviceTrustRequestDto;
import io.mosip.mds.dto.DeviceValidatorDto;
import io.mosip.mds.dto.DeviceValidatorResponseDto;
import io.mosip.mds.dto.Validation;
import io.restassured.http.Cookie;

@Component
public class TrustValidation {

	private static final String DEVICE = "DEVICE";

	private static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";

	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";

	private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	@Autowired
	private Environment env;

	Validation validation = new Validation();

	@Autowired
	CommonValidator commonValidator;

	@Autowired
	ObjectMapper mapper;

	public static String getCurrentDateAndTimeForAPI() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC);
		String currentTime = time.format(dateFormat);
		return currentTime;


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

	public Validation trustRootValidation(String certificateData , Validation validation) throws IOException {
		DeviceValidatorDto deviceValidatorDto = new DeviceValidatorDto();
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());
		DeviceTrustRequestDto trustRequest = new DeviceTrustRequestDto();

		trustRequest.setCertificateData(BEGIN_CERTIFICATE+certificateData+END_CERTIFICATE);
		trustRequest.setPartnerDomain(DEVICE);
		deviceValidatorDto.setRequest(trustRequest);

		Cookie.Builder builder = new Cookie.Builder("Authorization", getAuthToken());
		io.restassured.response.Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(deviceValidatorDto)
				.contentType("application/json").log().all().when().post(env.getProperty("keymanager.verifyCertificateTrust")).then()
				.log().all().extract().response();

		try{
			validation.setFound(postResponse.getBody().asString());
			DeviceValidatorResponseDto deviceValidatorResponseDto= mapper.readValue(postResponse.getBody().asString() , DeviceValidatorResponseDto.class);

			if((deviceValidatorResponseDto.getErrors() != null &&  deviceValidatorResponseDto.getErrors().size()>0) ||
					(deviceValidatorResponseDto.getResponse().getStatus().equals("false"))) {
				commonValidator.setFoundMessageStatus(validation,postResponse.getBody().asString(),"Trust Validation failed",CommonConstant.FAILED);
			}
		}
		catch(Exception e) {
			commonValidator.setFoundMessageStatus(validation,postResponse.getBody().asString(),"Exception in Trust Validation",CommonConstant.FAILED);
		}
		return validation;
	}
}
