package io.mosip.mds.entitiy;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DigitalId;

public class DeviceInfoHelper {
	
	private static ObjectMapper mapper;
	
	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
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
		//Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");
		
		try {
			input = (DeviceInfoMinimal[])(mapper.readValue(deviceInfo.getBytes(), DeviceInfoMinimal[].class));
			for(DeviceInfoMinimal respMin:input)
			{
				response.add(DecodeDeviceInfo(respMin.deviceInfo));
			}
		} catch (Exception exception) {
			DeviceInfoResponse errorResp = new DeviceInfoResponse();
			errorResp.analysisError = "Error parsing request input" + exception.getMessage();
			response.add(errorResp);
		}
		return response.toArray(new DeviceInfoResponse[response.size()]);
	}

	public static DeviceInfoResponse DecodeDeviceInfo(String encodeInfo)
	{
		DeviceInfoResponse resp = new DeviceInfoResponse();	
		try
		{		
			resp = (DeviceInfoResponse) (mapper.readValue(getPayload(encodeInfo), DeviceInfoResponse.class));
			try {
				if(resp.deviceStatus.equalsIgnoreCase("Not Registered"))
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(resp.digitalId.getBytes(), DigitalId.class));
				else
					resp.digitalIdDecoded = (DigitalId) (mapper.readValue(getPayload(resp.digitalId), DigitalId.class));
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
	
	private static byte[] getPayload(String data) throws Exception{		
		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(data);
			//TODO - validate header and signature
			return jws.getPayloadBytes();
			
		} catch (JoseException e) {
			e.printStackTrace();
			throw new Exception("Failed to parse and validate Json web signture");
		}
	}

}