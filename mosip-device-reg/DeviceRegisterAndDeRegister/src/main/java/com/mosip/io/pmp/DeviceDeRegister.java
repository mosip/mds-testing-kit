package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import java.util.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.dto.DeviceDeRegisterRequestDTO;
import com.mosip.io.pmp.model.Metadata;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class DeviceDeRegister extends Util {
	Boolean isDeviceDeRegisterSuccess=false;
	public boolean deviceDeRegister(String deviceCode) {
		DeviceDeRegisterRequestDTO dto = new DeviceDeRegisterRequestDTO();
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			dto.setId("io.mosip.devicederegister");
			dto.setVersion("1.0");
			dto.setMetadata(new Metadata().toString());
			dto.setRequesttime(getCurrentDateAndTimeForAPI());
			dto.getRequest().setDevice(createDeviceDetail(deviceCode));
			dto.getRequest().setIsItForRegistrationDevice(Boolean.TRUE);
			requestInJsonForm = mapper.writeValueAsString(dto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.DEVICE_DE_REGISTERED;
		RestAssured.baseURI = System.getProperty("baseUrl");
		Response api_response = given().cookie("Authorization", cookies).contentType("application/json").body(dto)
				.post(url);

		logApiInfo(requestInJsonForm, System.getProperty("baseUrl") + url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			isDeviceDeRegisterSuccess=true;
		} else {
			// String errorMessage = (String) ctx.read("$.errors[0].message");
			// auditLog.warning(errorMessage);
		}

		return isDeviceDeRegisterSuccess;
	}

	private String createDeviceDetail(String deviceCode) {
		String encodeJsonValue = "";
		String env = System.getProperty("env.user");
		switch (env.toLowerCase()) {
		case "dev":
			env = "Developer";
			break;
		case "qa":
			env = "Staging";
			break;
		}
		String jsonValue = buildJson(deviceCode, env);
		encodeJsonValue = Base64.getEncoder().encodeToString(jsonValue.getBytes());
		return encodeJsonValue;
	}

	private String buildJson(String deviceCode, String env) {
		String json = "{" + "\"deviceCode\"" + ":" + "\"" + deviceCode + "\"" + "," + "\"" + "env\"" + ":" + "\"" + env
				+ "\"" + "}";
		return json;
	}
}

