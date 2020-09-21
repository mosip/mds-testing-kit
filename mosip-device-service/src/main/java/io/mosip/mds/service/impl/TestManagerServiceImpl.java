package io.mosip.mds.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.mds.dto.*;
import io.mosip.mds.entitiy.RunStatus;
import io.mosip.mds.entitiy.TestResultKey;
import io.mosip.mds.entitiy.TestcaseResult;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.service.TestManagerService;

import javax.validation.constraints.NotNull;

@Service
public class TestManagerServiceImpl implements TestManagerService {

	private static  final Logger logger = LoggerFactory.getLogger(TestManagerServiceImpl.class);
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private RunIdStatusRepository runIdStatusRepository;

	@Autowired
	public TestCaseResultRepository testCaseResultRepository;

	@Override
	public MasterDataResponseDto getMasterData()
	{
		MasterDataResponseDto masterData = Store.getMasterData();
		return masterData;
	}

	@Override
	public List<TestDefinition> getTests(TestManagerGetDto filter)
	{
		Map<String, TestDefinition> testDefinitions = Store.getAllTestDefinitions();
		List<TestDefinition> results =  testDefinitions.values().stream().filter(test ->
				(isValid(test.processes) && test.processes.contains(filter.process)) &&
						(isValid(test.biometricTypes) && test.biometricTypes.contains(filter.biometricType)) &&
						(isValid(test.deviceSubTypes) && test.deviceSubTypes.contains(filter.deviceSubType)) &&
						( !isValid(test.mdsSpecVersions) || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion ) ))
				.collect(Collectors.toList());

		return results;
	}

	@Override
	public List<TestRunMetadata> getRuns(@NotNull String email)
	{
		List<TestRunMetadata> list = new ArrayList<>();
		List<io.mosip.mds.entitiy.RunStatus> runs = runIdStatusRepository.findAllByRunOwner(email);
		for (io.mosip.mds.entitiy.RunStatus runStatus : runs) {
			TestRunMetadata testRunMetadata = new TestRunMetadata();
			testRunMetadata.setRunId(runStatus.getRunId());
			testRunMetadata.setRunName(runStatus.getRunName());
			int total = testCaseResultRepository.countByTestResultKeyRunId(runStatus.getRunId());
			int completed = testCaseResultRepository.countByTestResultKeyRunIdAndPassed(runStatus.getRunId(), true);
			String status = (completed == total) ? "Completed (%d/%d)" : "InProgress (%d/%d)";
			testRunMetadata.setRunStatus(String.format(status, completed, total));
			testRunMetadata.setCreatedOn(new Date(Long.valueOf(runStatus.getRunId())));
			list.add(testRunMetadata);
		}
		list.sort((run1, run2) -> run1.getCreatedOn().compareTo(run2.getCreatedOn()));
		return list;
	}


	@Override
	public TestRunMetadata createRun(TestManagerDto testManagerDto) {
		//validate data
		List<TestDefinition> definitions = getTests(new TestManagerGetDto(testManagerDto.mdsSpecVersion, testManagerDto.process,
				testManagerDto.biometricType, testManagerDto.deviceSubType));

		definitions = definitions.stream()
				.filter(testDefinition -> testManagerDto.getTests().contains(testDefinition.getTestId()))
				.collect(Collectors.toList());

		if(definitions == null || definitions.isEmpty()) {
			return null;
		}
		//create entry in runStatus
		io.mosip.mds.entitiy.RunStatus runStatus = saveRunStatus(testManagerDto, definitions);

		logger.info("Runstatus saved >>> {}", runStatus);

		//then create initial entries in testcaseresult table
		saveRunCases(testManagerDto, definitions, runStatus);
		return getTestRunMetadata(runStatus);
	}


	//TODO - validation results must be stored in Db during validations itself
	@Override
	public void saveTestResult(TestRun run) {
		for (Map.Entry<String, TestResult> entry : run.getTestReport().entrySet()) {
			TestResultKey testResultKey = new TestResultKey();
			testResultKey.runId = run.runId;
			testResultKey.testcaseName = entry.getKey();
			Optional<TestcaseResult> result = testCaseResultRepository.findById(testResultKey);
			if (result.isPresent()) {
				TestcaseResult testcaseResult = result.get();
				testcaseResult.setRequest(entry.getValue().getRequestData());
				testcaseResult.setResponse(entry.getValue().getResponseData());
				testcaseResult.setValidationResults(entry.getValue().getValidationResults().toString());
				testcaseResult.setDeviceInfo(run.getDeviceInfo().toString());
				testcaseResult.setPassed(true); //TODO - need to check with kiran
				testCaseResultRepository.save(testcaseResult);
			}
		}

		//TODO - Need to update runStatus entity as well
	}

	@Override
	public TestReport getReport(String runId) {
		//TODO - see below for the old implementation
		//TODO - move this to reporting controller
		return null;
	}

	@Override
	public HttpEntity<byte[]> getPdfReport(String runId, String fileName) throws Exception {
		//TODO - see below for the old implementation
		//TODO - move this to reporting controller
		return null;
	}

	private RunStatus saveRunStatus(TestManagerDto testManagerDto, List<TestDefinition> definitions) {
		RunStatus runStatus = new RunStatus();
		runStatus.setRunId("" + System.currentTimeMillis());
		runStatus.setRunName(testManagerDto.getRunName());
		runStatus.setRunOwner(testManagerDto.getEmail());
		runStatus.setStatus("0/"+definitions.size());
		try {
			runStatus.setProfile(mapper.writeValueAsString(testManagerDto));
		} catch (JsonProcessingException ex) {
			logger.error("Error converting profile to json", ex);
		}
		return runIdStatusRepository.save(runStatus);
	}

	private void saveRunCases(TestManagerDto testManagerDto, List<TestDefinition> definitions,
							  io.mosip.mds.entitiy.RunStatus runStatus) {
		for(TestDefinition testDefinition : definitions) {
			TestResultKey testResultKey = new TestResultKey();
			testResultKey.setRunId(runStatus.getRunId());
			testResultKey.setTestcaseName(testDefinition.getTestId());

			TestcaseResult testCaseResult = new TestcaseResult();
			testCaseResult.setTestResultKey(testResultKey);
			testCaseResult.setDescription(testDefinition.getTestDescription());
			testCaseResult.setOwner(testManagerDto.getEmail());
			testCaseResult.setPassed(false);
			testCaseResultRepository.save(testCaseResult);
		}
	}

	private TestRunMetadata getTestRunMetadata(RunStatus runStatus) {
		TestRunMetadata testRunMetadata = new TestRunMetadata();
		testRunMetadata.setRunId(runStatus.getRunId());
		testRunMetadata.setRunName(runStatus.getRunName());
		testRunMetadata.setRunStatus(runStatus.getStatus()); //TODO
		testRunMetadata.setCreatedOn(new Date(Long.valueOf(runStatus.getRunId()))); //TODO
		return testRunMetadata;
	}

	private boolean isValid(List<String> value) {
		return (value != null && !value.isEmpty());
	}


	//TODO
	/*@Override
	public TestReport getReport(String runId)
	{
		if(!testServiceUtil.getTestRuns().containsKey(runId))
			return null;
		return new TestReport(testServiceUtil.getTestRuns().get(runId));
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
	}*/
}
