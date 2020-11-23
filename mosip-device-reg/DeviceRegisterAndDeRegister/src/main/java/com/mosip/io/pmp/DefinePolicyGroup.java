package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.dto.DefinePolicyGroupDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class DefinePolicyGroup extends Util{
	
	public String definePolicyGroup() {
		String policyGroupName=null;
		String requestInJsonForm = "";
		DefinePolicyGroupDTO definePolicyGroupDTO = new DefinePolicyGroupDTO();
		JSONObject jsonData =readJsonData("definePolicyGroup.json");
		try {
			ObjectMapper mapper = new ObjectMapper();
			definePolicyGroupDTO = mapper.readValue(jsonData.toJSONString(), DefinePolicyGroupDTO.class);
			definePolicyGroupDTO.getRequest().setDesc("Device Provider Desc "+generateRandomString());
			definePolicyGroupDTO.getRequest().setName("Device Provider Name "+generateRandomString());
			definePolicyGroupDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(definePolicyGroupDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.DEFINE_POLICY_GROUP;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(definePolicyGroupDTO)
                        .post(url);
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			 policyGroupName = (String) ctx.read("$.response.name");
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.warning(errorMessage);
		}
		return policyGroupName;
	}

}
