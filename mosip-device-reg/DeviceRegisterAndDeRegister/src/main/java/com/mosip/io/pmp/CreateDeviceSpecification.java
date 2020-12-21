package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import java.util.Map;
import java.util.Random;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.dto.CreateDeviceSpecDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CreateDeviceSpecification extends Util {
	
	public static String createDeviceSpec(String langcode, Map<String,String> prop,String deviceId) {
		DataBaseAccess db= new DataBaseAccess();
		CreateDeviceSpecDTO deviceSpecDTO =new CreateDeviceSpecDTO();
		JSONObject jsonData =readJsonData("createDeviceSpe.json");
		String requestInJsonForm="";
		try {
			ObjectMapper mapper = new ObjectMapper();
			deviceSpecDTO = mapper.readValue(jsonData.toJSONString(), CreateDeviceSpecDTO.class);
			String deviceSpecId=deviceSpecDTO.getRequest().getId();
			if (langcode.equalsIgnoreCase(commonDataProp.get("secondaryLanguage"))) {
				deviceSpecDTO.getRequest().setId(String.valueOf(deviceId));
			} else if (db.validateDataInDb("select * from master.device_spec where id=" + "'" + deviceSpecId + "'"
					+ " and lang_code=" + "'" + langcode + "'", "masterdata")) {
				Random random = new Random();
				int newDeviceSpecId = Integer.valueOf(deviceSpecId).intValue() + 10 + random.nextInt(300);
				deviceSpecDTO.getRequest().setId(String.valueOf(newDeviceSpecId));
			}
			deviceSpecDTO.getRequest().setLangCode(langcode);
			deviceSpecDTO.getRequest().setBrand(prop.get("name"));
			deviceSpecDTO.getRequest().setDescription(prop.get("description"));
			deviceSpecDTO.getRequest().setDeviceTypeCode(prop.get("deviceTypeCode"));
			deviceSpecDTO.getRequest().setBrand(prop.get("name"));
			deviceSpecDTO.getRequest().setModel(prop.get("model"));
			deviceSpecDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(deviceSpecDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();

		}
		String url = EndPoint.CREATE_DEVICE_SPECIFICATIONS;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(deviceSpecDTO)
                        .post(url);
	    logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
	    
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		String deviceSpecId = null;
		if (ctx.read("$.response") != null) {
			deviceSpecId = (String) ctx.read("$.response.id");
			auditLog.info("Device Specification Created In " + langcode + " With Id: " + deviceSpecId);
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.warning(errorMessage);
		}

		return deviceSpecId;
	}
	
}
