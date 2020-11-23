package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.dto.SecureBiometricInfoDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class SecureBiometricInfo extends Util{
	
	public static String saveSecureBiometricInfo(String deviceId,Map<String,String> prop) {
		String secureBiometricId=null;
		DataBaseAccess db= new DataBaseAccess();
		String sqlQuery="Select id FROM regdevice.secure_biometric_interface where device_detail_id="+"'"+deviceId+"'";
		List<String> sbi_Id = db.getDbData(sqlQuery, "regdevice");
		if(!sbi_Id.isEmpty() &&sbi_Id.size()>0 ) {
			secureBiometricId=sbi_Id.get(0);
			return secureBiometricId;
		}
		SecureBiometricInfoDTO secureBiometricInfoDTO= new SecureBiometricInfoDTO();
		JSONObject jsonData = readJsonData("secureBiometricInfo.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			secureBiometricInfoDTO = mapper.readValue(jsonData.toJSONString(), SecureBiometricInfoDTO.class);
			secureBiometricInfoDTO.getRequest().setDeviceDetailId(deviceId);
			secureBiometricInfoDTO.getRequest().setIsItForRegistrationDevice(Boolean.TRUE);
			secureBiometricInfoDTO.getRequest().setSwBinaryHash("string");
			secureBiometricInfoDTO.getRequest().setSwCreateDateTime(getCurrentDateAndTimeForAPI());
			secureBiometricInfoDTO.getRequest().setSwExpiryDateTime(commonDataProp.get("validityDateTime"));
			secureBiometricInfoDTO.getRequest().setSwVersion(prop.get("firmware"));
			secureBiometricInfoDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(secureBiometricInfoDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.SAVE_SECURE_BIOMETRIC_INFO;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(secureBiometricInfoDTO)
                        .post(url);
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			secureBiometricId = (String) ctx.read("$.response.id");
		} else {
			String errorMessage = (String) ctx.read("$.errors[0].message");
			auditLog.warning(errorMessage);
		}
		return secureBiometricId;
	}

}
