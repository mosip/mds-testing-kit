package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.dto.ApproveSecureBiometricInfoDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ApproveSecureBiometricInfo extends Util{

	public static String approveSecureBiometricInfo(String secureBiometricId) {
		String response=null;
		ApproveSecureBiometricInfoDTO approveSecureBiometricInfoDTO = new ApproveSecureBiometricInfoDTO();
		JSONObject jsonData = readJsonData("approveDeviceDetail.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			approveSecureBiometricInfoDTO = mapper.readValue(jsonData.toJSONString(), ApproveSecureBiometricInfoDTO.class);
			approveSecureBiometricInfoDTO.getRequest().setApprovalStatus("Activate");
			approveSecureBiometricInfoDTO.getRequest().setId(secureBiometricId);
			approveSecureBiometricInfoDTO.getRequest().setIsItForRegistrationDevice(Boolean.TRUE);
			approveSecureBiometricInfoDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(approveSecureBiometricInfoDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.APPROVE_SECURE_BIOMETRIC_INFO;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(approveSecureBiometricInfoDTO)
                        .patch(url);
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			 response = (String) ctx.read("$.response");
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.warning(errorMessage);
		}
		return response;
	}
}
