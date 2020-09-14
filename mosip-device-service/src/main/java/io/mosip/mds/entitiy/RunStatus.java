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
	
	@Column(name="status")
	public String status;
	
	@Lob
	@Column(name="profile")
	public String profile;

	@Lob
	@Column(name="name")
	public String runName;

	@Column(name="owner")
	public String runOwner;
}
