package io.mosip.mds.service;

import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.util.Intent;

public interface IMDSRequestBuilder {
   
    public String getSpecVersion();

    public ComposeRequestResponseDto buildRequest(String runId, TestManagerDto targetProfile, TestDefinition test, DeviceDto device, Intent op);

}