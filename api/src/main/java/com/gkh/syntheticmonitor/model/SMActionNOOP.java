package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;


@Data
@AllArgsConstructor
@SuperBuilder
@Slf4j
public class SMActionNOOP extends AbstractSMAction {

	public final static String NOOP = "NOOP";

	@Override
	public void execute(SMExecutionContext context) {
		log.debug(NOOP);

		ReportDetail stepResult = ReportDetail.builder()
				.name(this.getName())
				.type(this.getType())
				.status(NOOP)
				.content("")
				.build();

		context.getReport().getReportDetails().add(stepResult);
		context.setStatus(NOOP);
		context.setContent("");
	}

	@Override
	public String getType() {
		return NOOP;
	}
}
