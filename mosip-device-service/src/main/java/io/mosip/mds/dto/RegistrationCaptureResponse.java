package io.mosip.mds.dto;

import io.mosip.mds.dto.CaptureResponse.CaptureBiometricData;
import lombok.Data;
@Data
public class RegistrationCaptureResponse extends MdsResponse{

	@Data
	public static class RegistrationCaptureBiometric {
		public String specVersion;
		public String hash;
		public MDSError error;
		public CaptureBiometricData dataDecoded;
		public String data;
	}

	public RegistrationCaptureBiometric[] biometrics;

}