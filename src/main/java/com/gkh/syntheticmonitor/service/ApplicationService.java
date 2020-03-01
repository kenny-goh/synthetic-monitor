package com.gkh.syntheticmonitor.service;

import com.gkh.syntheticmonitor.model.SyntheticTest;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationService {

	@Autowired
	private SyntheticTestRepository repository;

	@Value("${app.settings.bootstrap_yaml_path}")
	private String bootstrapYamlPath;

	@Transactional
	@SneakyThrows
	public void init() {
		repository.deleteAll();

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
				repository.save(test);
			} catch (Exception e) {
				log.error(e.getMessage(), e.getStackTrace());
				e.printStackTrace();
			}
		});

		/*
		List<ReportTest> test = reportRepository.findAll();
		test.forEach(each -> each.getTransactionReports().forEach(action->{
			System.out.println(action.isResponseTimeOptimal());
			System.out.println(action.isResponseTimeUnderMax());
		}));
		 */

	}
}
