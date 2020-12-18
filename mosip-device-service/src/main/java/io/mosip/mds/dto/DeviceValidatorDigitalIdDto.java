package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceValidatorDigitalIdDto {

	String dateTime;
	String deviceSubType;
	String dp;
	String dpId;
	String make;
	String model;
	String serialNo;
	String type;
}

/*
{
	  "id": "string",
	  "metadata": {},
	  "request": {
	    "deviceCode": "d697add7-ab53-4c1a-8c55-d13ea4f1e9ae",
	    "deviceServiceVersion": "string",
	    "digitalId": {
	      "dateTime": "2020-10-05T04:46:18.242Z",
	      "deviceSubType": "Slab",
	      "dp": "Nprime",
	      "dpId": "Nprime",
	      "make": "MIAXIS",
	      "model": "SM-91M",
	      "serialNo": "TR001234567",
	      "type": "Finger"
	    },
	    "purpose": "Auth",
	    "timeStamp": "2020-12-09T12:06:23.921Z"
	  },
	  "requesttime": "2020-12-09T10:45:08.736Z",
	  "version": "0.1"
	}
*/