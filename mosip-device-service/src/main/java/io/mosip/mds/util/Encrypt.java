package io.mosip.mds.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.stereotype.Component;

/**
 * The Class Encrypt is used to encrypt the identity block using Kernel Api.
 *
 * @author Dinesh Karuppiah
 */

@Component
public class Encrypt {

	/** The Constant ASYMMETRIC_ALGORITHM. */
	private static final String SSL = "SSL";

	/**
	 * The Constant UNQUESTIONING_TRUST_MANAGER nullifies the check for certificates
	 * for SSL Connection
	 */
	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String arg1)
				throws CertificateException {
		}
	} };

	/**
	 * Turns off the ssl checking.
	 *
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws KeyManagementException   the key management exception
	 */
	public static void turnOffSslChecking() throws NoSuchAlgorithmException, KeyManagementException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance(Encrypt.SSL);
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	

}
