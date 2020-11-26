package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.dto.DeviceDetailDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class SaveDeviceDetail extends Util{
	 static String id=null;
	public static String saveDeviceDetail(String deviceProviderId,Map<String,String> prop) {
		String sqlQuery="Select id FROM regdevice.device_detail where dprovider_id="+"'"+prop.get("deviceProviderId")+"'" +"and make="+"'"+prop.get("make")+"'" +"and model="+"'"+prop.get("model")+"'";
		DataBaseAccess db= new DataBaseAccess();
		List<String> deviceId = db.getDbData(sqlQuery,"regdevice");
		if(!(deviceId.isEmpty()) && deviceId.size()>0) {
			auditLog.info("DeviceDetail  already present in DB");
			id=deviceId.get(0);
		}else {
		
		DeviceDetailDTO deviceDetailDTO = new DeviceDetailDTO();
		JSONObject jsonData =readJsonData("deviceDetail.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			deviceDetailDTO = mapper.readValue(jsonData.toJSONString(), DeviceDetailDTO.class);
			deviceDetailDTO.getRequest().setDeviceProviderId(deviceProviderId);
			deviceDetailDTO.getRequest().setDeviceSubTypeCode(prop.get("deviceSubType"));
			deviceDetailDTO.getRequest().setDeviceTypeCode(prop.get("type"));
			deviceDetailDTO.getRequest().setId(UUID.randomUUID().toString());
			deviceDetailDTO.getRequest().setIsItForRegistrationDevice(Boolean.TRUE);
			deviceDetailDTO.getRequest().setMake(prop.get("make"));
			deviceDetailDTO.getRequest().setModel(prop.get("model"));
			deviceDetailDTO.getRequest().setPartnerOrganizationName(prop.get("deviceProvider"));
			deviceDetailDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(deviceDetailDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.SAVE_DEVICE_DETAIL;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(deviceDetailDTO)
                        .post(url);
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			id = (String) ctx.read("$.response.id");
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.warning(errorMessage);
		}
		}
		
		return id;
	}
}
