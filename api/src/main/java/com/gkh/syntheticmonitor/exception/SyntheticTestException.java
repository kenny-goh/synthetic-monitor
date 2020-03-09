package com.gkh.syntheticmonitor.exception;


public class SyntheticTestException extends Exception {

	public SyntheticTestException(String message) {
		super(message);
	}

	public SyntheticTestException(String message, Throwable cause) {
		super(message, cause);
	}

	public SyntheticTestException(Throwable cause) {
		super(cause);
	}

	public SyntheticTestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
