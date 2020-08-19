package io.mosip.mds.service;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.MdsResponse;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.entitiy.CaptureHelper;
import io.mosip.mds.entitiy.DeviceInfoHelper;
import io.mosip.mds.entitiy.DiscoverHelper;
import io.mosip.mds.helper.ExtractDTO;
import io.mosip.mds.util.Intent;
import io.mosip.mds.util.SecurityUtil;

@Component
public class MDS_0_9_5_ResponseProcessor implements IMDSResponseProcessor {

	@Autowired
	private  ObjectMapper mapper;

	@Autowired
	CaptureHelper captureHelper;

	@Autowired
	DeviceInfoHelper deviceInfoHelper;

	@Autowired
	DiscoverHelper discoverHelper;

	@Autowired
	SecurityUtil securityUtil;

	private String RCAPTURE = "rCapture";
	private String CAPTURE = "Capture";
	private String RCAPTURE_DECODE_ERROR = "Error while decoding the " + CAPTURE + " request";

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
		StringBuilder builder = new StringBuilder("<p><u>Capture Info</u></p>");
		//builder.append("<b>Images Captured:</b>" + images.size() + "<br/>");

		if(captureResponse.biometrics != null) {
			for (CaptureResponse.CaptureBiometric biometric : captureResponse.biometrics) {
				byte[] decodedData = Base64.getUrlDecoder().decode(biometric.dataDecoded.bioValue);
				List<ExtractDTO> extractDTOS = captureHelper.extractJPGfromISO(decodedData,
						biometric.dataDecoded.bioType);
				if(extractDTOS != null) {
					for(ExtractDTO extract : extractDTOS) {
						String filename = java.util.UUID.randomUUID().toString();
						captureHelper.createImageFile(extract.getImage(), filename, extract.getFormat(),
								extract.getName());
						//data:image/jp2;base64,UklGRkoAAABXRUJQVlA4WAoAAAAQAAAAAAAAAAAAQUxQSAwAAAABBxAR/Q9ERP8DAABWUDggGAAAADABAJ0BKgEAAQADADQlpAADcAD++/1QAA==
						//builder.append("<img src=\""+filename+".pdf\"/><br/>");
						builder.append("<h3> "+extract.getName().toUpperCase()+" : </h3> <object data=\""+ filename+".pdf\" width=\"200\" height=\"200\" type=\"application/pdf\" style=\"display: 'block';\" ></object></br>");
						//builder.append("<img src=\"data:image/png;base64," + DatatypeConverter.printBase64Binary(extract.getImage()) +"\"/><br/>");
					}
				}
			}
		}
		return builder.toString();
	}

	@Override
	public CaptureResponse getCaptureResponse(Intent method, String encodedValue) {
		switch(method) {
		case Capture:
			return captureHelper.decode(encodedValue,false);
		case RegistrationCapture:
			return captureHelper.decode(encodedValue,true);
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
			DeviceInfoResponse[] diResponse = deviceInfoHelper.decode(responseData);
			for (DeviceInfoResponse deviceInfoResponse : diResponse) {
				renderContent += deviceInfoHelper.getRenderContent(deviceInfoResponse) + "<BR/>";
			} 
			renderContent = "";
			break;
		case Discover:
			DiscoverResponse[] dResponse = discoverHelper.decode(responseData);
			for (DiscoverResponse discoverResponse : dResponse) {
				renderContent += discoverHelper.getRenderContent(discoverResponse) + "<BR/>";
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

	public CaptureResponse decode(String responseInfo, boolean isRCapture) {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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
							securityUtil.getPayload(biometric.getData()), CaptureResponse.CaptureBiometricData.class)));

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
						.setDigitalIdDecoded((DigitalId) (mapper.readValue(securityUtil.getPayload(biometric.getDataDecoded().getDigitalId()),
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

	@Override
	public MdsResponse[] getMdsDecodedResponse(Intent method, String encodedValue) {
		MdsResponse[] mdsResponses=new MdsResponse[1];
		switch(method) {
		case Capture:
			mdsResponses[0]=captureHelper.decode(encodedValue,false);
			return mdsResponses;
		case RegistrationCapture:
			mdsResponses[0]=captureHelper.decode(encodedValue,true);
			return mdsResponses;
		case DeviceInfo:
			return deviceInfoHelper.decode(encodedValue); 
		case Discover:
			return discoverHelper.decode(encodedValue);
		}
		return null;


	}

}