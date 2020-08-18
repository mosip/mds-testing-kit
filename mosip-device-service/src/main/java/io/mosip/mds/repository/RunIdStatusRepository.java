package io.mosip.mds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.mds.entitiy.RunIdStatus;

@Repository
public interface RunIdStatusRepository extends JpaRepository<RunIdStatus, String> {

}
