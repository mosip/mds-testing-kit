package io.mosip.mds.service;

import io.mosip.mds.dto.CaptureRequest;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoRequest;
import io.mosip.mds.dto.DiscoverRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest;
import io.mosip.mds.dto.StreamRequest;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.util.Intent;

import java.util.Date;

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
                    requestInfoDto.body = getCaptureRequest(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                break;
                case RegistrationCapture:
                    requestInfoDto.verb = "RCAPTURE";
                    requestInfoDto.body = getRegistrationCaptureRequest(test, device);
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

    private String getCaptureRequest(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        CaptureRequest requestBody = new CaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        requestBody.domainUri = "default";
        requestBody.env = "test";
        requestBody.purpose = "AUTHENTICATION";
        requestBody.specVersion = "0.9.5";
        requestBody.timeout = 30;
        requestBody.transactionId = "" + System.currentTimeMillis();
        CaptureRequest.CaptureBioRequest bio = requestBody.new CaptureBioRequest();
        bio.count = 1;
//        bio.deviceId = device.discoverInfo;
        bio.deviceId = 1;
        bio.deviceSubId = 1;
        bio.previousHash = "";
        bio.requestedScore = 80;
        bio.bioSubType = new String[]{"FULL"};
        bio.type = "FACE";
        requestBody.bio = new CaptureRequest.CaptureBioRequest[]{bio};
        return mapper.writeValueAsString(requestBody);
    }

    private String getRegistrationCaptureRequest(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        RegistrationCaptureRequest requestBody = new RegistrationCaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        //requestBody.domainUri = "default";
        requestBody.env = "Developer";
        requestBody.purpose = "Registration";
        requestBody.specVersion = "0.9.5";
        requestBody.timeout = 30;
        requestBody.transactionId = "" + System.currentTimeMillis();
       
        RegistrationCaptureRequest.RegistrationCaptureBioRequest bio = requestBody.new RegistrationCaptureBioRequest();
        bio.count = 2;
        bio.deviceId = device.deviceInfo.deviceId;
        bio.deviceSubId = 3;
        bio.previousHash = "";
        bio.requestedScore = 80;
        bio.exception = new String[]{};
        bio.type = "Iris";
        bio.bioSubType = new String[] {"Left", "Right"};        
        requestBody.bio = new RegistrationCaptureRequest.RegistrationCaptureBioRequest[]{bio};
        return mapper.writeValueAsString(requestBody);
    }
    
    
    private String getBioType(String biometricType) {
    	switch (biometricType) {
		case "FINGERPRINT":	return "Finger";
		
		case "IRIS": return "Iris";
			
		case "FACE": return "Face";
		}
    	return null;
    }

}
