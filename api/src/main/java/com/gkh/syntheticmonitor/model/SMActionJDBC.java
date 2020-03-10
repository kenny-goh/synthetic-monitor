package com.gkh.syntheticmonitor.model;


import lombok.Data;
import lombok.experimental.SuperBuilder;

//@Entity
//@Table(name="ACTION_JDBC")
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@SuperBuilder
public class TestActionJDBC extends AbstractSMAction {

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
