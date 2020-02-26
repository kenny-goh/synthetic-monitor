package com.gkh.syntheticmonitor.model;


import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SyntheticTestActionJDBC extends AbstractSyntheticTestAction {

	public final static String JDBC = "JDBC";

	private String driver;
	private String url;
	private String username;
	private String password;

	@Override
	public void execute(TestExecutionContext context) {
		// todo
	}

	@Override
	public String getType() {
		return JDBC;
	}
}
