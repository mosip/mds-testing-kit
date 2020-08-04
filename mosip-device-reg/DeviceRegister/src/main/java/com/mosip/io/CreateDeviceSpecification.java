package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.List;
import java.util.Random;

import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pojo.CreateDeviceSpecDTO;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CreateDeviceSpecification extends Util {
	
	public String createDeviceSpec(String langCode,List<String> mosipDeviceServiceProviderList,String deviceId) {
		DataBaseAccess db= new DataBaseAccess();
		CreateDeviceSpecDTO deviceSpecDTO =new CreateDeviceSpecDTO();
		JSONObject jsonData =readJsonData("/Request/createDeviceSpe.json");
		auditLog.info("Acutual Reqeust: "+jsonData.toJSONString());
		try {
			ObjectMapper mapper = new ObjectMapper();
			deviceSpecDTO = mapper.readValue(jsonData.toJSONString(), CreateDeviceSpecDTO.class);
			String deviceSpecId=deviceSpecDTO.getRequest().getId();
			if(langCode.equalsIgnoreCase("ara")) {
				deviceSpecDTO.getRequest().setId(String.valueOf(deviceId));
			}else {
				if(db.validateDataInDb("select * from master.device_spec where id="+"'"+deviceSpecId+"'" +"and lang_code="+"'"+langCode+"'", "masterdata")) {
					Random random = new Random();
					int newDeviceSpecId = Integer.valueOf(deviceSpecId).intValue()+10+random.nextInt(90);
					deviceSpecDTO.getRequest().setId(String.valueOf(newDeviceSpecId));
				}
			}
			deviceSpecDTO.getRequest().setLangCode(langCode);
			deviceSpecDTO.getRequest().setBrand(commonDataProp.get("name"));
			deviceSpecDTO.getRequest().setDescription(commonDataProp.get("description"));
			deviceSpecDTO.getRequest().setDeviceTypeCode(commonDataProp.get("deviceTypeCode"));
			if(mosipDeviceServiceProviderList.size()>0) {
				deviceSpecDTO.getRequest().setBrand(mosipDeviceServiceProviderList.get(1));
				deviceSpecDTO.getRequest().setModel(mosipDeviceServiceProviderList.get(2));	
			}
			deviceSpecDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
			String value=mapper.writeValueAsString(deviceSpecDTO);
			auditLog.info("Updated Reqeust: "+value);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		
		}
		String url = "/v1/masterdata/devicespecifications";
        RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(deviceSpecDTO)
                        .post(url);
        
        auditLog.info("Endpoint :"+url);
	    auditLog.info("Request  :"+deviceSpecDTO);
	    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
	    
	    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
	    String deviceSpecId =null;
	    if(ctx.read("$.response")!=null) {
	    	deviceSpecId = (String)ctx.read("$.response.id");
	        auditLog.info("Device Specification Created in "+langCode+" with Id: "+deviceSpecId);
	    }else {
	    	String errorMessage =(String)ctx.read("$.errors[0].message");
	    	throw new RuntimeException(errorMessage);
	    }
        
		return deviceSpecId;
	}
	
}
