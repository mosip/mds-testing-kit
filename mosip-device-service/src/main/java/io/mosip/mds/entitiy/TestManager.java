package io.mosip.mds.entitiy;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import io.mosip.mds.dto.*;
import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.getresponse.BiometricTypeDto;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.getresponse.UIInput;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.service.IMDSRequestBuilder;
import io.mosip.mds.service.MDS_0_9_2_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_RequestBuilder;
import io.mosip.mds.service.IMDSRequestBuilder.Intent;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Entity
@Data
@Table(name ="test_manager")
public class TestManager {

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
		// TODO load master data here from file
		if(!isMasterDataLoaded)
		{
			MasterDataResponseDto masterData = Store.GetMasterData();
			if(masterData != null)
			{
				processList = masterData.process;
				mdsSpecVersions = masterData.mdsSpecificationVersion;
				biometricTypes = masterData.biometricType;
			}
			else
			{
				Collections.addAll(processList, "REGISTRATION", "AUTHENTICATION");
				Collections.addAll(mdsSpecVersions, "0.9.2","0.9.3", "0.9.4", "0.9.5");
				BiometricTypeDto finger = new BiometricTypeDto("FINGERPRINT");
				Collections.addAll(finger.deviceType, "SLAP", "FINGER", "CAMERA");
				Collections.addAll(finger.segments, "LEFT SLAP", "RIGHT SLAP", "TWO THUMBS", "LEFT THUMB", "RIGHT THUMB",
				"LEFT INDEX", "RIGHT INDEX");
				BiometricTypeDto iris = new BiometricTypeDto("IRIS");
				Collections.addAll(iris.deviceType, "MONOCULAR", "BINOCULAR", "CAMERA");
				Collections.addAll(iris.segments, "FULL", "CROPPED");
				BiometricTypeDto face = new BiometricTypeDto("IRIS");
				Collections.addAll(face.deviceType, "STILL", "VIDEO");
				Collections.addAll(face.segments, "BUST", "HEAD");
				Collections.addAll(biometricTypes, finger, iris, face);
			}
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
		List<TestExtnDto> results = new ArrayList<TestExtnDto>();

		for(TestExtnDto test:allTests.values())
		{
			// TODO see if forEach lambda expression can be used		
			if(test.processes.contains(filter.process)
				&& test.biometricTypes.contains(filter.biometricType)
				&& test.deviceTypes.contains(filter.deviceType)
				&& (test.mdsSpecVersions == null || test.mdsSpecVersions.isEmpty() || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion))
			)
				results.add(test);
		}
		return results;	
	}

	private void SaveRun(RunExtnDto newRun, TestManagerDto targetProfile)
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
		TestRun savedRun = PersistRun(newTestRun);
		testRuns.put(savedRun.runId, savedRun);
	}

	private TestRun PersistRun(TestRun run)
	{
		return Store.SaveTestRun(run.user, run);
	}

	public MasterDataResponseDto GetMasterData()
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

	public RunExtnDto CreateRun(TestManagerDto runInfo)
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
		SaveRun(newRun, runInfo);
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

	private TestRun[] FilterRuns(String email)
	{
		// TODO add filter code based on email
		return testRuns.values().toArray(new TestRun[0]);
	}

	public TestRun[] GetRuns(String email)
	{
		return FilterRuns(email);
	}

	private IMDSRequestBuilder GetRequestBuilder(List<String> version)
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

	private ComposeRequestResponseDto BuildRequest(ComposeRequestDto requestParams)
	{
		DeviceInfoResponse response = new DeviceInfoResponse();

		response = DeviceInfoHelper.DecodeDeviceInfo(requestParams.deviceInfo.deviceInfo);
		String specVersion = response.specVersion[0];
		IMDSRequestBuilder builder = GetRequestBuilder(Arrays.asList(specVersion));
		
		TestRun run = testRuns.get(requestParams.runId);
		if(run == null || !run.tests.contains(requestParams.testId))
			return null;

		run.deviceInfo = response;
		// Overwrites the device info for every call

		PersistRun(run);

		TestExtnDto test = allTests.get(requestParams.testId);
		Intent intent = Intent.Discover;

		if(test.method.equals("deviceinfo"))
		{
			intent = Intent.DeviceInfo;
		}
		else if(test.method.equals("rcapture"))
		{
			intent = Intent.RegistrationCapture;
		}
		else if(test.method.equals("capture"))
		{
			intent = Intent.Capture;
		}
		else if(test.method.equals("stream"))
		{
			intent = Intent.Stream;
		}

		return builder.BuildRequest(run, test, requestParams.deviceInfo, intent);
		
	}

	public ComposeRequestResponseDto ComposeRequest(ComposeRequestDto composeRequestDto) {

		// TODO create and use actual request composers		
		return BuildRequest(composeRequestDto);
	}

	public TestResult ValidateResponse(ValidateResponseRequestDto validateRequestDto) {
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
		
		validateRequestDto.captureResponse = getCaptureResponse(test.method, testResult.responseData);
		
		for(Validator v:test.validators)
		{
			testResult.validationResults.add(v.Validate(validateRequestDto));
		}
		
		testResult.renderContent = ProcessResponse(testResult);
		run.runStatus = RunStatus.InProgress;
		// TODO when should this status be Done
		run.testReport.put(test.testId, testResult);
		PersistRun(run);
		return testResult; 
	}
	
	private CaptureResponse getCaptureResponse(String method, String responseData) {
		switch(method) {
		case "capture":
			return CaptureHelper.Decode(responseData,false);
		case "rcapture":
			return CaptureHelper.Decode(responseData,true);
		}
		return null;
	}

	private String ProcessResponse(TestResult testResult)
	{
		String method = allTests.get(testResult.testId).method;
		String renderContent = "";
		switch(method)
		{
			case "capture":
				renderContent += CaptureHelper.Render(CaptureHelper.Decode(testResult.responseData,false));
				break;
			case "rcapture":
				renderContent += CaptureHelper.Render(CaptureHelper.Decode(testResult.responseData,true));
				break;
			case "deviceinfo":
				DeviceInfoResponse[] diResponse = DecodeDeviceInfo(testResult.responseData);
				for (DeviceInfoResponse deviceInfoResponse : diResponse) {
					renderContent += DeviceInfoHelper.Render(deviceInfoResponse) + "<BR/>";
				} 
				renderContent = "";
				break;
			case "discover":
				DiscoverResponse[] dResponse = DecodeDiscoverInfo(testResult.responseData);
				for (DiscoverResponse discoverResponse : dResponse) {
					renderContent += DiscoverHelper.Render(discoverResponse) + "<BR/>";
				} 
				break;
			case "stream":
				renderContent = "<p><u>Stream Output</u></p><img alt=\"stream video feed\" src=\"127.0.0.1:4501/stream\" style=\"height:200;width:200;\">";
				break;
			default:
				renderContent = "<img src=\"https://www.mosip.io/images/logo.png\"/>";
		}
		return renderContent;
	}

	public DiscoverResponse[] DecodeDiscoverInfo(String discoverInfo) {
		return DiscoverHelper.Decode(discoverInfo);
	}

	public DeviceInfoResponse[] DecodeDeviceInfo(String deviceInfo) {
		return DeviceInfoHelper.Decode(deviceInfo);
	}
}
