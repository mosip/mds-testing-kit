package io.mosip.mds.controller;

import io.mosip.mds.dto.*;
import org.springframework.web.bind.annotation.*;

import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.entitiy.TestManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@CrossOrigin("*")
@RestController
@RequestMapping("/testrunner")
@Api(tags = { "TestRunner" })
public class TestRunnerController {

	@PostMapping("/composerequest")
	@ApiOperation(value = "Service to save composeRequest", notes = "Saves composeRequest and json")
	@ApiResponses({ @ApiResponse(code = 201, message = "When composerequest Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating composerequest any error occured") })
	public ComposeRequestResponseDto composeRequest(@RequestBody ComposeRequestDto composeRequestDto) {

		TestManager testManager = new TestManager();
		return testManager.composeRequest(composeRequestDto);

		
	}

	@PostMapping("/getallrequests")
	@ApiOperation(value = "Service to save MDM requests for all test cases", notes = "Saves composeRequest and json")
	@ApiResponses({ @ApiResponse(code = 201, message = "When MDM request Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating MDM request any error occured") })
	public TestRun getAllRequests(@RequestBody ComposeRequestDto composeRequestDto) {
		TestManager testManager = new TestManager();
		return testManager.composeRequestForAllTests(composeRequestDto);
	}

	@PostMapping("/validateresponse")
	@ApiOperation(value = "Service to save validateResponse", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When validateResponse Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating validateResponse any error occured") })
	public TestRun validateResponse(@RequestBody ValidateResponseRequestDto validateRequestDto) {
		// TODO handle null return for invalid runId and testId
		TestManager testManager = new TestManager();
		return testManager.validateResponse(validateRequestDto);	
	}

	@PostMapping("/decodediscover")
	@ApiOperation(value = "Service to extract discover info from ", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When decode is successful"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While processing discover data any error occured") })
	public DiscoverResponse[] decodeDiscover(@RequestBody String discoverInfo) {
		// TODO handle null return for invalid runId and testId
		TestManager testManager = new TestManager();
		return testManager.decodeDiscoverInfo(discoverInfo);	
	}

	@PostMapping("/decodedeviceinfo")
	@ApiOperation(value = "Service to extract discover info from ", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When decode is successful"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While processing device info data any error occured") })
	public DeviceInfoResponse[] decodeDeviceInfo(@RequestBody String deviceInfo) {
		// TODO handle null return for invalid runId and testId
		TestManager testManager = new TestManager();
		return testManager.decodeDeviceInfo(deviceInfo);	
	}


}
