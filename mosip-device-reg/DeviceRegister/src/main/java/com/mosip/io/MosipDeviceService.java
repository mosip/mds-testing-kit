package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pojo.Metadata;
import com.mosip.io.pojo.MosipDeviceServiceDTO;
import com.mosip.io.pojo.MosipDeviceServiceRequest;
import com.mosip.io.util.ServiceUrl;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class MosipDeviceService extends Util {
	
	public List<String> registerMDS(String deviceProviderId) {
		MosipDeviceServiceDTO dto=createDTO(deviceProviderId);
		String url = ServiceUrl.MOSIP_DEVICE_SERVICE;
        RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(dto)
                        .post(url);
        
        auditLog.info("Endpoint :"+url);
	    auditLog.info("Request  :"+dto);
	    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
		
	    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
	    List<String> providerList= new ArrayList<>();
	    if(ctx.read("$.response") != null) {
	    	String mosipDeviceServiceId = (String)ctx.read("$.response.id");
	    	String make = (String)ctx.read("$.response.make");
	    	String model = (String)ctx.read("$.response.model");
	    	providerList.add(mosipDeviceServiceId);
	    	providerList.add(make);
	    	providerList.add(model);
	    }else {
	    	String errorMessage =(String)ctx.read("$.errors[0].message");
	    	throw new RuntimeException(errorMessage);
	    }
		return providerList;
	}
	
	public boolean dbCheck(String type,String deviceProderId) {
		if(type==null || type.isEmpty())
			throw new RuntimeException("Please provide type value from Vm argument");
		boolean isPresent=false;
		
		switch(type) {
		case "Face":
			isPresent = isProviderIdPresentInMDS(deviceProderId);
			break;
		case "Iris":
			isPresent = isProviderIdPresentInMDS(deviceProderId);
			break;
		case "Finger":
			isPresent = isProviderIdPresentInMDS(deviceProderId);
			break;
		case "Auth":
			isPresent = isProviderIdPresentInMDS(deviceProderId);
				break;
			default:
				throw new RuntimeException("Invalid type : "+type+" is found!");
		}
		return isPresent;
	}

	private boolean isProviderIdPresentInMDS(String deviceProderId) {
		boolean isPresent=false;
		DataBaseAccess db= new DataBaseAccess();
		String device_providerQuery = "Select * from master.mosip_device_service where dprovider_id="+"'"+deviceProderId+"'";
		String device_providerHistoryQuery = "Select * from master.mosip_device_service_h where dprovider_id="+"'"+deviceProderId+"'";
		if (db.getDbData(device_providerQuery, "masterdata").size()>0 && db.getDbData(device_providerHistoryQuery, "masterdata").size()>0)
			isPresent = true;
		return isPresent;
	}
	
	private MosipDeviceServiceDTO createDTO(String deviceProviderId) {
		MosipDeviceServiceDTO dto = new MosipDeviceServiceDTO("string", new Metadata(), createRequestBuilder(deviceProviderId),
				Util.getCurrentDateAndTimeForAPI(), "string");
		return dto;
	}

	private MosipDeviceServiceRequest createRequestBuilder(String deviceProviderId) {
		if(prop==null || prop.isEmpty())
			throw new RuntimeException("prop value cannot be Null Or Empty");
		MosipDeviceServiceRequest request= new MosipDeviceServiceRequest();
		request.setDeviceProviderId(deviceProviderId);
		request.setIsActive(Boolean.TRUE);
		request.setMake(prop.get("make"));
		request.setModel(prop.get("model"));
		request.setRegDeviceSubCode(prop.get("deviceSubType"));
		request.setRegDeviceTypeCode(prop.get("type"));
		request.setSwBinaryHash(0);
		request.setSwCreateDateTime(Util.getCurrentDateAndTimeForAPI());
		request.setSwExpiryDateTime("2020-12-31T07:00:13.375Z");
		request.setSwVersion(prop.get("serviceVersion"));
		return request;
	}
}
