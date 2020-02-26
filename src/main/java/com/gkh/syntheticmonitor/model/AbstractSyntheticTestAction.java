package com.gkh.syntheticmonitor.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Slf4j
public abstract class AbstractSyntheticTestAction implements SyntheticTestActionInterface {

	private String name;
	private boolean excludeInTestReport;
	private String postRequestScript;
	private String preRequestScript;

	@Value("${apitransaction.optimalResponseThreshold}")
	private long optimalResponseThreshold;
	@Value("${apitransaction.maximalResponseThreshold}")
	private long maximalResponseThreshold;

	public void preExecuteScript(TestExecutionContext context) {
		if (!this.preRequestScript.isBlank()) {
			log.debug("Executing pre script \"{}\"", this.preRequestScript);
			evalGroovyScript(context, this.preRequestScript);
		}
	}
	public void postExecuteScript(TestExecutionContext context) {
		if (!this.postRequestScript.isBlank()) {
			log.debug("Executing post script \"{}\"", this.postRequestScript);
			evalGroovyScript(context, this.postRequestScript);
		}
	}

	private void evalGroovyScript(TestExecutionContext context, String script) {
		Binding binding = new Binding();
		binding.setVariable("context", context);
		GroovyShell shell = new GroovyShell(binding);
		shell.evaluate(script);
	}


}
