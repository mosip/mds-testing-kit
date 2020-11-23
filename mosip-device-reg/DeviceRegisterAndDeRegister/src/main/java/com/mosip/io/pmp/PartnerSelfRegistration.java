package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.dto.PartnerSelfRegistrationDTO;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class PartnerSelfRegistration extends Util{
	
	public static String partnerSelfRegistration(Map<String,String> prop) {
		String partnerId=null;
		String partnerIdExist=prop.get("deviceProviderId");
		String sqlQuery="Select * FROM pms.partner where id="+"'"+partnerIdExist+"'";
		DataBaseAccess db= new DataBaseAccess();
		if(db.validateDataInDb(sqlQuery,"pms")) {
			auditLog.info("PartnerId "+partnerIdExist+" already present in DB");
			return partnerIdExist;
		}
		PartnerSelfRegistrationDTO partnerSelfRegistrationDTO= new PartnerSelfRegistrationDTO();
		JSONObject jsonData =readJsonData("partnerSelfRegistration.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			partnerSelfRegistrationDTO = mapper.readValue(jsonData.toJSONString(), PartnerSelfRegistrationDTO.class);
			partnerSelfRegistrationDTO.getRequest().setAddress(generateRandomString());
			partnerSelfRegistrationDTO.getRequest().setContactNumber(RandomStringUtils.randomNumeric(10));
			partnerSelfRegistrationDTO.getRequest().setEmailId(generateRandomString()+"@gmail.com");
			partnerSelfRegistrationDTO.getRequest().setOrganizationName(prop.get("deviceProvider"));
			partnerSelfRegistrationDTO.getRequest().setPartnerId(prop.get("deviceProviderId"));
			partnerSelfRegistrationDTO.getRequest().setPolicyGroup(prop.get("policyGroup"));
			partnerSelfRegistrationDTO.getRequest().setPartnerType(prop.get("partnerType"));
			partnerSelfRegistrationDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(partnerSelfRegistrationDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = EndPoint.PARTNER_SELF_REGISTRATION;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
                        .contentType("application/json")
                        .body(partnerSelfRegistrationDTO)
                        .post(url);
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			partnerId = (String) ctx.read("$.response.partnerId");
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.warning(errorMessage);
		}
		return partnerId;
	}

}
