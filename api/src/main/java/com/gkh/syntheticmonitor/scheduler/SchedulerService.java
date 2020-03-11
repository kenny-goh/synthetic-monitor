package com.gkh.syntheticmonitor.scheduler;

import com.gkh.syntheticmonitor.repository.ReportRepository;
import com.gkh.syntheticmonitor.repository.SyntheticTestRepository;
import com.gkh.syntheticmonitor.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class SchedulerService {

	@Autowired
	private SyntheticTestRepository syntheticTestRepository;

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	ApplicationService applicationService;

	@Scheduled(fixedDelay = 15000)
	public void checkTestReadyToFire() {
		log.info("checkTestReadyToFire: Time - {}", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) );
		applicationService.checkTestsReadyToFire();
	}

	@Scheduled(fixedDelay = 20000)
	public void executeNextTest()  {
		log.info("ExecuteNextTest: Time - {}", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		applicationService.executeNextTests();
	}


}