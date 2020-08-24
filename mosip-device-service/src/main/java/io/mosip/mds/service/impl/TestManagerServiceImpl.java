package io.mosip.mds.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestReport;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.service.TestManagerService;
import io.mosip.mds.util.TestServiceUtil;

@Service
public class TestManagerServiceImpl implements TestManagerService {

	@Autowired
	TestServiceUtil testServiceUtil;

	@Autowired
	Store store;
	
	//	@Override
	//	public RunExtnDto createRun(TestManagerDto testManagerDto) {
	//		RunExtnDto runExtnDto=new RunExtnDto();
	//		TestManager testManager=new TestManager();
	//		if(testManagerDto!=null) {
	//
	//			testManager.biometricType = testManagerDto.biometricType;
	//			testManager.deviceSubType = testManagerDto.deviceSubType;
	//			testManager.mdsSpecVersion = testManager.mdsSpecVersion;
	//			testManager.tests = testManagerDto.tests;
	//			testManager.process = testManagerDto.process;
	//
	//			//testManager=testManagerRepository.save(testManager);
	//		}
	//		runExtnDto.runId = testManager.runId;
	//		return runExtnDto;
	//	}

	@Override
	public TestExtnDto getTest(TestManagerGetDto testManagerGetDto) {
		// TODO q`-generated method stub
		return null;
	}

	@Override
	public MasterDataResponseDto getMasterData()
	{
		MasterDataResponseDto masterData = new MasterDataResponseDto();
		testServiceUtil.SetupMasterData();
		masterData.biometricType.addAll(testServiceUtil.getBiometricTypes());
		masterData.process.addAll(testServiceUtil.getProcessList());
		masterData.mdsSpecificationVersion.addAll(testServiceUtil.getMdsSpecVersions());
		return masterData;
	}

	@Override
	public List<TestRun> getRuns(String email)
	{
		testServiceUtil.SetupMasterData();
		testServiceUtil.loadTests();
		testServiceUtil.loadRuns();
		return filterRuns(email);
	}

	private List<TestRun> filterRuns(String email)
	{
		List<TestRun> runs = testServiceUtil.getTestRuns().values().stream().filter(test -> test.user.equals(email)).collect(Collectors.toList());

		if(runs == null)
			runs = new ArrayList<>();

		runs.sort((run1, run2) -> run1.createdOn.compareTo(run2.createdOn));

		return runs;
	}

	@Override
	public RunExtnDto createRun(TestManagerDto runInfo)
	{
		RunExtnDto newRun = new RunExtnDto();
		// Validate the tests given
		Boolean notFound = false;
		for(String testName:runInfo.tests)
		{
			if(!testServiceUtil.getAllTests().containsKey(testName))
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

	private void saveRun(RunExtnDto newRun, TestManagerDto targetProfile)
	{
		if(testServiceUtil.getTestRuns().containsKey(newRun.runId))
			return;

		TestRun newTestRun = new TestRun();
		newTestRun.targetProfile = targetProfile;
		newTestRun.runId = newRun.runId;
		newTestRun.runName = newRun.runName;
		newTestRun.createdOn = new Date();
		//newTestRun.runStatus = RunStatus.Created;
		newTestRun.tests = new ArrayList<>();
		newTestRun.user = newRun.email;
		Collections.addAll(newTestRun.tests, newRun.tests);
		TestRun savedRun = persistRun(newTestRun);
		testServiceUtil.getTestRuns().put(savedRun.runId, savedRun);
	}

	@Override
	public TestReport getReport(String runId)
	{
		if(!testServiceUtil.getTestRuns().containsKey(runId))
			return null;
		return new TestReport(testServiceUtil.getTestRuns().get(runId));
	}

	private TestRun persistRun(TestRun run)
	{
		boolean isAllInResponseValidationStage = run.testReport.values().stream()
				.allMatch( result -> result.currentState.startsWith("MDS Response Validations"));
		if(isAllInResponseValidationStage)
			//run.runStatus = RunStatus.Done;
			run.runStatus="Done";
		return store.saveTestRun(run.user, run);
	}

	//GENERATING REPORT IN PDF FORMAT
	@Override
	public HttpEntity<byte[]> getPdfReport(String runId,String fileName) throws Exception {

		//		return ;
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());

		ve.init();

		Template t = ve.getTemplate("templates/testReport.vm");

		VelocityContext context = new VelocityContext();
		if(!testServiceUtil.getTestRuns().keySet().contains(runId))
			context.put("testReport", "");
		else{
			context.put("testReport", (new TestReport(testServiceUtil.getTestRuns().get(runId))).toString());
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

	private ByteArrayOutputStream generatePdf(String html) {
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

	@Override
	public TestExtnDto[] getTests(TestManagerGetDto filter)
	{
		testServiceUtil.loadTests();
		return testServiceUtil.filterTests(filter).toArray(new TestExtnDto[0]);
	}
}
