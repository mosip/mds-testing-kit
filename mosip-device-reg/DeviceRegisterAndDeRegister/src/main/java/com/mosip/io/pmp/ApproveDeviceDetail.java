package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.dto.ApproveDeviceDetailDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ApproveDeviceDetail extends Util{
	
	public static String approveDeviceDetail(String deviceId) {
		String response=null;
		ApproveDeviceDetailDTO approveDeviceDetailDTO = new ApproveDeviceDetailDTO();
		JSONObject jsonData = readJsonData("approveDeviceDetail.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			approveDeviceDetailDTO = mapper.readValue(jsonData.toJSONString(), ApproveDeviceDetailDTO.class);
			approveDeviceDetailDTO.getRequest().setApprovalStatus("Activate");
			approveDeviceDetailDTO.getRequest().setId(deviceId);
			approveDeviceDetailDTO.getRequest().setIsItForRegistrationDevice(Boolean.TRUE);
			approveDeviceDetailDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(approveDeviceDetailDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.APPROVE_DEVICE_DETAIL;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(approveDeviceDetailDTO)
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
