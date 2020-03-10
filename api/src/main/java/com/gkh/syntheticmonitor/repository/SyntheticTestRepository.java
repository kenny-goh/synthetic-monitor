package com.gkh.syntheticmonitor.repository;

import com.gkh.syntheticmonitor.model.SMTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SyntheticTestRepository extends JpaRepository<SMTest, String> {

	// FIXME: There is a bug somewhere with this code or the hibernate library that causes this piece of code not to work properly

	@Modifying
	@Query("update SMTest test set test.readyToExecute = true where test.active=true and " +
			"DATEDIFF(SECOND, test.timeLastExecuted, CURRENT_TIMESTAMP) >= test.scheduleTimeInSeconds")
	void updateReadyToExecute();

	@Query("select test from SMTest test where test.active=true and test.readyToExecute=true")
	List<SMTest> selectReadyToExecuteTests();

	@Query("select test from SMTest test")
	List<SMTest> selectSyntheticTests();

}

