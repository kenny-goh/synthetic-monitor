package com.gkh.syntheticmonitor.controller;


import com.gkh.syntheticmonitor.model.SyntheticTest;
import com.gkh.syntheticmonitor.service.ApplicationService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class SyntheticTestController {

	@Autowired
	private ApplicationService appService;

	@GetMapping(
			value = "/get_synthetic_tests",
			produces = {APPLICATION_JSON_VALUE})
	public ResponseEntity<List<SyntheticTest>> getSyntheticTests() {
		List<SyntheticTest> results = appService.findAllSyntheticTests();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Access-Control-Allow-Origin", "*");
		return new ResponseEntity<>(results, httpHeaders, HttpStatus.OK);
	}

	@PostMapping(value = "toggle_synthetic_test")
	@SneakyThrows
	public ResponseEntity<String> toggleSyntheticTest(@RequestParam  String testName)  {
		Boolean result = appService.toggleSyntheticTests(testName);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Access-Control-Allow-Origin", "*");
		return new ResponseEntity<String>(result.toString(), httpHeaders, HttpStatus.OK);
	}

	@PostMapping(value = "execute_synthetic_test")
	@SneakyThrows
	public ResponseEntity<SyntheticTest> executeSyntheticTests(@RequestParam  String testName)  {
		SyntheticTest test = appService.executeSyntheticTest(testName);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Access-Control-Allow-Origin", "*");
		return new ResponseEntity<SyntheticTest>(test, httpHeaders, HttpStatus.OK);
	}


}
