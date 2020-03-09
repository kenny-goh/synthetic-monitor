package com.gkh.syntheticmonitor.service;

import com.gkh.syntheticmonitor.model.ReportTest;
import com.gkh.syntheticmonitor.model.SyntheticTest;
import com.gkh.syntheticmonitor.model.TestExecutionContext;
import com.gkh.syntheticmonitor.repository.ReportRepository;
import com.gkh.syntheticmonitor.repository.SyntheticTestRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationService {

	@Autowired
	private SyntheticTestRepository repository;

	@Autowired
	private ReportRepository reportRepository;

	@Value("${app.settings.bootstrap_yaml_path}")
	private String bootstrapYamlPath;

	@Transactional
	@SneakyThrows
	public void init() {


		// Sync YAML and delete

		List<File> filesInFolder = Files.walk(Paths.get(bootstrapYamlPath))
				.filter(p->p.toString().endsWith(".yml"))
				.map(Path::toFile)
				.collect(Collectors.toList());

		log.info("List of files {}", filesInFolder);

		filesInFolder.forEach(file->{
			try {
				log.info("Reading file {}", file.getName());
				String data = FileUtils.readFileToString(file, "UTF-8");
				SyntheticTest test = SyntheticTest.fromYAML(data);
				test.setTimeLastExecuted(new Timestamp(System.currentTimeMillis()));
				repository.save(test);
			} catch (Exception e) {
				log.error(e.getMessage(), e.getStackTrace());
				e.printStackTrace();
			}
		});

	}

	public List<SyntheticTest> findAllSyntheticTests() {
		return repository.selectSyntheticTests();
	}

	@Transactional
	public boolean toggleSyntheticTests(String testName) throws Exception {
		Optional<SyntheticTest> optional = repository.findById(testName);
		if (optional.isPresent()) {
			SyntheticTest test = optional.get();
			test.setActive(!test.isActive());
			return test.isActive();
		} else {
			throw new Exception("Test not found:" + testName);
		}

	}

	@Transactional
	public SyntheticTest executeSyntheticTest(String testName) throws Exception {
		Optional<SyntheticTest> optional = repository.findById(testName);
		if (optional.isPresent()) {
			SyntheticTest test = optional.get();
			log.info("Firing test: {}", test.getName());
			TestExecutionContext context = new TestExecutionContext();
			test.execute(context);
			ReportTest report = context.getReport();
			repository.save(test);
			reportRepository.save(report);
			return test;
		} else {
			throw new Exception("Test not found:" + testName);
		}
	}

	@Transactional
	public void checkTestsReadyToFire() {
		repository.updateReadyToExecute();
	}

	@Transactional
	public void executeNextTests() {
		List<SyntheticTest> tests = repository.selectReadyToExecuteTests();
		int maxSize = tests.size();
		if (maxSize > 0) {
			// Pick a random test
			SyntheticTest test = tests.get(new Random().nextInt(maxSize));
			try {
				log.info("Firing test: {}", test.getName());
				TestExecutionContext context = new TestExecutionContext();
				test.execute(context);
				ReportTest report = context.getReport();
				test.setReadyToExecute(false);
				reportRepository.save(report);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			} finally {
				test.setReadyToExecute(false);
			}
			//repository.save(test);
		}

	}
}
