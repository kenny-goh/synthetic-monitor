package com.gkh.syntheticmonitor.model;

import com.gkh.syntheticmonitor.exception.SyntheticTestException;


public interface SMActionInterface {
	/**
	 *
	 * @param context
	 */
	void preExecuteScript(SMExecutionContext context);

	/**
	 *
	 * @param context
	 */
	void resolveVariables(SMExecutionContext context);

	/**
	 *
	 * @param context
	 * @throws SyntheticTestException
	 */
	void  execute(SMExecutionContext context) throws Exception;

	/**
	 *
	 * @param context
	 */
	void postExecuteScript(SMExecutionContext context) throws Exception;

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
