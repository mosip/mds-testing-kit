package io.mosip.mds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.mds.entitiy.TestResultKey;
import io.mosip.mds.entitiy.TestcaseResult;

@Repository
public interface TestCaseResultRepository extends JpaRepository<TestcaseResult, TestResultKey>{
	public List<Optional<TestcaseResult>> findByOwner(String testcaseOwner);
	public List<Optional<TestcaseResult>> findByTestResultKeyRunId(String runId);
}
