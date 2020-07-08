package io.mosip.mds.dto;

import lombok.Data;

@Data
public class CaptureResponse extends MdsResponse{
	@Data
	public static class CaptureBiometricData {
		public String digitalId;
		public DigitalId digitalIdDecoded;
		public String deviceCode;
		public String deviceServiceVersion; // Service version
		public String bioType; // Finger
		public String bioSubType; // UNKNOWN
		public String purpose; // Auth or Registration
		public String env; // Target environment
		public String domainUri; // URI of the auth server
		public String bioValue; // Encrypted with session key and base64urlencoded biometric data
		public String transactionId; // Unique transaction id
		public String timestamp; // ISO format datetime with time zone
		public float requestedScore; // Floating point number to represent the minimum required score for the capture
		public float qualityScore; // Floating point number representing the score for the current capture
	}

	@Data
	public static class CaptureBiometric {
		public String specVersion;
		public String hash;
		public String sessionKey;
		public String thumbprint;
		public MDSError error;
		public CaptureBiometricData dataDecoded;
		public String data;
	}

	public String analysisError = "";
	public CaptureBiometric[] biometrics;

	/*
	 * 
	 * { "biometrics": [ { "specVersion": "MDS spec version", "data": { "digitalId":
	 * "digital Id as described in this document", "deviceCode":
	 * "A unique code given by MOSIP after successful registration",
	 * "deviceServiceVersion": "Service version", "bioType": "Finger", "bioSubType":
	 * "UNKNOWN", "purpose": "Auth  or Registration", "env": "Target environment",
	 * "domainUri": "URI of the auth server", "bioValue":
	 * "Encrypted with session key and base64urlencoded biometric data",
	 * "transactionId": "Unique transaction id", "timestamp":
	 * "ISO format datetime with time zone", "requestedScore":
	 * "Floating point number to represent the minimum required score for the capture"
	 * , "qualityScore":
	 * "Floating point number representing the score for the current capture" },
	 * "hash":
	 * "sha256(sha256 hash in hex format of the previous data block + sha256 hash in hex format of the current data block before encryption)"
	 * , "sessionKey":
	 * "encrypted with MOSIP public key (dynamically selected based on the uri) and encoded session key biometric"
	 * , "thumbprint":
	 * "SHA256 representation of thumbprint of the certificate that was used for encryption of session key. All texts to be treated as uppercase without any spaces or hyphens"
	 * , "error": { "errorcode": "101", "errorinfo": "Invalid JSON Value" } }, {
	 * "specVersion" : "MDS spec version", "data": { "digitalId":
	 * "Digital Id as described in this document", "deviceCode":
	 * "A unique code given by MOSIP after successful registration",
	 * "deviceServiceVersion": "Service version", "bioType": "Finger", "bioSubType":
	 * "Left IndexFinger", "purpose": "Auth  or Registration", "env":
	 * "target environment", "domainUri": "uri of the auth server", "bioValue":
	 * "encrypted with session key and base64urlencoded biometric data",
	 * "transactionId": "unique transaction id", "timestamp":
	 * "ISO Format date time with timezone", "requestedScore":
	 * "Floating point number to represent the minimum required score for the capture"
	 * , "qualityScore":
	 * "Floating point number representing the score for the current capture" },
	 * "hash":
	 * "sha256(sha256 hash in hex format of the previous data block + sha256 hash in hex format of the current data block before encryption)"
	 * , "sessionKey":
	 * "encrypted with MOSIP public key and encoded session key biometric",
	 * "thumbprint":
	 * "SHA256 representation of thumbprint of the certificate that was used for encryption of session key. All texts to be treated as uppercase without any spaces or hyphens"
	 * , "error": { "errorcode": "101", "errorinfo": "Invalid JSON Value" } } ] }
	 * 
	 */

}