package com.gkh.syntheticmonitor;

import com.gkh.syntheticmonitor.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

// Todo: Agent to execute action remotely?
// Todo: Plugin architecture

@SpringBootApplication
@Slf4j
public class SyntheticMonitorApplication {

	@Autowired
	ApplicationService syntheticTestService;

	@PostConstruct
	private void init() {
		log.info("Initializing SyntheticMonitor ...");
		syntheticTestService.init();
	}
	public static void main(String[] args) {
		SpringApplication.run(SyntheticMonitorApplication.class, args);
	}
}
