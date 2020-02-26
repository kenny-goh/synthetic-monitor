package com.gkh.syntheticmonitor.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportSyntheticTest {
	private String name;
	private List<ReportSyntheticTestAction> transactionResults = new ArrayList<>();

}
