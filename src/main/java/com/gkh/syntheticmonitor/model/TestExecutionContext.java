package com.gkh.syntheticmonitor.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class TestExecutionContext {
	private ReportSyntheticTest syntheticTestResult = new ReportSyntheticTest();
	private HashMap vars = new HashMap();
	private String status;
	private String content;
	private String contentType;
}
