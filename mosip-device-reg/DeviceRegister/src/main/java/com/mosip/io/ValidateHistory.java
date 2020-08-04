package com.mosip.io;

import static io.restassured.RestAssured.given;

import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.pojo.ValidateHistoryDTO;
import com.mosip.io.pojo.ValidateHistoryRequest;
import com.mosip.io.pojo.ValidateHistory_DigitalId;
import com.mosip.io.util.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ValidateHistory extends Util{
	
public void validateDeviceHistory() {
	ValidateHistoryDTO deviceHistorycDTO =new ValidateHistoryDTO();
	JSONObject jsonData =readJsonData("/Request/validateHistory.json");
	auditLog.info("Acutual Reqeust: "+jsonData.toJSONString());
	try {
		ObjectMapper mapper = new ObjectMapper();
		deviceHistorycDTO = mapper.readValue(jsonData.toJSONString(), ValidateHistoryDTO.class);
		deviceHistorycDTO.setRequest(createRequest());
		String value=mapper.writeValueAsString(deviceHistorycDTO);
		auditLog.info("Update Request :"+ value);
	} catch (JsonProcessingException e) {
		e.printStackTrace();
	}
	String url = "/v1/masterdata/deviceprovidermanagement/validate";
    RestAssured.baseURI = System.getProperty("baseUrl");
    Response api_response =
            given()
                    .cookie("Authorization", Util.cookies)
                    .contentType("application/json")
                    .body(deviceHistorycDTO)
                    .post(url);
    
    auditLog.info("Endpoint :"+url);
    auditLog.info("Request  :"+deviceHistorycDTO);
    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
    
    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
    if(ctx.read("$.response") != null) {
    	String status = (String)ctx.read("$.response.status");
    	String message = (String)ctx.read("$.response.message");
    	auditLog.info("**** Device Validate Success*****");
    	auditLog.info("Status :" + status + "\nMessage :" + message );
    }else {
    	String errorMessage =(String)ctx.read("$.errors[0].message");
    	throw new RuntimeException(errorMessage);
    }
	
}

private ValidateHistoryRequest createRequest() {
	ValidateHistoryRequest request= new ValidateHistoryRequest();
	request.setDeviceCode(prop.get("deviceCode"));
	request.setDeviceServiceVersion(prop.get("serviceVersion"));
	request.setDigitalId(createDititalId());
	request.setPurpose(prop.get("purpose"));
	request.setTimeStamp(getCurrentDateAndTimeForAPI());
	return request;
}

private ValidateHistory_DigitalId createDititalId() {
	ValidateHistory_DigitalId digitalId= new ValidateHistory_DigitalId();
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
