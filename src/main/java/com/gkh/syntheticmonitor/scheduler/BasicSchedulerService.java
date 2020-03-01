package com.gkh.syntheticmonitor.scheduler;

import com.gkh.syntheticmonitor.model.ReportTest;
import com.gkh.syntheticmonitor.model.SyntheticTest;
import com.gkh.syntheticmonitor.model.TestExecutionContext;
import com.gkh.syntheticmonitor.repository.ReportRepository;
import com.gkh.syntheticmonitor.repository.SyntheticTestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class BasicSchedulerService {

	@Autowired
	private SyntheticTestRepository syntheticTestRepository;

	@Autowired
	private ReportRepository reportRepository;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void checkTestReadyToFire() {
		log.info("checkTestReadyToFire: Time - {}", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) );
		syntheticTestRepository.updateReadyToExecute();
	}


	@Scheduled(fixedDelay = 15000)
	@Transactional
	public void executeNextTest()  {
		log.info("ExecuteNextTest: Time - {}", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		List<SyntheticTest> tests = syntheticTestRepository.selectReadyToExecuteTests();
		int maxSize = tests.size();
		if (maxSize > 0) {
			// Pick a random test
			SyntheticTest test = tests.get(new Random().nextInt(maxSize));
			try {
				log.info("Firing test: {}", test.getName());
				TestExecutionContext context = new TestExecutionContext();
				test.execute(context);
				ReportTest report = context.getReport();
				reportRepository.save(report);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			} finally {
				test.setReadyToExecute(false);
			}
		}


	}


}
