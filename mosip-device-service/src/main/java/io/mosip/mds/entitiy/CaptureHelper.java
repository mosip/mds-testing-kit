package io.mosip.mds.entitiy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;

public class CaptureHelper {
    public static CaptureResponse Decode(String responseInfo)
    {
		CaptureResponse response = null;
		ObjectMapper mapper = new ObjectMapper();
		//Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			response = (CaptureResponse)(mapper.readValue(responseInfo.getBytes(), CaptureResponse.class));
        }
 		catch (Exception exception) {
			response = new CaptureResponse();
			response.analysisError = "Error parsing request input" + exception.getMessage();
		}
		return response;
    }

    public static String Render(CaptureResponse response)
    {
        //TODO modify this method for proper reponse
		String renderContent = "<p><u>Capture Info</u></p>";
		renderContent += "<b>Response: </b>" + response.toString() + "<br/>";
		return renderContent;
    }
}