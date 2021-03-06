package io.mosip.mds.controller;

import io.mosip.mds.dto.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.mosip.mds.service.TestRunnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin("*")
@RestController
@RequestMapping("/testrunner")
@Api(tags = { "TestRunner" })
public class TestRunnerController {

	@Autowired
	private TestRunnerService testRunnerService;

	/*@PostMapping("/composerequest")
	@ApiOperation(value = "Service to save composeRequest", notes = "Saves composeRequest and json")
	@ApiResponses({ @ApiResponse(code = 201, message = "When composerequest Details successfully created"),
		@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
		@ApiResponse(code = 500, message = "While creating composerequest any error occured") })
	public ComposeRequestResponseDto composeRequest(@RequestBody ComposeRequestDto composeRequestDto) {
		return testRunnerService.composeRequest(composeRequestDto);
	}*/

	@PostMapping("/getallrequests")
	@ApiOperation(value = "Service to save MDM requests for all test cases", notes = "Saves composeRequest and json")
	@ApiResponses({ @ApiResponse(code = 201, message = "When MDM request Details successfully created"),
		@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
		@ApiResponse(code = 500, message = "While creating MDM request any error occured") })
	public TestRun getAllRequests(@RequestBody ComposeRequestDto composeRequestDto) throws Exception {
		return testRunnerService.composeRequestForAllTests(composeRequestDto);
	}

	@PostMapping("/validateresponse")
	@ApiOperation(value = "Service to save validateResponse", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When validateResponse Details successfully created"),
		@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
		@ApiResponse(code = 500, message = "While creating validateResponse any error occured") })
	public TestRun validateResponse(@RequestBody ValidateResponseRequestDto validateRequestDto) throws Exception {
		return testRunnerService.validateResponse(validateRequestDto);
	}

	@PostMapping("/decodediscover")
	@ApiOperation(value = "Service to extract discover info from ", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When decode is successful"),
		@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
		@ApiResponse(code = 500, message = "While processing discover data any error occured") })
	public DiscoverResponse[] decodeDiscover(@RequestBody String discoverInfo) {
		// TODO handle null return for invalid runId and testId
		return testRunnerService.decodeDiscoverInfo(discoverInfo);	
	}

	@PostMapping("/decodedeviceinfo")
	@ApiOperation(value = "Service to extract discover info from ", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When decode is successful"),
		@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
		@ApiResponse(code = 500, message = "While processing device info data any error occured") })
	public DeviceInfoResponse[] decodeDeviceInfo(@RequestBody String deviceInfo) {
		// TODO handle null return for invalid runId and testId
		return testRunnerService.decodeDeviceInfo(deviceInfo);	
	}

	@PostMapping("/validateauthrequest")
	@ApiOperation(value = "Service to validate auth request", notes = "Service to validate auth request")
	public String validateAuthRequest(@RequestBody AuthApiRequestDto authRequestDto) {
		// TODO handle null return for invalid runId and testId
		return testRunnerService.validateAuthRequest(authRequestDto.getRunId(), authRequestDto.getTestId());
	}

	@GetMapping("/download/{runId}/{testId}")
	public ResponseEntity<Object> download(@PathVariable("runId")String runId,@PathVariable("testId")String testId) {
		testRunnerService.downloadReport(runId, testId);

		String filename = runId+".pdf";
		File file = new File(filename);
		InputStreamResource resource = null;
		try {
			resource = new InputStreamResource(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		HttpHeaders headers = new HttpHeaders();		        
		headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");		        
		ResponseEntity<Object> 
		responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(
				MediaType.parseMediaType("application/txt")).body(resource);		        
		return responseEntity;		     
	}
}
