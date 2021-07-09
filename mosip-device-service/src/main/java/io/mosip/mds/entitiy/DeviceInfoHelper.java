package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.util.SecurityUtil;

@Component
public class DeviceInfoHelper {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SecurityUtil securityUtil;

	public String getRenderContent(DeviceInfoResponse response)
	{
		//TODO modify this method for proper response
		String renderContent = "<p><u>Device Info</u></p>";
		renderContent += "<b>Id: </b>" + response.deviceId + "/" + response.deviceSubId[0] + "<br/>";
		renderContent += "<b>Provider: </b>" + response.digitalIdDecoded.deviceProvider + "<br/>";
		renderContent += "<b>Type: </b>" + response.digitalIdDecoded.type + "/" + response.digitalIdDecoded.deviceSubType + "<br/>";
		renderContent += "<b>Purpose: </b>" + response.purpose + "<br/>";
		renderContent += "<b>Spec: </b>" + response.specVersion[0] + "<br/>";
		renderContent += "<b>Status: </b>" + response.deviceStatus + "<br/>";
		renderContent += "<b>Callback: </b>" + response.callbackId + "<br/>";
		return renderContent;
	}

	public DeviceInfoResponse[] decode(String deviceInfo) {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		DeviceInfoMinimal[] input = null;
		List<DeviceInfoResponse> response = new ArrayList<DeviceInfoResponse>();		
		//Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");

		try {
			input = (DeviceInfoMinimal[])(mapper.readValue(deviceInfo.getBytes(), DeviceInfoMinimal[].class));
			for(DeviceInfoMinimal respMin:input)
			{
				response.add(decodeDeviceInfo(respMin.deviceInfo));
			}
		} catch (Exception exception) {
			DeviceInfoResponse errorResp = new DeviceInfoResponse();
			errorResp.analysisError = "Error parsing request input" + exception.getMessage();
			response.add(errorResp);
		}
		return response.toArray(new DeviceInfoResponse[response.size()]);
	}

	public DeviceInfoResponse decodeDeviceInfo(String encodeInfo)
	{
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		DeviceInfoResponse resp = new DeviceInfoResponse();	
		try
		{		
			try {
			resp = (DeviceInfoResponse) mapper.readValue(securityUtil.getPayload(encodeInfo), DeviceInfoResponse.class);
			
			}catch (Exception e) {
				
					resp = (DeviceInfoResponse) (mapper.readValue(Base64.getUrlDecoder().decode(encodeInfo), DeviceInfoResponse.class));
				
			}
						
			try {
				if(resp.deviceStatus.equalsIgnoreCase("Not Registered"))
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(resp.digitalId), DigitalId.class));
				else
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(securityUtil.getPayload(resp.digitalId), DigitalId.class));
			}
			catch(Exception dex)
			{
				resp.analysisError = "Error interpreting digital id: " + dex.getMessage();
			}
		}
		catch(Exception rex)
		{
			resp.analysisError = "Error interpreting device info id: " + rex.getMessage();
		}
		return resp;		
	}

}