package io.mosip.mds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.entitiy.TestManager;
import io.mosip.mds.service.TestCaseResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@CrossOrigin("*")
@RestController
@RequestMapping("/testrunner")
@Api(tags = { "TestRunner" })
public class TestRunnerController {
/*	@Autowired
	TestCaseResultService testCaseResultService;
*/
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
		/*TestRun testRun=testManager.validateResponse(validateRequestDto);
		testCaseResultService.saveTestResult(testRun);*/
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
	
	
/*	@GetMapping("/getRunStatus/{runId}")
	@ApiOperation(value = "Service to get RunStatus", notes = "Gets status of current run Id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When status of the run id is successfully returned"),
			@ApiResponse(code = 400, message = "When request does not have a run Id"),
			@ApiResponse(code = 500, message = "While fetching runStatus any error occured") })
	public boolean getRunStatus(@PathVariable("runId") String runId) {
		return	testCaseResultService.getRunStatus(runId);
	 	
	}*/


}
