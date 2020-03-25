package com.gkh.syntheticmonitor.repository;

import com.gkh.syntheticmonitor.model.SMTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SMTestRepository extends JpaRepository<SMTest, String> {

	@Modifying
	@Query("update SMTest test set test.readyToExecute = true where test.active=true and " +
			"DATEDIFF(SECOND, test.timeLastExecuted, CURRENT_TIMESTAMP) >= test.scheduleTimeInSeconds")
	void updateReadyToExecute();

	@Query("select test from SMTest test where test.active=true and test.readyToExecute=true")
	List<SMTest> selectReadyToExecuteTests();

	@Query("select test from SMTest test")
	List<SMTest> selectTests();

}

