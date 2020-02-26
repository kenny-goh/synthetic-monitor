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
public class SyntheticTestActionNOOP extends AbstractSyntheticTestAction {

	public final static String NOOP = "NOOP";

	@Override
	public void execute(TestExecutionContext context) {
		log.debug(NOOP);

		ReportSyntheticTestAction stepResult = ReportSyntheticTestAction.builder()
				.stepName(this.getName())
				.type(this.getType())
				.status(NOOP)
				.content("")
				.build();

		context.getSyntheticTestResult().getTransactionResults().add(stepResult);
		context.setStatus(NOOP);
		context.setContent("");
	}

	@Override
	public String getType() {
		return NOOP;
	}
}
