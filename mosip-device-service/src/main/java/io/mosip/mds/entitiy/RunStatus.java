package io.mosip.mds.entitiy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name ="run_status")
public class RunStatus {
	
	@Id
	@Column(name = "run_id",nullable=false)
	public String runId;
	
	@Column(name="run_passed")
	public boolean runPassed;
	
	@Lob
	@Column(name="target_profile")
	public String targetProfile;

	@Lob
	@Column(name="run_name")
	public String runName; 
}
