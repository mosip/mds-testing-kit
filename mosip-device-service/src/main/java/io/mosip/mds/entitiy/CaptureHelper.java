package io.mosip.mds.entitiy;

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

	private static String RCAPTURE = "rCapture";
	private static String CAPTURE = "Capture";
	private static String RCAPTURE_DECODE_ERROR = "Error while decoding the " + RCAPTURE + " request";
	private static String CAPTURE_DECODE_ERROR = "Error while decoding the " + CAPTURE + " request";

	private static String PAYLOAD_EMPTY = "PayLoad Empty";

	public static CaptureResponse Decode(String responseInfo, boolean isRCapture) {

		CaptureResponse response = null;
		if (isRCapture) {
			ObjectMapper mapper = new ObjectMapper();

			// Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			// Deserialize Capture Response
			try {
				response = (CaptureResponse) (mapper.readValue(responseInfo.getBytes(), CaptureResponse.class));

				for (CaptureResponse.CaptureBiometric biometric : response.biometrics) {

					if (biometric.getData() == null) {

						response.setAnalysisError(RCAPTURE_DECODE_ERROR + " : data empty");
						break;

					}
					// extract payload from encoded data
					String[] groups = biometric.getData().split("[.]");
					if (groups.length != 3) {
						response.setAnalysisError(
								"Error parsing request input. Data not in header.payload.signature format");
						break;
					}
					// String header = groups[0];
					String payload = groups[1];
					// String signature = groups[2];

					if (payload == null) {
						response.analysisError = RCAPTURE_DECODE_ERROR + " : " + PAYLOAD_EMPTY;
						break;

					}
					try {
						// Decode data
						biometric.setDataDecoded((CaptureResponse.CaptureBiometricData) (mapper.readValue(
								Base64.getUrlDecoder().decode(payload.getBytes()),
								CaptureResponse.CaptureBiometricData.class)));

						// TODO Verify Digital Id with mock mds
						// Decode.DigitalId

						if (biometric.getDataDecoded().getDigitalId() != null) {
							biometric.getDataDecoded()
									.setDigitalIdDecoded((DigitalId) (mapper.readValue(
											Base64.getUrlDecoder()
													.decode(biometric.getDataDecoded().getDigitalId().getBytes()),
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
		}

		return response;

	}

	public static String Render(CaptureResponse response) {
		// TODO modify this method for proper reponse
		List<File> images = new ArrayList<>();
		for (CaptureResponse.CaptureBiometric biometric : response.biometrics) {
			File imageFile = ExtractImage(biometric.dataDecoded.bioValue, biometric.dataDecoded.bioSubType);
			images.add(imageFile);
		}

		String renderContent = "<p><u>Capture Info</u></p>";
		renderContent += "<b>Images Captured:</b>" + images.size() + "<br/>";
		for (File file : images) {
			renderContent += "<img src=\"data/renders/" + file.getName() + "\"/>";
		}
		return renderContent;
	}

	private static File ExtractImage(String bioValue, String bioType) {
		// do base64 url decoding
		byte[] decodedData = Base64.getUrlDecoder().decode(bioValue);
		// strip iso header
		byte[] imageData = ExtractJPGfromISO(decodedData, bioType);
		// save image to file
		String fileName = "data/renders/" + UUID.randomUUID() + ".jp2";

		File file = new File(fileName);
		try {
			if (file.createNewFile()) {
				OutputStream writer = new FileOutputStream(file);
				writer.write(imageData);
				writer.close();
			}
		} catch (Exception ex) {
			file = null;
		}
		return file;
	}

	private static byte[] ExtractJPGfromISO(byte[] isoValue, String bioType) {
		// TODO set the correct iso handling technique here
		int isoHeaderSize = 0;
		byte hasCertBlock = 0;
		int recordLength = 0;
		int sizeIndex = 0;
		int imageSize = 0;
		int qbSize = 0;
		int cbSize = 0;
		if (bioType.equalsIgnoreCase("Finger")) {
			hasCertBlock = isoValue[14];
			qbSize = isoValue[34] * 5;
			cbSize = (hasCertBlock == 1) ? hasCertBlock + (isoValue[35 + qbSize] * 3) : 0;
			recordLength = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 8, 12)).getInt();
			sizeIndex = 35 + qbSize + cbSize + 18;
			imageSize = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, sizeIndex, sizeIndex + 4)).getInt();
			isoHeaderSize = sizeIndex + 4;
		} else if (bioType.equalsIgnoreCase("Face")) {
			hasCertBlock = isoValue[14];
			qbSize = isoValue[35] * 5;
			// cbSize = (hasCertBlock == 1) ? hasCertBlock + (isoValue[35 + qbSize] * 3) :
			// 0;
			recordLength = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 8, 12)).getInt();
			int landmarkPoints = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 36 + qbSize, 36 + qbSize + 4)).getShort();
			sizeIndex = 36 + qbSize + (landmarkPoints * 8) + cbSize + 28;
			imageSize = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, sizeIndex, sizeIndex + 4)).getInt();
			isoHeaderSize = sizeIndex + 4;
		} else if (bioType.equalsIgnoreCase("Iris")) {
			hasCertBlock = isoValue[14];
			qbSize = isoValue[34] * 5;
			// cbSize = (hasCertBlock == 1) ? hasCertBlock + (isoValue[35 + qbSize] * 3) :
			// 0;
			recordLength = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, 8, 12)).getInt();
			sizeIndex = 35 + qbSize + cbSize + 29;
			imageSize = ByteBuffer.wrap(Arrays.copyOfRange(isoValue, sizeIndex, sizeIndex + 4)).getInt();
			isoHeaderSize = sizeIndex + 4;
		}
		return Arrays.copyOfRange(isoValue, isoHeaderSize, isoHeaderSize + imageSize);
	}
}