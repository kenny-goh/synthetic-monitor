package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

//@Entity
//@Table(name="ActionPuppeteer")
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@AllArgsConstructor
@SuperBuilder
@Slf4j
public class TestActionPuppeteer extends AbstractTestAction {

	public final static String PUPPETEER = "PUPPETEER";

	@Override
	public void execute(TestExecutionContext context) {
	}

	@Override
	public String getType() {
		return PUPPETEER;
	}
}
