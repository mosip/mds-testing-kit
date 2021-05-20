package io.mosip.mds.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TestRun {

    public enum RunStatus
    {
        Created,
        InProgress,
        Done
    };

    public String user;

    public String runId;

    public RunStatus runStatus;

    public Date createdOn;

    public String runName;

    public DeviceInfoResponse deviceInfo;

    public TestManagerDto targetProfile;

    public List<String> tests;
    
    public LinkedHashMap<String, Object>  testReportKey= new LinkedHashMap<>();
    
    public LinkedHashMap<String, TestResult> testReport = new LinkedHashMap<>();
}