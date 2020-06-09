package io.mosip.mds.entitiy;

import java.util.Base64;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.DigitalId;

public class DiscoverHelper {
    public static String Render(DiscoverResponse response)
	{
		//TODO modify this method for proper response
		String renderContent = "<p><u>Discover Info</u></p>";
		renderContent += "<b>Id: </b>" + response.deviceId + "/" + response.deviceSubId[0] + "<br/>";
		renderContent += "<b>Provider: </b>" + response.digitalIdDecoded.deviceProvider + "<br/>";
		renderContent += "<b>Type: </b>" + response.digitalIdDecoded.type + "/" + response.digitalIdDecoded.deviceSubType + "<br/>";
		renderContent += "<b>Purpose: </b>" + response.purpose + "<br/>";
		renderContent += "<b>Spec: </b>" + response.specVersion[0] + "<br/>";
		return renderContent;
    }
    
    public static DiscoverResponse[] Decode(String discoverInfo) {
		DiscoverResponse[] response = new DiscoverResponse[1];

		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			response = (DiscoverResponse[]) (mapper.readValue(discoverInfo.getBytes(), DiscoverResponse[].class));
			for(DiscoverResponse resp:response)
			{
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
		}
		catch(Exception ex)
		{
			response[0] = new DiscoverResponse();
			response[0].analysisError = "Error parsing discover info: " + ex.getMessage();
		}
		return response;
	}

}