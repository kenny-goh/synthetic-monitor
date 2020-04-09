package com.gkh.syntheticmonitor.repository

import com.gkh.syntheticmonitor.model.SMTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SMTestRepository : JpaRepository<SMTest, String> {
    @Modifying
    @Query("update SMTest test set test.isReadyToExecute = true where test.isActive=true and " +
            "DATEDIFF(SECOND, test.timeLastExecuted, CURRENT_TIMESTAMP) >= test.scheduleTimeInSeconds")
    fun updateReadyToExecute()

    @Query("select test from SMTest test where test.isActive=true and test.isReadyToExecute=true")
    fun selectReadyToExecuteTests(): List<SMTest>

    @Query("select test from SMTest test")
    fun selectTests(): List<SMTest>
}