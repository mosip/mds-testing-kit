package io.mosip.mds.entitiy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.DigitalId;;

public class CaptureHelper {
    public static CaptureResponse Decode(String responseInfo)
    {
		CaptureResponse response = null;
		ObjectMapper mapper = new ObjectMapper();
		//Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// Deserialize Capture Response
		try {
			response = (CaptureResponse)(mapper.readValue(responseInfo.getBytes(), CaptureResponse.class));
			for(CaptureResponse.CaptureBiometric biometric:response.biometrics)
			{
				// extract payload from encoded data
				String[] groups = biometric.data.split("[.]"); 
				if(groups.length != 3)
				{
					response.analysisError = "Error parsing request input. Data not in header.payload.signature format";
					break;
				}
				//String header = groups[0];
				String payload = groups[1];
				//String signature = groups[2];

				// Decode data
				biometric.dataDecoded = (CaptureResponse.CaptureBiometricData) (mapper.readValue(Base64.getUrlDecoder().decode(payload.getBytes()), CaptureResponse.CaptureBiometricData.class));

				// Decode Digital Id
				//biometric.dataDecoded.digitalIdDecoded = (DigitalId) (mapper.readValue(Base64.getUrlDecoder().decode(biometric.dataDecoded.digitalId.getBytes()), DigitalId.class));

			}
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
		List<File> images = new ArrayList<>();
		for(CaptureResponse.CaptureBiometric biometric:response.biometrics)
		{
			File imageFile = ExtractImage(biometric.dataDecoded.bioValue, biometric.dataDecoded.bioSubType);
			images.add(imageFile);
		}

		String renderContent = "<p><u>Capture Info</u></p>";
		renderContent += "<b>Images Captured:</b>" + images.size() + "<br/>";
		for(File file:images)
		{
			renderContent += "<img src=\"data/renders/" + file.getName() + "\"/>";
		}
		return renderContent;
	}
	
	private static File ExtractImage(String bioValue, String bioType)
	{
		// do base64 url decoding 
		byte[] decodedData = Base64.getUrlDecoder().decode(bioValue);
		// strip iso header
		byte[] imageData = ExtractJPGfromISO(decodedData, bioType);
		// save image to file
		String fileName = "data/renders/" + UUID.randomUUID() + ".jpg";
		
		File file = new File(fileName);
		try
		{
			if(file.createNewFile())
			{
				OutputStream writer = new FileOutputStream(file);
				writer.write(imageData);
				writer.close();
			}
		}
		catch(Exception ex)
		{
			file = null;
		}
		return file;
	}

	private static byte[] ExtractJPGfromISO(byte[] isoValue, String bioType)
	{
		// TODO set the correct iso handling technique here
		int isoHeaderSize = 0;
		byte hasCertBlock = 0;
		int recordLength = 0;
		int sizeIndex = 0;
		int imageSize = 0;
		int qbSize = 0;
		int cbSize = 0;
		if(bioType.equalsIgnoreCase("Finger"))
		{
			hasCertBlock = isoValue[14];
			qbSize = isoValue[34] * 5;
			cbSize = (hasCertBlock == 1) ? hasCertBlock + (isoValue[35 + qbSize] * 3) : 0;
			recordLength = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 8, 12)).getInt();
			sizeIndex = 35 + qbSize + cbSize + 18;
			imageSize = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, sizeIndex, sizeIndex + 4)).getInt();
			isoHeaderSize = sizeIndex + 4;
		}
		else if(bioType.equalsIgnoreCase("Face"))
		{
			hasCertBlock = isoValue[14];
			qbSize = isoValue[35] * 5;
			//cbSize = (hasCertBlock == 1) ? hasCertBlock + (isoValue[35 + qbSize] * 3) : 0;
			recordLength = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 8, 12)).getInt();
			int landmarkPoints = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 36 + qbSize, 36 + qbSize + 4)).getShort();
			sizeIndex = 36 + qbSize + (landmarkPoints * 8) + cbSize + 28;
			imageSize = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, sizeIndex, sizeIndex + 4)).getInt();
			isoHeaderSize = sizeIndex + 4;
		}
		else if(bioType.equalsIgnoreCase("Iris"))
		{
			hasCertBlock = isoValue[14];
			qbSize = isoValue[34] * 5;
			//cbSize = (hasCertBlock == 1) ? hasCertBlock + (isoValue[35 + qbSize] * 3) : 0;
			recordLength = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 8, 12)).getInt();
			sizeIndex = 35 + qbSize + cbSize + 29;
			imageSize = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, sizeIndex, sizeIndex + 4)).getInt();
			isoHeaderSize = sizeIndex + 4;
		}
		return Arrays.copyOfRange(isoValue, isoHeaderSize, isoHeaderSize + imageSize);		
	}
}