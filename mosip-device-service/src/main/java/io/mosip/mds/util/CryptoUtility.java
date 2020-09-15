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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CryptoUtility {

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

	static {
		bouncyCastleProvider = init();
	}
	
	private static BouncyCastleProvider init() {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		return provider;
	}

	public static String getTimestamp() {
		LocalDateTime localDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
		return localDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
	
	public Map<String, String>  encrypt(PublicKey publicKey, String data) {
		Map<String, String> result = new HashMap<>();
		try {	
			String timestamp = getTimestamp();
			
			byte[] aadBytes = timestamp.substring(timestamp.length() - 16).getBytes();
			byte[] ivBytes = timestamp.substring(timestamp.length() - 12).getBytes();
			byte[] dataBytes = data.getBytes();
		
			SecretKey secretKey = getSymmetricKey();
			final byte[] encryptedData = cryptoCore.symmetricEncrypt(secretKey, dataBytes, ivBytes, aadBytes);
			final byte[] encryptedSymmetricKey =  cryptoCore.asymmetricEncrypt(publicKey, secretKey.getEncoded());
					
			result.put("ENC_SESSION_KEY", java.util.Base64.getUrlEncoder().encodeToString(encryptedSymmetricKey));
			result.put("ENC_DATA", java.util.Base64.getUrlEncoder().encodeToString(encryptedData));
			result.put("TIMESTAMP", timestamp);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public String decrypt(PrivateKey privateKey, String sessionKey, String data, String timestamp,
						  String transactionId) {
		try {
			//TODO - XOR logic
			timestamp = timestamp.trim();
			byte[] aadBytes = timestamp.substring(timestamp.length() - 16).getBytes();
			byte[] ivBytes = timestamp.substring(timestamp.length() - 12).getBytes();
			
			byte[] decodedSessionKey =  java.util.Base64.getUrlDecoder().decode(sessionKey);		
			final byte[] symmetricKey = cryptoCore.asymmetricDecrypt(privateKey, decodedSessionKey);
			SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");
			
			byte[] decodedData =  java.util.Base64.getUrlDecoder().decode(data);
			final byte[] decryptedData = cryptoCore.symmetricDecrypt(secretKeySpec, decodedData, ivBytes, aadBytes);
			return new String(decryptedData);
			
		} catch(Exception ex) {
			ex.printStackTrace();
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

}
