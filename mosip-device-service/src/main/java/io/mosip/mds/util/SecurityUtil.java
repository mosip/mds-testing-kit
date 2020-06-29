package io.mosip.mds.util;

import java.util.Base64;



public class SecurityUtil {
	
	public static byte[] getPayload(String data) {		
		try {
			/*JsonWebSignature jws = new JsonWebSignature();
			
			FileReader certreader = new FileReader("MosipTestCert.pem");
			PemReader certpemReader = new PemReader(certreader);
			final byte[] certpemContent = certpemReader.readPemObject().getContent();
			certpemReader.close();	   
		    EncodedKeySpec certspec = new X509EncodedKeySpec(certpemContent);
		    CertificateFactory cf = CertificateFactory.getInstance("X.509");
		    X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certspec.getEncoded()));
		    PublicKey publicKey = certificate.getPublicKey();
		    
		    jws.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST,   AlgorithmIdentifiers.RSA_USING_SHA256));
		    jws.setCompactSerialization(data);		    
		    jws.setKey(publicKey);
		    
		    System.out.println("JWS validation >>> " + jws.verifySignature());
			*/
			
			String [] parts = data.split("\\.");
			if(parts.length == 3) {
				return Base64.getUrlDecoder().decode(parts[1]); 
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO
		return null;
	}

}
