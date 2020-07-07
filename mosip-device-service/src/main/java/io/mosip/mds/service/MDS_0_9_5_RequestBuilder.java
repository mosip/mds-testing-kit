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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
                    String streamUrl = "http://127.0.0.1:" + getPort(device) + "/stream?deviceId=%s&deviceSubId=%s";                    
                    composeRequestResponseDto.streamUrl = String.format(streamUrl, device.deviceInfo.deviceId, test.deviceSubId);
                    
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
        requestBody.type = "Biometric Device";
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
        requestBody.deviceId = device.deviceInfo.deviceId;
        requestBody.deviceSubId = test.deviceSubId;
        return mapper.writeValueAsString(requestBody);
    }

    private String getCaptureRequest(TestManagerDto targetProfile, TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        CaptureRequest requestBody = new CaptureRequest();
        requestBody.captureTime = getTimestamp();
        requestBody.domainUri = "default";
        requestBody.env = "Developer";
        requestBody.purpose = "Auth";
        requestBody.specVersion = "0.9.5";
        requestBody.timeout = 10000;
        requestBody.transactionId = "" + System.currentTimeMillis();
        
        requestBody.bio = new CaptureRequest.CaptureBioRequest[1];
        
        CaptureRequest.CaptureBioRequest bio = requestBody.new CaptureBioRequest();
        bio.count = test.bioCount;
        bio.deviceId = device.deviceInfo.deviceId;
        bio.deviceSubId = test.deviceSubId;
        bio.previousHash = "";
        bio.requestedScore = test.requestedScore;
        bio.bioSubType = test.segments == null ? null : 
        	BioSubType.convertTo095(test.segments).toArray(new String[0]);            
        bio.type = targetProfile.biometricType;
        requestBody.bio[0] = bio;
        return mapper.writeValueAsString(requestBody);
    }

    private String getRegistrationCaptureRequest(TestManagerDto targetProfile, TestExtnDto test, DeviceDto device)
    		throws JsonProcessingException
    {
        RegistrationCaptureRequest requestBody = new RegistrationCaptureRequest();
        requestBody.captureTime = getTimestamp();
        //requestBody.domainUri = "default";
        requestBody.env = "Developer";
        requestBody.purpose = "Registration";
        requestBody.specVersion = "0.9.5";
        requestBody.timeout = 10000;
        requestBody.transactionId = "" + System.currentTimeMillis();
        
        requestBody.bio = new RegistrationCaptureRequest.RegistrationCaptureBioRequest[1];
        
        RegistrationCaptureRequest.RegistrationCaptureBioRequest bio = requestBody.new RegistrationCaptureBioRequest();        
    	bio.type = targetProfile.biometricType;
    	bio.previousHash = "";
        bio.deviceId = device.deviceInfo.deviceId;
        bio.requestedScore = test.requestedScore;;
        bio.deviceSubId = test.deviceSubId;
        bio.count = test.bioCount;
        
        bio.exception = test.exceptions == null ? new String[0] : 
        	BioSubType.convertTo095(test.exceptions).toArray(new String[0]);
        
        bio.bioSubType = test.segments == null ? null : 
        	BioSubType.convertTo095(test.segments).toArray(new String[0]);
        
        requestBody.bio[0] = bio;        
        return mapper.writeValueAsString(requestBody);
    }

     public static String getTimestamp() {
    	DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    	return formatter.format(ZonedDateTime.now());
    }

}
