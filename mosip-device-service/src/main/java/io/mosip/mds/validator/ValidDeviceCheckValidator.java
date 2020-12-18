package io.mosip.mds.validator;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.mds.dto.DeviceValidatorDigitalIdDto;
import io.mosip.mds.dto.DeviceValidatorDto;
import io.mosip.mds.dto.DeviceValidatorRequestDto;
import io.mosip.mds.dto.DeviceValidatorResponseDto;
import io.mosip.mds.dto.Validation;
import io.restassured.http.Cookie;

@Component
public class ValidDeviceCheckValidator {

	private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	@Autowired
	private Environment env;

	Validation validation = new Validation();

	@Autowired
	CommonValidator commonValidator;

	public static void main(String[] args) {
		ValidDeviceCheckValidator v=new ValidDeviceCheckValidator();
		DeviceValidatorDto deviceValidatorDto=new DeviceValidatorDto();
		DeviceValidatorRequestDto devicevalidatorrequestdto = new DeviceValidatorRequestDto();
		DeviceValidatorDigitalIdDto digitalId=new DeviceValidatorDigitalIdDto();
		digitalId.setDateTime(getCurrentDateAndTimeForAPI());
		digitalId.setDeviceSubType("Slab");
		digitalId.setDp("");
		digitalId.setDpId("");
		digitalId.setMake("");
		digitalId.setModel("");
		digitalId.setSerialNo("");
		digitalId.setType("Finger");
		devicevalidatorrequestdto.setDeviceCode("123");
		devicevalidatorrequestdto.setDeviceServiceVersion("1.0");
		devicevalidatorrequestdto.setPurpose("Registration");

		devicevalidatorrequestdto.setDigitalId(digitalId);
		devicevalidatorrequestdto.setTimeStamp(getCurrentDateAndTimeForAPI());
		deviceValidatorDto.setRequest(devicevalidatorrequestdto );;
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());
		try {
			System.out.println(v.doValidateDevice(deviceValidatorDto, null));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getCurrentDateAndTimeForAPI() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC);
		String currentTime = time.format(dateFormat);
		return currentTime;

	}

	public List<Validation> doValidateDevice( DeviceValidatorDto deviceValidatorDto, List<Validation> validations) throws IOException {
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());

		Cookie.Builder builder = new Cookie.Builder("Authorization", getAuthToken());
		io.restassured.response.Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(deviceValidatorDto)
				.contentType("application/json").log().all().when().post(env.getProperty("ida.validation.url")).then()
				.log().all().extract().response();

		//DeviceValidatorResponseDto deviceValidatorResponseDto=(DeviceValidatorResponseDto) postResponse.getBody() ;

		try{
			validation = commonValidator.setFieldExpected("Registered Device","Succes Response",postResponse.getBody().asString());

			Gson gson = new Gson();

			DeviceValidatorResponseDto deviceValidatorResponseDto= gson.fromJson(postResponse.getBody().asString() , DeviceValidatorResponseDto.class);


			if(deviceValidatorResponseDto.getErrors().size()>0) {
				commonValidator.setFoundMessageStatus(validation,postResponse.getBody().asString(),"Device Registration check failed",CommonConstant.FAILED);
				validations.add(validation);
			}else if(deviceValidatorResponseDto.getResponse().size()>0) {
				validations.add(validation);
			}
		}
		catch(Exception e) {
			commonValidator.setFoundMessageStatus(validation,postResponse.getBody().asString(),"Exception Device Registration check filed",CommonConstant.FAILED);
			validations.add(validation);
		}
		return validations;
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
}
