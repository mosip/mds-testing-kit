package io.mosip.mds.service;

import io.mosip.mds.dto.CaptureRequest;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoRequest;
import io.mosip.mds.dto.DiscoverRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest;
import io.mosip.mds.dto.StreamRequest;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.util.BioSubType;
import io.mosip.mds.util.Intent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MDS_0_9_5_RequestBuilder implements IMDSRequestBuilder {

	 private static ObjectMapper mapper;

    static {
    	mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public String getSpecVersion()
    {
        return "0.9.5";
    }

    String defaultPort = "4501";

    private String getPort(DeviceDto deviceDto)
	{
		return (deviceDto != null && deviceDto.port != null) ? deviceDto.port : defaultPort;
	}


    public ComposeRequestResponseDto buildRequest(TestRun run, TestExtnDto test, DeviceDto device, Intent op)
    {
        ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(run.runId, test.testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		
		
        try
        {
            switch(op)
            {
                case Discover:
                    requestInfoDto.verb = "MOSIPDISC";
                    requestInfoDto.body = getDiscoverRequest(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/device";
                    break;
                case Stream:
                    requestInfoDto.verb = "STREAM";
                    requestInfoDto.body = getStreamRequest(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/stream";
                break;
                case Capture:
                    requestInfoDto.verb = "CAPTURE";
                    requestInfoDto.body = getCaptureRequest(run.targetProfile, test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                break;
                case RegistrationCapture:
                    requestInfoDto.verb = "RCAPTURE";
                    requestInfoDto.body = getRegistrationCaptureRequest(run.targetProfile, test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                break;
                case DeviceInfo:
                    requestInfoDto.verb = "MOSIPDINFO";
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/info";
                    requestInfoDto.body = getDeviceInfoRequest(test, device);
                    break;
        }
        }
        catch(JsonProcessingException jEx)
        {
        	jEx.printStackTrace();
        }
        composeRequestResponseDto.requestInfoDto = requestInfoDto;
        return composeRequestResponseDto;
    }

    private String getDiscoverRequest(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        DiscoverRequest requestBody = new DiscoverRequest();
        requestBody.type = "BIOMETRIC DEVICE";
        return mapper.writeValueAsString(requestBody);
    }

    private String getDeviceInfoRequest(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        DeviceInfoRequest requestBody = new DeviceInfoRequest();
        return mapper.writeValueAsString(requestBody);
    }

    private String getStreamRequest(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        StreamRequest requestBody = new StreamRequest();
        requestBody.deviceId = device.discoverInfo;
        requestBody.deviceSubId = 1;
        // TODO extract discoverinfo into device dto
        return mapper.writeValueAsString(requestBody);
    }

    private String getCaptureRequest(TestManagerDto targetProfile, TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        CaptureRequest requestBody = new CaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        requestBody.domainUri = "default";
        requestBody.env = "Developer";
        requestBody.purpose = "AUTHENTICATION";
        requestBody.specVersion = "0.9.5";
        requestBody.timeout = 30;
        requestBody.transactionId = "" + System.currentTimeMillis();
        
        requestBody.bio = new CaptureRequest.CaptureBioRequest[test.segments.size()];
        
        int i=0;
        for(String segment : test.segments) {
        	CaptureRequest.CaptureBioRequest bio = requestBody.new CaptureBioRequest();
            bio.count = 1;
            bio.deviceId = device.deviceInfo.deviceId;
            bio.deviceSubId = getDeviceSubId(targetProfile.deviceSubType, segment);
            bio.previousHash = "";
            bio.requestedScore = 80;            
            bio.bioSubType = getBioSubTypes(segment, test.exceptions).toArray(new String[0]);            
            bio.type = targetProfile.biometricType;
            requestBody.bio[i++] = bio;
        }

        return mapper.writeValueAsString(requestBody);
    }

    private String getRegistrationCaptureRequest(TestManagerDto targetProfile, TestExtnDto test, DeviceDto device)
    		throws JsonProcessingException
    {
        RegistrationCaptureRequest requestBody = new RegistrationCaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        //requestBody.domainUri = "default";
        requestBody.env = "Developer";
        requestBody.purpose = "Registration";
        requestBody.specVersion = "0.9.5";
        requestBody.timeout = 30;
        requestBody.transactionId = "" + System.currentTimeMillis();
        
        requestBody.bio = new RegistrationCaptureRequest.RegistrationCaptureBioRequest[test.segments.size()];
       
        int i=0;
        for(String segment : test.segments) {
        	RegistrationCaptureRequest.RegistrationCaptureBioRequest bio = requestBody.new RegistrationCaptureBioRequest();
            
        	bio.type = targetProfile.biometricType;
        	bio.previousHash = "";
            bio.deviceId = device.deviceInfo.deviceId;
            bio.requestedScore = 80;
            
            bio.deviceSubId = getDeviceSubId(targetProfile.deviceSubType, segment);         
            
            bio.exception = test.exceptions == null ? new String[0] : 
            	BioSubType.convertTo095(test.exceptions).toArray(new String[0]);
            
            bio.bioSubType = null; 
            bio.count = getCount(segment, bio.exception);

            requestBody.bio[i++] = bio;
        }
        return mapper.writeValueAsString(requestBody);
    }

    
    /*private String getBioType(String biometricType) {
    	switch (biometricType) {
		case "Finger":	return "Finger";		
		case "Iris": return "Iris";			
		case "Face": return "Face";
		}
    	return null;
    }*/
    
    private List<String> getBioSubTypes(String segment, List<String> exceptions) {
    	if("FULL_FACE".equals(segment))
    		return new ArrayList<>();
    	
    	List<String> subTypes = BioSubType.get095BioSubTypes(segment);

    	if(exceptions != null)
    		subTypes.removeAll(BioSubType.convertTo095(exceptions));

    	return subTypes;
    }

    private int getCount(String segment, String[] exceptions) {
    	switch (segment) {
		case "LEFT_SLAP":
			return 4 - exceptions.length;

		case "RIGHT_SLAP":
			return 4 - exceptions.length;

		case "TWO_THUMBS":
			return 2 - exceptions.length;
			
		case "LEFT_IRIS":			
			return 1 - exceptions.length;

		case "RIGHT_IRIS":			
			return 1 - exceptions.length;
			
		case "TWO_IRIS":			
			return 2 - exceptions.length;
			
		case "FULL_FACE":			
			return 1;
		}
    	return 0;
    }

    private int getDeviceSubId(String deviceType, String segment) {
    	switch (deviceType) {
		case "Slap": 
			if(segment.equals("LEFT_SLAP"))
				return 1;

			if(segment.equals("RIGHT_SLAP"))
				return 2;

			if(segment.equals("TWO_THUMBS"))
				return 3;

			break;

		
		case "Double":
			if(segment.equals("LEFT_IRIS"))
				return 1;
			
			if(segment.equals("RIGHT_IRIS"))
				return 2;
			
			if(segment.equals("TWO_IRIS"))
				return 3;
			
		case "Single": 
			if(segment.equals("LEFT_IRIS"))
				return 1;
			
			if(segment.equals("RIGHT_IRIS"))
				return 2;
						
			break;
		case "Full face":
			if(segment.equals("FULL_FACE"))
				return 0;
			break;
		}
    	return 0;
    }

}
