package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import java.util.Map;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.dto.ValidateHistoryDTO;
import com.mosip.io.pmp.model.ValidateHistoryRequest;
import com.mosip.io.pmp.model.ValidateHistory_DigitalId;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ValidateHistory extends Util{
	
public static String validateDeviceHistory(Map<String,String> prop,String deviceCode) {
	    String errorCode=null;
		ValidateHistoryDTO deviceHistorycDTO = new ValidateHistoryDTO();
		JSONObject jsonData = readJsonData("validateHistory.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			deviceHistorycDTO = mapper.readValue(jsonData.toJSONString(), ValidateHistoryDTO.class);
			deviceHistorycDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			deviceHistorycDTO.setRequest(createRequest(prop,deviceCode));
			requestInJsonForm = mapper.writeValueAsString(deviceHistorycDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.VALIDATE_DEVICE_HISTORY;
		RestAssured.baseURI = System.getProperty("baseUrl");
    Response api_response =
            given()
                    .cookie("Authorization", cookies)
                    .contentType("application/json")
                    .body(deviceHistorycDTO)
                    .post(url);
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			String status = (String) ctx.read("$.response.status");
			auditLog.info("Status :" + status );
		} else {
			String errorcode = (String) ctx.read("$.errors.errorCode");
			//auditLog.info(errorcode);
			errorCode=errorcode;
		}
		return errorCode;
	
}

private static ValidateHistoryRequest createRequest(Map<String,String> prop,String deviceCode) {
		ValidateHistoryRequest request = new ValidateHistoryRequest();
		//request.setDeviceCode(prop.get("deviceCode"));
		request.setDeviceCode(deviceCode);
		request.setDeviceServiceVersion(prop.get("serviceVersion"));
		request.setDigitalId(createDititalId(prop));
		request.setPurpose(prop.get("purpose"));
		request.setTimeStamp(getCurrentDateAndTimeForAPI());
		return request;
}

private static ValidateHistory_DigitalId createDititalId(Map<String,String> prop) {
		ValidateHistory_DigitalId digitalId = new ValidateHistory_DigitalId();
		digitalId.setDateTime(getCurrentDateAndTimeForAPI());
		digitalId.setDeviceSubType(prop.get("deviceSubType"));
		digitalId.setDp(prop.get("deviceProvider"));
		digitalId.setDpId(prop.get("deviceProviderId"));
		digitalId.setMake(prop.get("make"));
		digitalId.setModel(prop.get("model"));
		digitalId.setSerialNo(prop.get("serialNo"));
		digitalId.setType(prop.get("type"));
		return digitalId;
}
	
}
