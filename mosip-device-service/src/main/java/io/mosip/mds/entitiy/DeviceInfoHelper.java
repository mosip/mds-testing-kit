package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DigitalId;

public class DeviceInfoHelper {
    public static String Render(DeviceInfoResponse response)
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
    
    public static DeviceInfoResponse[] Decode(String deviceInfo) {
		DeviceInfoMinimal[] input = null;
		List<DeviceInfoResponse> response = new ArrayList<DeviceInfoResponse>();
		ObjectMapper mapper = new ObjectMapper();
		Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			input = (DeviceInfoMinimal[])(mapper.readValue(deviceInfo.getBytes(), DeviceInfoMinimal[].class));
			for(DeviceInfoMinimal respMin:input)
			{
				DeviceInfoResponse resp = new DeviceInfoResponse();
				try
				{
					Matcher matcher = pattern.matcher(respMin.deviceInfo);
					String afterMatch = null;
					if (matcher.find()) {
						afterMatch = matcher.group(1);
					}			
					String result = new String(
						Base64.getUrlDecoder().decode(new String(Base64.getUrlDecoder().decode(afterMatch)).getBytes()));
					resp = (DeviceInfoResponse) (mapper.readValue(result.getBytes(), DeviceInfoResponse.class));
				
					try {
						if(resp.deviceStatus.equalsIgnoreCase("Not Registered"))
							resp.digitalIdDecoded = (DigitalId) (mapper.readValue(resp.digitalId.getBytes(), DigitalId.class));
						else
						resp.digitalIdDecoded = (DigitalId) (mapper.readValue(
							new String(Base64.getDecoder().decode(resp.digitalId)).getBytes(),
							DigitalId.class));
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
				response.add(resp);
			}
		} catch (Exception exception) {
			DeviceInfoResponse errorResp = new DeviceInfoResponse();
			errorResp.analysisError = "Error parsing request input" + exception.getMessage();
			response.add(errorResp);
		}
		return response.toArray(new DeviceInfoResponse[response.size()]);
	}

}