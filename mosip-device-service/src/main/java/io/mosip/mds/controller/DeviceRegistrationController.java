package io.mosip.mds.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mds.dto.DeviceDeRegistrationRequest;
import io.mosip.mds.dto.DeviceDeRegistrationResponse;
import io.mosip.mds.dto.DeviceRegistrationRequest;
import io.mosip.mds.dto.DeviceRegistrationResponse;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin("*")
@RestController
@Api(tags = { "DeviceRegistration" })
@RequestMapping("/v1/masterdata")
public class DeviceRegistrationController {
	
	@PostMapping("/registereddevices")
	@ApiOperation(value = "Service to save registration Details", notes = "Saves registration Details and return details")
	@ApiResponses({ @ApiResponse(code = 201, message = "When registration Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating registration any error occured") })
	public DeviceRegistrationResponse registerDevices(@RequestBody DeviceRegistrationRequest deviceRegistrationRequest) {
		return null;
		//return testManager.createRun(testManagerDto);
	}
	
	@PostMapping("/device/deregister")
	@ApiOperation(value = "Service to deregister device details", notes = "deregister device Details and return details")
	@ApiResponses({ @ApiResponse(code = 201, message = "When deregister  successfully done"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While deregister any error occured") })
	public DeviceDeRegistrationResponse deregister(@RequestBody DeviceDeRegistrationRequest deviceDeRegistrationRequest) {
		return null;
		//return testManager.createRun(testManagerDto);
	}
	
	
}
