package com.gkh.syntheticmonitor.model;

import com.gkh.syntheticmonitor.exception.SyntheticTestException;


public interface SyntheticTestActionInterface {
	/**
	 *
	 * @param context
	 */
	void preExecuteScript(TestExecutionContext context);

	/**
	 *
	 * @param context
	 */
	void resolveVariables(TestExecutionContext context);

	/**
	 *
	 * @param context
	 * @throws SyntheticTestException
	 */
	void execute(TestExecutionContext context) throws SyntheticTestException;

	/**
	 *
	 * @param context
	 */
	void postExecuteScript(TestExecutionContext context);

	/**
	 *
	 * @return
	 */
	String getType();

	/**
	 *
	 * @return
	 */
	String getName();
}
