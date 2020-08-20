package io.mosip.mds.entitiy;

import io.mosip.mds.dto.MDSError;
import lombok.Data;

@Data
public class DeviceInfoMinimal {
	public String deviceInfo;
	public MDSError error;
}
