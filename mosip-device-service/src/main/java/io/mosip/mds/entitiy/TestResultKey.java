package io.mosip.mds.entitiy;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
public class TestResultKey implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name = "run_id",nullable=false)
	public String runId;
	
	@Column(name="testcase_name",nullable=false)
	public String testcaseName;
}
