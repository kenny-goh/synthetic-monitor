package com.gkh.syntheticmonitor.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class TestExecutionContext {
	private ReportTest report = new ReportTest();
	private HashMap vars = new HashMap();
	private String status;
	private String content;
	private String contentType;

}
