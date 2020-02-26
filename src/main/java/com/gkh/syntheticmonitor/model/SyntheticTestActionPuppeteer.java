package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@SuperBuilder
@Slf4j
public class SyntheticTestActionPuppeteer extends AbstractSyntheticTestAction {

	public final static String PUPPETEER = "PUPPETEER";

	@Override
	public void execute(TestExecutionContext context) {
	}

	@Override
	public String getType() {
		return PUPPETEER;
	}
}
