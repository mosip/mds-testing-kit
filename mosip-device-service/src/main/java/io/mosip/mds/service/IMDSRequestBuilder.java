package io.mosip.mds.service;

import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.util.Intent;

public interface IMDSRequestBuilder {
   
    public String getSpecVersion();

    public ComposeRequestResponseDto buildRequest(TestRun run, TestExtnDto test, DeviceDto device, Intent op);
   

}