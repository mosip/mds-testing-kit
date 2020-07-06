package io.mosip.mds.entitiy;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestReport;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.getresponse.BiometricTypeDto;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.getresponse.UIInput;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.service.IMDSRequestBuilder;
import io.mosip.mds.service.IMDSResponseProcessor;
import io.mosip.mds.service.MDS_0_9_2_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_ResponseProcessor;
import io.mosip.mds.util.Intent;
import io.mosip.mds.validator.CoinTossValidator;
import io.mosip.mds.validator.Validator;
import lombok.Data;

@Entity
@Data
@Table(name ="test_manager")
public class TestManager {
	
	private static ObjectMapper mapper;
	
	static {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Id
	@Column(name = "run_id")
	public String runId;
	
	@Column(name = "mds_spec_version")
	public String mdsSpecVersion;
	
	
	public String process;

	@Column(name = "biometric_type")
	public String biometricType;

	@Column(name = "device_type")
	public String deviceType;

	public List<String> tests;

	//private static List<TestExtnDto> allTests = null;

	private static HashMap<String, TestExtnDto> allTests = new HashMap<>();

	private static Boolean isMasterDataLoaded = false;
	private static Boolean areTestsLoaded = false;
	private static Boolean areRunsLoaded = false;

	private static HashMap<String, TestRun> testRuns = new HashMap<>();

	private static List<String> processList = new ArrayList<String>();

	private static List<BiometricTypeDto> biometricTypes = new ArrayList<BiometricTypeDto>();

	private static List<String> mdsSpecVersions = new ArrayList<String>();

	public TestManager()
	{
		InitStaticData();
	}

	private static void InitStaticData()
	{
		SetupMasterData();
		LoadTests();
		LoadRuns();
	}

	private static void LoadRuns()
	{
		if(areRunsLoaded)
			return;
		List<String> users = Store.GetUsers();
		for(String user:users)
		{
			List<String> runIds = Store.GetRunIds(user);
			for(String runId:runIds)
			{
				TestRun run = Store.GetRun(user, runId);
				testRuns.put(runId, run);
			}
		}
		areRunsLoaded = true;
	}


	private static void LoadTests()
	{
		// TODO Load Tests from file or db and comment out the below lines
		if(!areTestsLoaded)
		{
			TestExtnDto[] tests = Store.GetTestDefinitions();
			if(tests == null)
			{
				tests = LoadTestsFromMemory();
			}
			for(TestExtnDto test:tests)
			{
				allTests.put(test.testId, test);
			}
		}
		areTestsLoaded = true;
	}

	private static void SetupMasterData()
	{
		if(!isMasterDataLoaded)
		{
			MasterDataResponseDto masterData = Store.GetMasterData();
			processList = masterData.process;
			mdsSpecVersions = masterData.mdsSpecificationVersion;
			biometricTypes = masterData.biometricType;
			isMasterDataLoaded = true;
		}
	}

	private static TestExtnDto[] LoadTestsFromMemory()
	{
		// Add test 1
		List<TestExtnDto> memTests = new ArrayList<TestExtnDto>();
		TestExtnDto test1 = new TestExtnDto();
		test1.testId = "discover";
		test1.processes = Arrays.asList("REGISTRATION", "AUTHENTICATION");
		test1.biometricTypes = Arrays.asList("FINGERPRINT");
		test1.deviceTypes = Arrays.asList("SLAP", "FINGER");
		test1.uiInput = Arrays.asList(new UIInput("port","numeric"));
		test1.validators = Arrays.asList(new CoinTossValidator());
		memTests.add(test1);

		// Add test 2
		TestExtnDto test2 = new TestExtnDto();
		test2.testId = "deviceinfo";
		test2.processes = Arrays.asList("REGISTRATION");
		test2.biometricTypes = Arrays.asList("FINGERPRINT");
		test2.deviceTypes = Arrays.asList("SLAP");
		test2.validators = Arrays.asList(new CoinTossValidator());
		memTests.add(test2);

		// Add test 3
		TestExtnDto test3 = new TestExtnDto();
		test3.testId = "capture";
		test3.processes = Arrays.asList("REGISTRATION", "AUTHENTICATION");
		test3.biometricTypes = Arrays.asList("FINGERPRINT");
		test3.deviceTypes = Arrays.asList("SLAP", "FINGER");
		test3.validators = Arrays.asList(new CoinTossValidator());
		memTests.add(test3);
		
		// Add test 4
//		TestExtnDto test4 = new TestExtnDto();
//		test4.testId = "stream";
//		memTests.add(test4);
//
//		// Add test 5
//		TestExtnDto test5 = new TestExtnDto();
//		test5.testId = "rcapture";
//		memTests.add(test5);

		return memTests.toArray(new TestExtnDto[memTests.size()]);
	}



	private static List<TestExtnDto> FilterTests(TestManagerGetDto filter)
	{
		List<TestExtnDto> results =  allTests.values().stream().filter(test -> 
				(isValid(test.processes) && test.processes.contains(filter.process)) && 
				(isValid(test.biometricTypes) && test.biometricTypes.contains(filter.biometricType)) &&
				(isValid(test.deviceTypes) && test.deviceTypes.contains(filter.deviceType)) && 
				( !isValid(test.mdsSpecVersions) || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion ) ))
		.collect(Collectors.toList());

		return results;	
	}
	
	private static boolean isValid(List<String> value) {
		return (value != null && !value.isEmpty());
	}

	private void saveRun(RunExtnDto newRun, TestManagerDto targetProfile)
	{
		// TODO save the Run to file as well as memory
		if(testRuns.keySet().contains(newRun.runId))
			return;
		TestRun newTestRun = new TestRun();
		newTestRun.targetProfile = targetProfile;
		newTestRun.runId = newRun.runId;
		newTestRun.runName = newRun.runName;
		newTestRun.createdOn = new Date();
		newTestRun.runStatus = RunStatus.Created;
		newTestRun.tests = new ArrayList<>();
		newTestRun.user = newRun.email;
		Collections.addAll(newTestRun.tests, newRun.tests);
		TestRun savedRun = persistRun(newTestRun);
		testRuns.put(savedRun.runId, savedRun);
	}

	private TestRun persistRun(TestRun run)
	{
		return Store.saveTestRun(run.user, run);
	}

	public MasterDataResponseDto getMasterData()
	{
		MasterDataResponseDto masterData = new MasterDataResponseDto();
		SetupMasterData();
		masterData.biometricType.addAll(biometricTypes);
		masterData.process.addAll(processList);
		masterData.mdsSpecificationVersion.addAll(mdsSpecVersions);
		return masterData;
	}

	public TestExtnDto[] GetTests(TestManagerGetDto filter)
	{
		LoadTests();
		return FilterTests(filter).toArray(new TestExtnDto[0]);
	}

	public RunExtnDto createRun(TestManagerDto runInfo)
	{
		RunExtnDto newRun = new RunExtnDto();
		// Validate the tests given
		Boolean notFound = false;
		for(String testName:runInfo.tests)
		{
			if(!allTests.keySet().contains(testName))
			{
				notFound = true;
				break;
			}
		}
		if(notFound)
			return null;
		// Assign a Run Id
		newRun.runId = "" + System.currentTimeMillis();
		newRun.runName = runInfo.runName;
		// Save the run details
		newRun.tests = runInfo.tests.toArray(new String[runInfo.tests.size()]);
		if(!runInfo.email.isEmpty())
			newRun.email = runInfo.email;
		else
		newRun.email = "misc";
		saveRun(newRun, runInfo);
		return newRun;
	}

	public TestReport GetReport(String runId)
	{
		if(!testRuns.keySet().contains(runId))
			return null;
		return new TestReport(testRuns.get(runId));
	}


	//GENERATING REPORT IN PDF FORMAT
	public HttpEntity<byte[]> GetPdfReport(String runId,String fileName) throws Exception {

//		return ;
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());

		ve.init();

		Template t = ve.getTemplate("templates/testReport.vm");

		VelocityContext context = new VelocityContext();
		if(!testRuns.keySet().contains(runId))
			context.put("testReport", "");
		else{
			context.put("testReport", (new TestReport(testRuns.get(runId))).toString());
		}
		context.put("genDateTime", LocalDateTime.now().toString());
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		System.out.println(writer.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos = generatePdf(writer.toString());
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_PDF);

		header.set(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + fileName.replace(" ", "_"));
		header.setContentLength(baos.toByteArray().length);

		return new HttpEntity<byte[]>(baos.toByteArray(), header);
	}

	public ByteArrayOutputStream generatePdf(String html) {
		PdfWriter pdfWriter = null;
		Document document = new Document();
		try {

			document = new Document();
			// document header attributes
			document.addAuthor("Shubam");
			document.addAuthor("Shubam Gupta");
			document.addCreationDate();
			document.addProducer();
			document.addCreator("github.com/shubamgupta2509/");
			document.addTitle("TEST_REPORT ");
			document.setPageSize(PageSize.LETTER);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);

			// open document
			document.open();

			XMLWorkerHelper xmlWorkerHelper = XMLWorkerHelper.getInstance();
			xmlWorkerHelper.getDefaultCssResolver(true);
			xmlWorkerHelper.parseXHtml(pdfWriter, document, new StringReader(
					html));
			// close the document
			document.close();
			System.out.println("PDF generated successfully");

			return baos;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<TestRun> filterRuns(String email)
	{
		List<TestRun> runs = testRuns.values().stream().filter(test -> test.user.equals(email)).collect(Collectors.toList());
		
		if(runs == null)
			runs = new ArrayList<>();
		
		return runs;
	}

	public List<TestRun> getRuns(String email)
	{
		return filterRuns(email);
	}

	private IMDSRequestBuilder getRequestBuilder(List<String> version)
	{
		// Order of comparison is from newer to older. Default is the latest
		//if(version == null || version.size() == 0)
		//	return new MDS_0_9_5_RequestBuilder();	
		if(version.contains("0.9.5"))
			return new MDS_0_9_5_RequestBuilder();
		if(version.contains("0.9.2"))
			return new MDS_0_9_2_RequestBuilder();
		return new MDS_0_9_5_RequestBuilder();
	}

	private ComposeRequestResponseDto buildRequest(ComposeRequestDto requestParams)
	{
		DeviceInfoResponse  response = requestParams.deviceInfo.getDeviceInfo();
		String specVersion = response.specVersion[0];
		IMDSRequestBuilder builder = getRequestBuilder(Arrays.asList(specVersion));
		
		TestRun run = testRuns.get(requestParams.runId);
		if(run == null || !run.tests.contains(requestParams.testId))
			return null;

		run.deviceInfo = response;
		// Overwrites the device info for every call

		persistRun(run);

		TestExtnDto test = allTests.get(requestParams.testId);
		
		return builder.buildRequest(run, test, requestParams.deviceInfo, getIntent(test.method));
		
	}

	public ComposeRequestResponseDto composeRequest(ComposeRequestDto composeRequestDto) {

		// TODO create and use actual request composers		
		return buildRequest(composeRequestDto);
	}

	public TestResult validateResponse(ValidateResponseRequestDto validateRequestDto) {
		if(!testRuns.keySet().contains(validateRequestDto.runId) || !allTests.keySet().contains(validateRequestDto.testId))
			return null;
		TestRun run = testRuns.get(validateRequestDto.runId);
		TestExtnDto test = allTests.get(validateRequestDto.testId);
		TestResult testResult = new TestResult();
		testResult.executedOn = new Date();
		testResult.requestData = validateRequestDto.mdsRequest;
		testResult.responseData = validateRequestDto.mdsResponse;
		testResult.runId = run.runId;
		testResult.testId = test.testId;
		
		Intent intent = getIntent(test.method);
		
		IMDSResponseProcessor responseProcessor = getResponseProcessor(run.targetProfile.mdsSpecVersion);
		
		validateRequestDto.captureResponse = responseProcessor.getCaptureResponse(intent, testResult.responseData);
		
		for(Validator v:test.validators)
		{
			testResult.validationResults.add(v.Validate(validateRequestDto));
		}
		
		testResult.renderContent = responseProcessor.getRenderContent(intent, testResult.responseData);
		run.runStatus = RunStatus.InProgress;
		// TODO when should this status be Done
		run.testReport.put(test.testId, testResult);
		persistRun(run);
		return testResult; 
	}
	
	private IMDSResponseProcessor getResponseProcessor(String version)
	{
		if(version.contains("0.9.5"))
			return new MDS_0_9_5_ResponseProcessor();
		
		return new MDS_0_9_5_ResponseProcessor();
	}
	
	private Intent getIntent(String method) {
		Intent intent = Intent.Discover;

		if(method.equals("deviceinfo"))
		{
			intent = Intent.DeviceInfo;
		}
		else if(method.equals("rcapture"))
		{
			intent = Intent.RegistrationCapture;
		}
		else if(method.equals("capture"))
		{
			intent = Intent.Capture;
		}
		else if(method.equals("stream"))
		{
			intent = Intent.Stream;
		}
		return intent;
	}

	public DiscoverResponse[] decodeDiscoverInfo(String discoverInfo) {
		return DiscoverHelper.decode(discoverInfo);
	}

	public DeviceInfoResponse[] decodeDeviceInfo(String deviceInfo) {
		return DeviceInfoHelper.decode(deviceInfo);
	}
}
