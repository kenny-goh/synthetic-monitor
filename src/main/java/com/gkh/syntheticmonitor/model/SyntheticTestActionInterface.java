package com.gkh.syntheticmonitor.model;

public interface SyntheticTestActionInterface {
	void preExecuteScript(TestExecutionContext context);
	void execute(TestExecutionContext context);
	void postExecuteScript(TestExecutionContext context);
	String getType();
	String getName();
}
