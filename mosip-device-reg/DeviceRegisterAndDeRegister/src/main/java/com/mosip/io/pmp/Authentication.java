package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import org.json.simple.JSONObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.util.DeviceRegisterException;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class Authentication extends Util{
	
	
	public void login(String appId,String userName,String passWord) {
		JSONObject request = loginRequestBuilder(appId,userName,passWord);
	    
	    String url = EndPoint.USER_AUTHENTICATE;
	    RestAssured.baseURI = System.getProperty("baseUrl");
	    Response api_response = given()
	            .contentType(ContentType.JSON).body(request).post(url);
	    String response=api_response.getBody().jsonPath().prettify();
	    
	    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
	    if(ctx.read("$.response") == null) {
	    	auditLog.info("Request :"+ request);
	    	throw new DeviceRegisterException("Invalid Credentials");
	    }
	    
	    cookies=api_response.getCookie("Authorization");
	    logInfo(request, System.getProperty("baseUrl")+url, response);
	}

	
	@SuppressWarnings("unchecked")
	private JSONObject loginRequestBuilder(String appId,String userName,String passWord) {
		JSONObject request = new JSONObject();
	    request.put("id", "mosip.authentication.useridPwd");
	    request.put("version", "1.0");
	    request.put("requesttime", getCurrentDateAndTimeForAPI());
	    request.put("metadata", "{}");
	    
	    JSONObject api_input=new JSONObject();
	    api_input.put("appId", commonDataProp.get(appId));
	    api_input.put("password", commonDataProp.get(passWord));
	    api_input.put("userName", commonDataProp.get(userName));
	    request.put("request", api_input);
		return request;
	}
	
	private void logInfo(JSONObject request, String url, String response) {
		auditLog.info("      ");
		auditLog.info("Endpoint :"+url);
	    auditLog.info("Request  :"+request.toJSONString());
	    auditLog.info("Response  :"+response);
	    auditLog.info("Authorization  :"+cookies);
	}
}
