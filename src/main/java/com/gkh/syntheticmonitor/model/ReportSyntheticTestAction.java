package com.gkh.syntheticmonitor.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportSyntheticTestAction {
	private String stepName;
	private String type;
	private String status;
	private String content;
	private long responseTime;
	private long optimalResponseThreshold;
	private long maximumResponseThreshold;

	public boolean isResponseTimeOptimal() {
		return this.responseTime <= this.optimalResponseThreshold;
	}

	public boolean isResponseTimeUnderMax() {
		return this.responseTime <= this.maximumResponseThreshold;
	}
}
