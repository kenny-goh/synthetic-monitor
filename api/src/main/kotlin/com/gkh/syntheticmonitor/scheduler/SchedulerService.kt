package com.gkh.syntheticmonitor.scheduler

import com.gkh.syntheticmonitor.repository.ReportRepository
import com.gkh.syntheticmonitor.repository.SMTestRepository
import com.gkh.syntheticmonitor.service.ApplicationService
import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Slf4j
@Service
class SchedulerService(
    private val testRepository: SMTestRepository,
    private val reportRepository: ReportRepository,
    private var applicationService: ApplicationService
) {

    companion object {
        var logger = LogManager.getLogger()
    }

    @Scheduled(fixedDelay = 45000)
    fun checkTestReadyToFire() {
        logger.info("checkTestReadyToFire: Time - {}", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()))
        applicationService!!.checkTestsReadyToFire()
    }

    @Scheduled(fixedDelay = 60000)
    fun executeNextTest() {
        logger.info("ExecuteNextTest: Time - {}", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()))
        applicationService!!.executeNextTests()
    }
}