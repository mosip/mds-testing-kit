package io.mosip.mds.authentication.dto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.binary.Base64;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;


/**
 * The Class CryptoUtility is used for encryption and decryption.
 *
 * The Class CryptoUtil.
 */
@Component
public class CryptoUtility {

	/** The Constant SYM_ALGORITHM. */
	private static final String SYM_ALGORITHM = "AES";
	
	/** The Constant SYM_ALGORITHM_LENGTH. */
	private static final int SYM_ALGORITHM_LENGTH = 256;

	/** The bouncy castle provider. */
	private static BouncyCastleProvider bouncyCastleProvider;

	static {
		bouncyCastleProvider = addProvider();
	}

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */

	public CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore=new CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String>() {
		
		@Override
		public boolean verifySignature(byte[] data, String signature, PublicKey publicKey) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean verifySignature(String signature) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public byte[] symmetricEncrypt(SecretKey key, byte[] data, byte[] iv, byte[] aad) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public byte[] symmetricEncrypt(SecretKey key, byte[] data, byte[] aad) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public byte[] symmetricDecrypt(SecretKey key, byte[] data, byte[] iv, byte[] aad) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public byte[] symmetricDecrypt(SecretKey key, byte[] data, byte[] aad) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String sign(byte[] data, PrivateKey privateKey, X509Certificate x509Certificate) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String sign(byte[] data, PrivateKey privateKey) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public <U> U random() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String hash(byte[] data, byte[] salt) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public byte[] asymmetricEncrypt(PublicKey key, byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public byte[] asymmetricDecrypt(PrivateKey key, byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	


	/**
	 * Symmetric encrypt.
	 *
	 * @param data the data
	 * @param secretKey the secret key
	 * @return the byte[]
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws InvalidAlgorithmParameterException the invalid algorithm parameter exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public byte[] symmetricEncrypt(byte[] data, SecretKey secretKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return cryptoCore.symmetricEncrypt(secretKey, data, null);

	}
	
	
	
	/**
	 * Symmetric decrypt.
	 *
	 * @param secretKey the secret key
	 * @param encryptedDataByteArr the encrypted data byte arr
	 * @return the byte[]
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws InvalidAlgorithmParameterException the invalid algorithm parameter exception
	 */
	public byte[] symmetricDecrypt(SecretKey secretKey, byte[] encryptedDataByteArr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		return cryptoCore.symmetricDecrypt(secretKey, encryptedDataByteArr, null);
	}

	/**
	 * Adds the provider.
	 *
	 * @return the bouncy castle provider
	 */
	private static BouncyCastleProvider addProvider() {
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
		Security.addProvider(bouncyCastleProvider);
		return bouncyCastleProvider;
	}

	/**
	 * Gen sec key.
	 *
	 * @return the secret key
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public SecretKey genSecKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen;
		SecretKey secretKey = null;
        keyGen = KeyGenerator.getInstance(CryptoUtility.SYM_ALGORITHM, bouncyCastleProvider);
		keyGen.init(CryptoUtility.SYM_ALGORITHM_LENGTH, new SecureRandom());
		secretKey = keyGen.generateKey();
        return secretKey;

	}

	/**
	 * Asymmetric encrypt.
	 *
	 * @param data the data
	 * @param publicKey the public key
	 * @return the byte[]
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public byte[] asymmetricEncrypt(byte[] data, PublicKey publicKey) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return cryptoCore.asymmetricEncrypt(publicKey, data);
	}
	
	/**
	 * Decodes from BASE64
	 * 
	 * @param data data to decode
	 * @return decoded data
	 */
	public byte[] decodeBase64(String data) {
		return Base64.decodeBase64(data);
	}

}