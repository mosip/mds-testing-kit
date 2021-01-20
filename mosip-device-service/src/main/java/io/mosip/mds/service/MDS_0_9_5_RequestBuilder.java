package io.mosip.mds.service;

import io.mosip.mds.dto.*;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.util.BioSubType;
import io.mosip.mds.util.Intent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.mosip.mds.dto.CaptureRequest;
import io.mosip.mds.dto.CaptureRequest.CaptureBioRequest;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoRequest;
import io.mosip.mds.dto.DiscoverRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest.RegistrationCaptureBioRequest;
import io.mosip.mds.dto.StreamRequest;
import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.util.BioSubType;
import io.mosip.mds.util.Intent;

@Component
public class MDS_0_9_5_RequestBuilder implements IMDSRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MDS_0_9_5_RequestBuilder.class);

    @Autowired
	private ObjectMapper mapper;

    public String getSpecVersion()
    {
        return "0.9.5";
    }

    String defaultPort = "4501";

    private String getPort(DeviceDto deviceDto)
	{
		return (deviceDto != null && deviceDto.port != null) ? deviceDto.port : defaultPort;
	}


    public ComposeRequestResponseDto buildRequest(String runId, TestManagerDto targetProfile, TestDefinition test, DeviceDto device, Intent op)
    {
        ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(runId, test.testId);
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
                    requestInfoDto.body = getCaptureRequest(targetProfile, test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                break;
                case RegistrationCapture:
                    requestInfoDto.verb = "RCAPTURE";
                    requestInfoDto.body = getRegistrationCaptureRequest(targetProfile, test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                   // String streamUrl = "http://127.0.0.1:" + getPort(device) + "/stream?deviceId=%s&deviceSubId=%s";                    
                    requestInfoDto.streamUrl = "http://127.0.0.1:" + getPort(device) + "/stream";                    
                   // composeRequestResponseDto.streamUrl = String.format(streamUrl, device.deviceInfo.deviceId, test.deviceSubId);
                    
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
        	logger.error("Error building request", jEx);
        }
        composeRequestResponseDto.requestInfoDto = requestInfoDto;
        return composeRequestResponseDto;
    }

    //TODO - consider testDefinition.biometricTypes - but its an array
    private String getDiscoverRequest(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        DiscoverRequest requestBody = new DiscoverRequest();
        requestBody.type = (test.biometricTypes == null || test.biometricTypes.size() == 0) ? null : test.biometricTypes.get(0);
        return mapper.writeValueAsString(requestBody);
    }

    private String getDeviceInfoRequest(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
    	mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DeviceInfoRequest requestBody = new DeviceInfoRequest();
        return mapper.writeValueAsString(requestBody);
    }

    private String getStreamRequest(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        StreamRequest requestBody = new StreamRequest();
        requestBody.deviceId = device.deviceInfo.deviceId;
        requestBody.deviceSubId = test.deviceSubId;
        return mapper.writeValueAsString(requestBody);
    }

    private String getCaptureRequest(TestManagerDto targetProfile, TestDefinition test, DeviceDto device) throws JsonProcessingException
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
        
        CaptureRequest.CaptureBioRequest bio = new CaptureBioRequest();
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

    private String getRegistrationCaptureRequest(TestManagerDto targetProfile, TestDefinition test, DeviceDto device)
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
        
        RegistrationCaptureRequest.RegistrationCaptureBioRequest bio = new RegistrationCaptureBioRequest();        
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

     public String getTimestamp() {
    	DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    	return formatter.format(ZonedDateTime.now());
    }

}
