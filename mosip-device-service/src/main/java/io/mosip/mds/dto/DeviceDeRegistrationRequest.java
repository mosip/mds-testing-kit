package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceDeRegistrationRequest {
	
	private String id;
	private String version;
	private Device request;
	private String requesttime;

}


//{
//	  "id": "io.mosip.devicederegister",
//	  "version": "de-registration server api version as defined above",
//	  "request": { 
//	    "device": {
//	      "deviceCode": "<device code>",
//	      "env": "<environment>"
//	    }
//	  }
//	  "requesttime": "current timestamp in ISO format"
//	}