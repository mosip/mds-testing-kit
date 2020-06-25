package io.mosip.mds.entitiy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.CaptureResponse;
import io.mosip.mds.dto.CaptureResponse.CaptureBiometric;
import io.mosip.mds.dto.DigitalId;;

public class CaptureHelper {

	private static String RCAPTURE = "rCapture";
	private static String CAPTURE = "Capture";
	private static String RCAPTURE_DECODE_ERROR = "Error while decoding the " + CAPTURE + " request";
	// private static String CAPTURE_DECODE_ERROR = "Error while decoding the " +
	// CAPTURE + " request";

	private static String PAYLOAD_EMPTY = "PayLoad Empty";

	public static CaptureResponse Decode(String responseInfo, boolean isRCapture) {

		CaptureResponse response = null;

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

					if (!isRCapture) {

						if (biometric.getSessionKey() == null) {
							response.analysisError = RCAPTURE_DECODE_ERROR + " : " + "Session Key is empty";
							break;
						}

						biometric.getDataDecoded().setBioValue(String.valueOf(getDecryptedBioValue(biometric)));
					}
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

		return response;

	}

	private static byte[] getDecryptedBioValue(CaptureBiometric biometric)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {
		CaptureHelper captureHelper = new CaptureHelper();
		PrivateKey privateKey = captureHelper.getPrivateKeyFromResources("private.key");

		byte[] decryptedSessionKey = decryptSessionKey(privateKey, biometric.getSessionKey().getBytes());

		SecretKey secretKey = getSecretKey(decryptedSessionKey);

		// byte[] saltLastBytes = getLastBytes(timestamp,
		// env.getProperty(IdAuthConfigKeyConstants.IDA_SALT_LASTBYTES_NUM,
		// Integer.class, DEFAULT_SALT_LAST_BYTES_NUM));
		//
		// String salt = CryptoUtil.encodeBase64(saltLastBytes);

		return decryptBioValue(secretKey, biometric.getDataDecoded().getBioValue().getBytes(),
				getAad(biometric.getDataDecoded().getTimestamp()));
	}

	private static byte[] getAad(String timestamp) {
		timestamp = String.valueOf(timestamp);

		byte[] aadLastBytes = getLastBytes(timestamp, 16);

		return org.apache.commons.codec.binary.Base64.encodeBase64(aadLastBytes);
	}

	private static byte[] decryptBioValue(SecretKey secretKey, byte[] data, byte[] aad)
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {

		Cipher cipher;
		cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");

		byte[] output = null;
		byte[] randomIV = generateIV(cipher.getBlockSize());

		SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, randomIV);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
		output = new byte[cipher.getOutputSize(data.length) + cipher.getBlockSize()];
		if (aad != null && aad.length != 0) {
			cipher.updateAAD(aad);
		}
		byte[] processData = cipher.doFinal(data);
		System.arraycopy(processData, 0, output, 0, processData.length);
		System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);

		return output;

	}

	private static SecretKey getSecretKey(byte[] decryptedSessionKey) {
		return new SecretKeySpec(decryptedSessionKey, 0, decryptedSessionKey.length, "AES");

	}

	@SuppressWarnings("restriction")
	private static byte[] decryptSessionKey(PrivateKey privateKey, byte[] data)
			throws InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException {
		Cipher cipher;
		cipher = Cipher.getInstance("RSA/ECB/NoPadding");

		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		/*
		 * This is a hack of removing OEAP padding after decryption with NO Padding as
		 * SoftHSM does not support it.Will be removed after HSM implementation
		 */
		byte[] paddedPlainText = cipher.doFinal(data);
		if (paddedPlainText.length < 2048 / 8) {
			byte[] tempPipe = new byte[2048 / 8];
			System.arraycopy(paddedPlainText, 0, tempPipe, tempPipe.length - paddedPlainText.length,
					paddedPlainText.length);
			paddedPlainText = tempPipe;
		}
		final OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);

		sun.security.rsa.RSAPadding padding = sun.security.rsa.RSAPadding
				.getInstance(sun.security.rsa.RSAPadding.PAD_OAEP_MGF1, 2048 / 8, new SecureRandom(), oaepParams);
		return padding.unpad(paddedPlainText);

	}

	private PrivateKey getPrivateKeyFromResources(String privateKeyFilePath)
			throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		File privateKeyFile = new File(getClass().getClassLoader().getResource(privateKeyFilePath).getFile());

		FileInputStream privateKeyInputStream = new FileInputStream(privateKeyFile);

		DataInputStream privateKeyDataInputStream = new DataInputStream(privateKeyInputStream);
		byte[] keyBytes = new byte[(int) privateKeyFile.length()];
		privateKeyDataInputStream.readFully(keyBytes);
		privateKeyDataInputStream.close();

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(spec);
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

	/**
	 * Gets the last bytes.
	 *
	 * @param timestamp
	 *            the timestamp
	 * @param lastBytesNum
	 *            the last bytes num
	 * @return the last bytes
	 */
	private static byte[] getLastBytes(String timestamp, int lastBytesNum) {
		assert (timestamp.length() >= lastBytesNum);
		return timestamp.substring(timestamp.length() - lastBytesNum).getBytes();
	}

	/**
	 * Generator for IV(Initialisation Vector)
	 * 
	 * @param blockSize
	 *            blocksize of current cipher
	 * @return generated IV
	 */
	private static byte[] generateIV(int blockSize) {
		byte[] byteIV = new byte[blockSize];
		new SecureRandom().nextBytes(byteIV);
		return byteIV;
	}
}