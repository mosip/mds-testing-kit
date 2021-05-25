package io.mosip.mds.dto;

import io.mosip.mds.dto.getresponse.UIInput;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TestDefinition {

    @ApiModelProperty(value = "method", required = true, dataType = "java.lang.String")
    public String method;

    @ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
    public String testId;

    @ApiModelProperty(value = "testOrderId", required = true, dataType = "java.lang.String")
    public String testOrderId;
    
    @ApiModelProperty(value = "testDescription", required = true, dataType = "java.lang.String")
    public String testDescription;

    @ApiModelProperty(value = "requestGenerator", required = true, dataType = "java.lang.String")
    public String requestGenerator;

    public List<UIInput> uiInput;

    public List<String> purposes;

    public List<String> biometricTypes;

    public List<String> deviceSubTypes;

    public List<String> segments;

    public List<String> exceptions;

    public int deviceSubId;

    public int bioCount;

    public String uinNumber;

    public String mispLicenseKey;

    public String partnerId;

    public String partnerApiKey;

    public List<String> sbiSpecVersions;

    public List<ValidatorDef> validatorDefs;

    public int requestedScore;
}
