package io.mosip.mds.service;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureRequest;
import io.mosip.mds.dto.CaptureRequest.CaptureBioRequest;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoRequest;
import io.mosip.mds.dto.DiscoverRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest_0_9_2;
import io.mosip.mds.dto.StreamRequest;
import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.util.Intent;

public class MDS_0_9_2_RequestBuilder implements IMDSRequestBuilder {

    public String getSpecVersion()
    {
        return "0.9.2";
    }

    String defaultPort = "4501";

    private String getPort(DeviceDto deviceDto)
	{
		return (deviceDto != null && deviceDto.port != null) ? deviceDto.port : defaultPort;
	}
    
    private ObjectMapper mapper = new ObjectMapper();

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
                    requestInfoDto.body = discover(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/device";
                    break;
                case Stream:
                    requestInfoDto.verb = "STREAM";
                    requestInfoDto.body = stream(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/stream";
                break;
                case Capture:
                    requestInfoDto.verb = "CAPTURE";
                    requestInfoDto.body = capture(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                break;
                case RegistrationCapture:
                    requestInfoDto.verb = "RCAPTURE";
                    requestInfoDto.body = registrationCapture(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/capture";
                break;
                case DeviceInfo:
                    requestInfoDto.verb = "MOSIPDINFO";
                    requestInfoDto.body = deviceInfo(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + getPort(device) + "/device";
        }
        }
        catch(JsonProcessingException jEx)
        {
        }
        composeRequestResponseDto.requestInfoDto = requestInfoDto;
        return composeRequestResponseDto;
    }

    private String discover(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        DiscoverRequest requestBody = new DiscoverRequest();
        requestBody.type = "BIOMETRIC DEVICE";
        return mapper.writeValueAsString(requestBody);
    }

    private String deviceInfo(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        DeviceInfoRequest requestBody = new DeviceInfoRequest();
        return mapper.writeValueAsString(requestBody);
    }

    private String stream(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        StreamRequest requestBody = new StreamRequest();
        requestBody.deviceId = device.discoverInfo;
        requestBody.deviceSubId = "1";
        // TODO extract discoverinfo into device dto
        return mapper.writeValueAsString(requestBody);
    }

    private String capture(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        CaptureRequest requestBody = new CaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        requestBody.domainUri = "default";
        requestBody.env = "test";
        requestBody.purpose = "AUTHENTICATION";
        requestBody.specVersion = "0.9.2";
        requestBody.timeout = "30";
        requestBody.transactionId = "" + System.currentTimeMillis();
        CaptureRequest.CaptureBioRequest bio = new CaptureBioRequest();
        bio.count = "1";
//        bio.deviceId = device.discoverInfo;
        bio.deviceId = "1";
        bio.deviceSubId = "1";
        bio.previousHash = "";
        bio.requestedScore = "80";
        bio.bioSubType = new String[]{"FULL"};
        bio.type = "FACE";
        requestBody.bio = new CaptureRequest.CaptureBioRequest[]{bio};
        return mapper.writeValueAsString(requestBody);
    }

    private String registrationCapture(TestDefinition test, DeviceDto device) throws JsonProcessingException
    {
        RegistrationCaptureRequest_0_9_2 requestBody = new RegistrationCaptureRequest_0_9_2();
        requestBody.captureTime = (new Date()).toString();
        //requestBody.domainUri = "default";
        requestBody.env = "test";
        //requestBody.purpose = "AUTHENTICATION";
        requestBody.specVersion = "0.9.2";
        requestBody.timeout = "30";
        requestBody.registrationId = "" + System.currentTimeMillis();
        RegistrationCaptureRequest_0_9_2.RegistrationCaptureBioRequest_0_9_2 bio = requestBody.new RegistrationCaptureBioRequest_0_9_2();
        bio.count = 1;
        bio.deviceId = device.discoverInfo;
        bio.deviceSubId = 1;
        bio.previousHash = "";
        bio.requestedScore = 80;
        bio.exception = new String[]{};
        bio.type = "FACE";
        requestBody.bio = new RegistrationCaptureRequest_0_9_2.RegistrationCaptureBioRequest_0_9_2[]{bio};
        return mapper.writeValueAsString(requestBody);
    }

}
