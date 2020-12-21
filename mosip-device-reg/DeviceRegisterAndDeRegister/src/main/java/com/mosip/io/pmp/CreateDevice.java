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
import com.mosip.io.dto.CreateDeviceDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CreateDevice extends Util{
	
	public static String createDevice(String deviceSpecId,String langcode,Map<String,String> prop) {
		String deviceId=null;
		DataBaseAccess db= new DataBaseAccess();
		String isSerialNoPresent = "Select id from master.device_master where serial_num="+"'"+prop.get("serialNo")+"'"+" and is_active='true' and lang_code="+"'"+langcode+"'";
		List<String> list = db.getDbData(isSerialNoPresent, "masterdata");
		if (list.size()>0) {
			auditLog.info("Device already exist with serailNo : "+prop.get("serialNo"));
			deviceId=list.get(0);
			auditLog.info("Device created with deviceId: "+deviceId);
			return deviceId;
		}
		
		CreateDeviceDTO createDeviceDTO = new CreateDeviceDTO();
		JSONObject jsonData = Util.readJsonData("createDevice.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			createDeviceDTO = mapper.readValue(jsonData.toJSONString(), CreateDeviceDTO.class);
			createDeviceDTO.getRequest().setDeviceSpecId(deviceSpecId);
			createDeviceDTO.getRequest().setId(UUID.randomUUID().toString());
			createDeviceDTO.getRequest().setLangCode(langcode);
			createDeviceDTO.getRequest().setMacAddress(commonDataProp.get("macAddress"));
			createDeviceDTO.getRequest().setName(prop.get("name"));
			createDeviceDTO.getRequest().setRegCenterId(prop.get("regCenterId"));
			createDeviceDTO.getRequest().setSerialNum(prop.get("serialNo"));
			createDeviceDTO.getRequest().setValidityDateTime(commonDataProp.get("validityDateTime"));
			createDeviceDTO.getRequest().setZoneCode(prop.get("zoneCode"));
			createDeviceDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(createDeviceDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.CREATE_DEVICE;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(createDeviceDTO)
                        .post(url);
        
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			deviceId = (String) ctx.read("$.response.id");
			auditLog.info("Device Created In " + langcode + " With DeviceId: " + deviceId);
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.warning(errorMessage);
		}
      return deviceId;
	}
	
}
