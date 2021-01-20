package io.mosip.mds.entitiy;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.util.SecurityUtil;

@Component
public class DiscoverHelper {

	@Autowired
	SecurityUtil securityUtil;

	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public String getRenderContent(DiscoverResponse response)
	{
		//TODO modify this method for proper response
		String renderContent = "<p><u>Discover Info</u></p>";
		if(!ObjectUtils.isEmpty(response)) {
			renderContent += "<b>Id: </b>" + response.deviceId + "/" + response.deviceSubId[0] + "<br/>";
			if(!ObjectUtils.isEmpty(response.digitalIdDecoded)) {
				renderContent += "<b>Provider: </b>" + response.digitalIdDecoded.deviceProvider + "<br/>";
				renderContent += "<b>Type: </b>" + response.digitalIdDecoded.type + "/" + response.digitalIdDecoded.deviceSubType + "<br/>";
			}
			renderContent += "<b>Purpose: </b>" + response.purpose + "<br/>";
			renderContent += "<b>Spec: </b>" + response.specVersion[0] + "<br/>";
		}
		return renderContent;
	}

	public DiscoverResponse[] decode(String discoverInfo) {
		DiscoverResponse[] response = new DiscoverResponse[1];
		try {
			response = (DiscoverResponse[]) (mapper.readValue(discoverInfo.getBytes(), DiscoverResponse[].class));
			for(DiscoverResponse resp:response)
			{
				try {
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(resp.digitalId), DigitalId.class));
					//TODO always unsigned in discover
					//					if(resp.deviceStatus.equalsIgnoreCase("Not Registered"))
					//						resp.digitalIdDecoded = (DigitalId) (mapper.readValue(resp.digitalId.getBytes(), DigitalId.class));
					//					else
					//						resp.digitalIdDecoded = (DigitalId) (mapper.readValue(SecurityUtil.getPayload(resp.digitalId),	DigitalId.class));
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

	public DiscoverResponse decodeDiscoverInfo(String discoverInfo) {

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		DiscoverResponse resp;
		try {
			resp = (DiscoverResponse) (mapper.readValue(discoverInfo.getBytes(), DiscoverResponse.class));

			try {
				if(resp.deviceStatus.equalsIgnoreCase("Not Registered"))
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(resp.digitalId), DigitalId.class));
				else
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(securityUtil.getPayload(resp.digitalId),	DigitalId.class));
			}
			catch(Exception dex)
			{
				resp.analysisError = "Error interpreting digital id: " + dex.getMessage();		
			}
		}
		catch(Exception ex)
		{
			resp = new DiscoverResponse();
			resp.analysisError = "Error parsing discover info: " + ex.getMessage();
		}
		return resp;
	}
}