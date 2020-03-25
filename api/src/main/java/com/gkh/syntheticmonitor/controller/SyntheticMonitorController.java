package com.gkh.syntheticmonitor.controller;

import com.gkh.syntheticmonitor.model.SMTest;
import com.gkh.syntheticmonitor.service.ApplicationService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class SyntheticMonitorController {

	@Autowired
	private ApplicationService appService;

	@GetMapping(
			value = "/find_all_tests",
			produces = {APPLICATION_JSON_VALUE})
	public ResponseEntity<List<SMTest>> getTests() {
		List<SMTest> results = appService.findAllTests();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Access-Control-Allow-Origin", "*");
		return new ResponseEntity<>(results, httpHeaders, HttpStatus.OK);
	}

	@PostMapping(value = "toggle_test")
	@SneakyThrows
	public ResponseEntity<String> toggleTest(@RequestParam  String testName)  {
		Boolean result = appService.toggleTests(testName);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Access-Control-Allow-Origin", "*");
		return new ResponseEntity<String>(result.toString(), httpHeaders, HttpStatus.OK);
	}

	@PostMapping(value = "execute_test")
	@SneakyThrows
	public ResponseEntity<SMTest> executeTests(@RequestParam  String testName)  {
		SMTest test = appService.executeTest(testName);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Access-Control-Allow-Origin", "*");
		return new ResponseEntity<SMTest>(test, httpHeaders, HttpStatus.OK);
	}


}
