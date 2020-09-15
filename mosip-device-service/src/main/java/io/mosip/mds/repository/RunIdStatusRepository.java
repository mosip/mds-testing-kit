package io.mosip.mds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.mds.entitiy.RunStatus;

import java.util.List;

@Repository
public interface RunIdStatusRepository extends JpaRepository<RunStatus, String> {

    RunStatus findByRunId(String runId);
    List<RunStatus> findAllByRunOwner(String email);

}
