package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

//@Entity
//@Table(name="ACTION_NOOP")
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@AllArgsConstructor
@SuperBuilder
@Slf4j
public class TestActionNOOP extends AbstractTestAction {

	public final static String NOOP = "NOOP";

	@Override
	public void execute(TestExecutionContext context) {
		log.debug(NOOP);

		ReportTestAction stepResult = ReportTestAction.builder()
				.name(this.getName())
				.type(this.getType())
				.status(NOOP)
				.content("")
				.build();

		context.getReport().getTransactionReports().add(stepResult);
		context.setStatus(NOOP);
		context.setContent("");
	}

	@Override
	public String getType() {
		return NOOP;
	}
}
