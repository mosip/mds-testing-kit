package io.mosip.mds.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.entitiy.CaptureHelper;
import io.mosip.mds.entitiy.DeviceInfoHelper;
import io.mosip.mds.entitiy.DiscoverHelper;
import io.mosip.mds.util.Intent;
import io.mosip.mds.util.SecurityUtil;

public class MDS_0_9_5_ResponseProcessor implements IMDSResponseProcessor {
	
	private static ObjectMapper mapper;
	
	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	private static String RCAPTURE = "rCapture";
	private static String CAPTURE = "Capture";
	private static String RCAPTURE_DECODE_ERROR = "Error while decoding the " + CAPTURE + " request";

    @Override
    public String getSpecVersion() {
        return "0.9.5";
    }

    @Override
    public String processResponse(TestRun run, TestExtnDto test, DeviceDto device, Intent op) {
        switch(op)
        {
            case Capture:
                return processCapture(run, test, device);
            case DeviceInfo:
                return processDeviceInfo(run, test, device);
            case Discover:
                return processDiscover(run, test, device);
            case RegistrationCapture:
                //return processRegistrationCapture(run, test, device);
            case Stream:
                return processStream(run, test, device);
            default:
                return "";
        }
    }

    private String processCapture(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String processDiscover(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String processDeviceInfo(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String processStream(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String getCaptureRenderContent(String responseData, boolean isRCapture)
    {
    	CaptureResponse captureResponse = decode(responseData,isRCapture);
    	List<File> images = new ArrayList<>();
    	
    	if(captureResponse.biometrics != null) {
    		for (CaptureResponse.CaptureBiometric biometric : captureResponse.biometrics) {
    			File imageFile = CaptureHelper.extractImage(biometric.dataDecoded.bioValue, 
    					biometric.dataDecoded.bioSubType);
    			images.add(imageFile);
    		}
    	}

		String renderContent = "<p><u>Capture Info</u></p>";
		renderContent += "<b>Images Captured:</b>" + images.size() + "<br/>";
		for (File file : images) {
			renderContent += "<img src=\"data/renders/" + file.getName()+ "\"/>";
		}
		return renderContent;
    }
    
    @Override
    public CaptureResponse getCaptureResponse(Intent method, String encodedValue) {
    	switch(method) {
		case Capture:
			return CaptureHelper.decode(encodedValue,false);
		case RegistrationCapture:
			return CaptureHelper.decode(encodedValue,true);
		}
		return null;
    }
    
    @Override
    public String getRenderContent(Intent method, String responseData) {
    	String renderContent = "";
    	switch(method)
		{
			case Capture:
				renderContent += getCaptureRenderContent(responseData, false);
				break;
			case RegistrationCapture:
				renderContent += getCaptureRenderContent(responseData, true);
				break;
			case DeviceInfo:
				DeviceInfoResponse[] diResponse = DeviceInfoHelper.decode(responseData);;
				for (DeviceInfoResponse deviceInfoResponse : diResponse) {
					renderContent += DeviceInfoHelper.getRenderContent(deviceInfoResponse) + "<BR/>";
				} 
				renderContent = "";
				break;
			case Discover:
				DiscoverResponse[] dResponse = DiscoverHelper.decode(responseData);
				for (DiscoverResponse discoverResponse : dResponse) {
					renderContent += DiscoverHelper.getRenderContent(discoverResponse) + "<BR/>";
				} 
				break;
			case Stream:
				renderContent = "<p><u>Stream Output</u></p><img alt=\"stream video feed\" src=\"127.0.0.1:4501/stream\" style=\"height:200;width:200;\">";
				break;
			default:
				renderContent = "<img src=\"https://www.mosip.io/images/logo.png\"/>";
		}
		return renderContent;
    }
    
    public static CaptureResponse decode(String responseInfo, boolean isRCapture) {

		CaptureResponse response = null;
		try {
			response = (CaptureResponse) (mapper.readValue(responseInfo.getBytes(), CaptureResponse.class));

			for (CaptureResponse.CaptureBiometric biometric : response.biometrics) {

				if (biometric.getData() == null) {
					response.setAnalysisError(RCAPTURE_DECODE_ERROR + " : data empty");
					break;
				}
				
				try {
					biometric.setDataDecoded((CaptureResponse.CaptureBiometricData) (mapper.readValue(
							SecurityUtil.getPayload(biometric.getData()), CaptureResponse.CaptureBiometricData.class)));

					if (!isRCapture) {

						if (biometric.getSessionKey() == null) {
							response.analysisError = RCAPTURE_DECODE_ERROR + " : " + "Session Key is empty";
							break;
						}

						//TODO -- decrypt bioValue
					}
					// TODO Verify Digital Id with mock mds
					// Decode.DigitalId

					if (biometric.getDataDecoded().getDigitalId() != null) {
						biometric.getDataDecoded()
								.setDigitalIdDecoded((DigitalId) (mapper.readValue(SecurityUtil.getPayload(biometric.getDataDecoded().getDigitalId()),
										DigitalId.class)));
					} else {
						response.analysisError = RCAPTURE_DECODE_ERROR + " : " + "digital id is empty";
						break;
					}

				} catch (IllegalArgumentException illegalArgumentException) {
					response.setAnalysisError(RCAPTURE_DECODE_ERROR + " : Error while decoding payload"
							+ illegalArgumentException.getMessage());
				}

			}
		} catch (Exception exception) {
			response = new CaptureResponse();
			response.setAnalysisError(RCAPTURE_DECODE_ERROR + exception.getMessage());
		}
		return response;
	}
    
}