package com.gkh.syntheticmonitor.repository;

import com.gkh.syntheticmonitor.model.SyntheticTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SyntheticTestRepository extends JpaRepository<SyntheticTest, String> {
	
	@Modifying
	@Query("update SyntheticTest test set test.readyToExecute = true where test.active=true and DATEDIFF(SECOND, test.timeLastExecuted, CURRENT_TIMESTAMP) >= test.scheduleTimeInSeconds")
	void updateReadyToExecute();

	@Query("select test from SyntheticTest test where test.active = true and test.readyToExecute = true")
	List<SyntheticTest> selectReadyToExecuteTests();

}
