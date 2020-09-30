package io.mosip.mds.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.MGF1ParameterSpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PSource.PSpecified;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.mds.entitiy.CaptureHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.squareup.okhttp.*;

@Component
public class CryptoUtility {

	private static final String IDA_FIR = "IDA-FIR";

	private static final String IDA = "IDA";

	private static final String KEY_SPLITTER = "#KEY_SPLITTER#";

	private static final Logger logger = LoggerFactory.getLogger(CryptoUtility.class);

	private static String DECRYPT_REQ_TEMPLATE = "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"aad\": \"%s\", \"applicationId\": \"%s\", \"data\": \"%s\", \"referenceId\": \"%s\", \"salt\": \"%s\", \"timeStamp\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	/** The Constant SYM_ALGORITHM. */
	private final String SYM_ALGORITHM = "AES";

	/** The Constant SYM_ALGORITHM_LENGTH. */
	private final int SYM_ALGORITHM_LENGTH = 256;

	private static BouncyCastleProvider bouncyCastleProvider;

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@Autowired
	public CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	@Autowired
    private Environment env;
	
	static {
		bouncyCastleProvider = init();
	}
	
	private static BouncyCastleProvider init() {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		return provider;
	}

//	public static String getTimestamp() {
//		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
//    	return formatter.format(ZonedDateTime.now());
//    	
////		LocalDateTime localDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
////		return localDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//	}
	
	public String decryptbio(String encSessionKey, String encData, String timestamp, String transactionId, String authToken) {
		   try {
		      timestamp = timestamp.trim();
		      byte[] xorResult = getXOR(timestamp, transactionId);
		      byte[] aadBytes = getLastBytes(xorResult, 16);
		      byte[] ivBytes = getLastBytes(xorResult, 12);
		      String data = CryptoUtil.encodeBase64(CryptoUtil.combineByteArray(CryptoUtil.decodeBase64(encData),
		            CryptoUtil.decodeBase64(encSessionKey), KEY_SPLITTER));
		      OkHttpClient client = new OkHttpClient();
		      String requestBody = String.format(DECRYPT_REQ_TEMPLATE,
		            CryptoUtil.encodeBase64(aadBytes),
		            IDA,
		            data,
		            IDA_FIR,
		            CryptoUtil.encodeBase64(ivBytes),
		            DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()),
		            DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
		      MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		      RequestBody body = RequestBody.create(mediaType, requestBody);
		      Request request = new Request.Builder()
		            .header("cookie", "Authorization="+authToken)
		            .url( env.getProperty("ida.decrypt.url"))
		            .post(body)
		            .build();
		      Response response = client.newCall(request).execute();
		      System.out.println("successful response >>> " + response.body().string());
		      if(response.isSuccessful()) {
					return response.body().toString();
				}
		   } catch(Exception ex) {
		      logger.error("Error decrypting transactionId : {} ", transactionId, ex);
		   }
		return null;
		}
	
	public SecretKey getSymmetricKey() throws NoSuchAlgorithmException {
		javax.crypto.KeyGenerator generator = KeyGenerator.getInstance("AES", bouncyCastleProvider);
		SecureRandom random = new SecureRandom();
		generator.init(256, random);
		return generator.generateKey();
	}


	public byte[] symmetricEncrypt(byte[] data, SecretKey secretKey)
			throws Exception {
		return cryptoCore.symmetricEncrypt(secretKey, data, null);

	}


	public byte[] symmetricDecrypt(SecretKey secretKey, byte[] encryptedDataByteArr) throws Exception {
		return cryptoCore.symmetricDecrypt(secretKey, encryptedDataByteArr, null);
	}

	private BouncyCastleProvider addProvider() {
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
		Security.addProvider(bouncyCastleProvider);
		return bouncyCastleProvider;
	}


	public SecretKey genSecKey() throws Exception {
		KeyGenerator keyGen;
		SecretKey secretKey = null;
		keyGen = KeyGenerator.getInstance(SYM_ALGORITHM, bouncyCastleProvider);
		keyGen.init(SYM_ALGORITHM_LENGTH, new SecureRandom());
		secretKey = keyGen.generateKey();
		return secretKey;
	}


	public byte[] asymmetricEncrypt(byte[] data, PublicKey publicKey) throws Exception {
		return cryptoCore.asymmetricEncrypt(publicKey, data);
	}


	public byte[] decodeBase64(String data) {
		return java.util.Base64.getDecoder().decode(data);
	}

	// Function to insert n 0s in the
	// beginning of the given string
	static byte[] prependZeros(byte[] str, int n) {
		byte[] newBytes = new byte[str.length + n];
		int i = 0;
		for (; i < n; i++) {
			newBytes[i] = 0;
		}

		for(int j = 0;i < newBytes.length; i++, j++) {
			newBytes[i] = str[j];
		}

		return newBytes;
	}

	// Function to return the XOR
	// of the given strings
	private static byte[] getXOR(String a, String b) {
		byte[] aBytes = a.getBytes();
		byte[] bBytes = b.getBytes();
		// Lengths of the given strings
		int aLen = aBytes.length;
		int bLen = bBytes.length;
		// Make both the strings of equal lengths
		// by inserting 0s in the beginning
		if (aLen > bLen) {
			bBytes = prependZeros(bBytes, aLen - bLen);
		} else if (bLen > aLen) {
			aBytes = prependZeros(aBytes, bLen - aLen);
		}
		// Updated length
		int len = Math.max(aLen, bLen);
		byte[] xorBytes = new byte[len];

		// To store the resultant XOR
		for (int i = 0; i < len; i++) {
			xorBytes[i] = (byte)(aBytes[i] ^ bBytes[i]);
		}
		return xorBytes;
	}

	private static byte[] getLastBytes(byte[] xorBytes, int lastBytesNum) {
		assert(xorBytes.length >= lastBytesNum);
		return java.util.Arrays.copyOfRange(xorBytes, xorBytes.length - lastBytesNum, xorBytes.length);
	}

}
