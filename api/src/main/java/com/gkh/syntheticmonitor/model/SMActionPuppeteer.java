package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@SuperBuilder
@Slf4j
public class SMActionPuppeteer extends AbstractSMAction {

	public final static String PUPPETEER = "PUPPETEER";

	@Override
	public void execute(SMExecutionContext context) {
	}

	@Override
	public String getType() {
		return PUPPETEER;
	}

	@Override
	public String getDetails() {
		return null;
	}

	@Override
	public String getExpectedStatus() {
		return null;
	}
}
