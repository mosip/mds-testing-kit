package io.mosip.mds.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TestRunMetadata {

    private String email;
    private String runId;
    private String runName;
    private String runStatus;
    private List<String> tests;
    private Date createdOn;
    private Date updatedOn;
}