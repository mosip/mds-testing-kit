package io.mosip.mds.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.Certificate;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.bouncycastle.util.io.pem.PemReader;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;

import io.mosip.kernel.core.util.CryptoUtil;

public class SecurityUtil {
	
	public static byte[] getPayload(String encodedData) throws Exception {		
		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(encodedData);
			/*
			 * List<X509Certificate> certificateChainHeaderValue =
			 * jws.getCertificateChainHeaderValue(); X509Certificate certificate =
			 * certificateChainHeaderValue.get(0); certificate.checkValidity();
			 */
			PublicKey publicKey =  getPublicKey1();
			jws.setKey(publicKey);
			return jws.getPayloadBytes();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to parse and validate Json web signture");
		}	
	}
	
	/*private static PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = Files.readAllBytes(new File("data/config/PublicKey.pem").toPath());
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(keySpec);
	}*/
	
	public static PublicKey getPublicKey1() throws Exception {
		FileReader reader = new FileReader("public.pem");
		PemReader pemReader = new PemReader(reader);		
		final byte[] pemContent = pemReader.readPemObject().getContent();
	    pemReader.close();
	   
	    EncodedKeySpec spec = new X509EncodedKeySpec(pemContent);
	    return KeyFactory.getInstance("RSA").generatePublic(spec);
	}
	
	/*public static PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {		
		FileInputStream pkeyfis = new FileInputStream("data/config/MOSIPCert.pem");
		String pKey = getFileContent(pkeyfis, "UTF-8");
		pKey = trimBeginEnd(pKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pKey));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(keySpec);
	}*/
	
	public static String getFileContent(FileInputStream fis, String encoding) throws IOException {
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
	
	private static String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

}
