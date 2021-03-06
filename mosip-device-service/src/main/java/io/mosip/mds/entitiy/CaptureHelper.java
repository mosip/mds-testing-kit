package io.mosip.mds.entitiy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfWriter;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometric;
import io.mosip.mds.dto.DigitalId;
import io.mosip.mds.util.BioAuthRequestUtil;
import io.mosip.mds.util.CryptoUtility;
import io.mosip.mds.util.SecurityUtil;;

@Component
public class CaptureHelper {

	private static final Logger logger = LoggerFactory.getLogger(CaptureHelper.class);

	@Autowired
	private  ObjectMapper mapper;

	@Autowired
	SecurityUtil securityUtil;

	@Autowired
	Store store;

	@Autowired
	BioAuthRequestUtil bioAuthRequestutil;
	
	@Autowired
	private CryptoUtility cryptoUtility;

	private String CAPTURE = "Capture";
	private String RCAPTURE_DECODE_ERROR = "Error while decoding the " + CAPTURE + " request";
	// private static String CAPTURE_DECODE_ERROR = "Error while decoding the " +
	// CAPTURE + " request";

	public CaptureResponse decode(String responseInfo, boolean isRCapture) {

		CaptureResponse response;
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// Deserialize Capture Response
		try {
			response = (CaptureResponse) (mapper.readValue(responseInfo.getBytes(), CaptureResponse.class));
			for (CaptureResponse.CaptureBiometric biometric : response.biometrics) {
				if (biometric.getData() == null) {
					response.setAnalysisError(RCAPTURE_DECODE_ERROR + " : data empty");
					break;
				}
				try {
					// Decode data
					biometric.setDataDecoded((CaptureResponse.CaptureBiometricData) (mapper.readValue(
							securityUtil.getPayload(biometric.getData()), CaptureResponse.CaptureBiometricData.class)));
					if (!isRCapture) {
						if (biometric.getSessionKey() == null) {
							response.analysisError = RCAPTURE_DECODE_ERROR + " : " + "Session Key is empty";
							break;
						}
						String decryptedBioValue = getDecryptedBioValue(biometric);
						biometric.getDataDecoded().setBioValue(decryptedBioValue);
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
			logger.error("Error decoding capture response", exception);
			response = new CaptureResponse();
			response.setAnalysisError(RCAPTURE_DECODE_ERROR + exception.getMessage());
		}
		return response;
	}

	private String getDecryptedBioValue(CaptureBiometric biometric) throws IOException {
		String plainBioValue = cryptoUtility.decryptbio(biometric.sessionKey, biometric.getDataDecoded().bioValue,
				biometric.getDataDecoded().timestamp, biometric.getDataDecoded().getTransactionId(),bioAuthRequestutil.getAuthToken());
		return Base64.getUrlEncoder().encodeToString(plainBioValue.getBytes());
	}

	public PrivateKey getPrivateKey() {		
		try {
			FileInputStream pkeyfis = new FileInputStream("data/keys/PrivateKey.pem");
			String pKey = getFileContent(pkeyfis, "UTF-8");
			pKey = trimBeginEnd(pKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pKey)));
		} catch (Exception ex) {
			logger.error("Error creating private key", ex);
		}
		return null;
	}

	public String getFileContent(FileInputStream fis, String encoding) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		}
	}

	private String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

	/*public static File extractImage(String bioValue, String bioType) {
		// do base64 url decoding
		byte[] decodedData = Base64.getUrlDecoder().decode(bioValue);
		// strip iso header
		//byte[] imageData = extractJPGfromISO(decodedData, bioType);
		// save image to file

		File path = Store.getOrCreateDirectory(Store.getStorePath() + File.separator + "renders");
		String fileName = path.getAbsolutePath()  + File.separator + UUID.randomUUID() + ".jp2";

		File file = new File(fileName);
		try {
			if (file.createNewFile()) {
				OutputStream writer = new FileOutputStream(file);
				writer.write(imageData);
				writer.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return file;
	}*/

	public void createImageFile(byte[] image, String randomFilename, String format, String segment) {
		File path = store.getOrCreateDirectory(store.getStorePath() + File.separator + "renders");
		String fileName = path.getAbsolutePath()  + File.separator + randomFilename + "." + format;

		try(FileOutputStream fos = new FileOutputStream(new File(fileName))) {
			fos.write(image);
			fos.flush();
		} catch (IOException ex) {
			logger.error("Error creating image", ex);
		}

		try {
			String pdfFileName = path.getAbsolutePath()  + File.separator + randomFilename + ".pdf";
			File pdfFile = new File(pdfFileName);
			pdfFile.createNewFile();
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
			document.open();
			Image imageObj = Image.getInstance(fileName);
			PdfImage stream = new PdfImage(imageObj, segment, null);
			document.add(imageObj);
			document.close();

		} catch (Exception ex) {
			logger.error("Error creating image", ex);
		}
	}
}