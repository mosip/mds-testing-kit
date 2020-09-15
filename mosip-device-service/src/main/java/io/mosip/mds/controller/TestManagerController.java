package io.mosip.mds.controller;

import java.util.List;

import io.mosip.mds.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.service.TestManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin("*")
@RestController
@Api(tags = { "TestManager" })
@RequestMapping("/testmanager")
public class TestManagerController {

	@Autowired
	TestManagerService testManagerService;

	@GetMapping("/masterdata")
	@ApiOperation(value = "Retrieve all MasterData", notes = "Retrieve all MasterData")
	@ApiResponses({
		@ApiResponse(code = 200, message = "When MasterData retrieved from database"),
		@ApiResponse(code = 404, message = "When No MasterData found"),
		@ApiResponse(code = 500, message = "While retrieving MasterData any error occured") })
	public MasterDataResponseDto getMasterData() {
		return testManagerService.getMasterData();
	}

	@PostMapping("/test")
	@ApiOperation(value = "Retrieve Test based on selected profile", notes = "Retrieve Test based on testManagerGetDto")
	@ApiResponses({
		@ApiResponse(code = 200, message = "When Test retrieved"),
		@ApiResponse(code = 404, message = "When No Test found"),
		@ApiResponse(code = 500, message = "While retrieving Test any error occurred") })
	public List<TestDefinition> getTest(@RequestBody TestManagerGetDto testManagerGetDto) {
		return testManagerService.getTests(testManagerGetDto);
	}

	@GetMapping("/runs/{email}")
	@ApiOperation(value = "Retrieve Run metadata based on testManagerGetDto", notes = "Retrieve Test bassed on testManagerGetDto")
	@ApiResponses({
		@ApiResponse(code = 200, message = "When Test retrieved"),
		@ApiResponse(code = 404, message = "When No Test found"),
		@ApiResponse(code = 500, message = "While retrieving Test any error occurred") })
	public List<TestRunMetadata> getRuns(@PathVariable("email")String email) {
		return testManagerService.getRuns(email);
	}


	@PostMapping("/createrun")
	@ApiOperation(value = "Service to save test Details", notes = "Saves test Details and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When test Details successfully created"),
		@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
		@ApiResponse(code = 500, message = "While creating test any error occured") })
	public TestRunMetadata createRun(@RequestBody TestManagerDto testManagerDto) {
		return testManagerService.createRun(testManagerDto);
	}

	@GetMapping("/report/{runId}/{format}")
	@ApiOperation(value = "Retrieve Test Report bassed on runId and format", notes = "Retrieve Test Report bassed on runId and format")
	@ApiResponses({
		@ApiResponse(code = 200, message = "When Test Report retrieved"),
		@ApiResponse(code = 404, message = "When Test Report found"),
		@ApiResponse(code = 500, message = "While retrieving Test Report any error occured") })
	public TestReport getTestReport(@PathVariable("runId")String runId, @PathVariable String format) {
		return testManagerService.getReport(runId);
	}

	@GetMapping("/pdfreport/{runId}")
	@ApiOperation(value = "Retrieve Test Report bassed on runId and format", notes = "Retrieve Test Report bassed on runId and format")
	@ApiResponses({
		@ApiResponse(code = 200, message = "When Test Report retrieved"),
		@ApiResponse(code = 404, message = "When Test Report found"),
		@ApiResponse(code = 500, message = "While retrieving Test Report any error occured") })
	public HttpEntity<byte[]> getpdfTestReport(@PathVariable("runId")String runId) throws Exception {
		return testManagerService.getPdfReport(runId,"report.pdf");

	}

}
