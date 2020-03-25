package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.springframework.util.StringUtils;

import javax.persistence.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Slf4j
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = SMActionAPI.class, name = "API")
})
public abstract class AbstractSMAction implements SMActionInterface {

	@Id
	private String name;
	private String description;
	private String type;
	private boolean excludeInTestReport;
	private String postRequestScript;
	private String preRequestScript;

	private long maximalResponseThreshold;
	private transient long prePauseTimeMillis;
	private transient long postPauseTimeMillis;

	public void preExecuteScript(SMExecutionContext context) {
		if (!StringUtils.isEmpty(this.preRequestScript)) {
			log.debug("Executing pre script \"{}\"", this.preRequestScript);
			evalGroovyScript(context, this.preRequestScript);
		}
	}
	public void postExecuteScript(SMExecutionContext context) {
		if (!StringUtils.isEmpty(this.postRequestScript)) {
			log.debug("Executing post script \"{}\"", this.postRequestScript);
			evalGroovyScript(context, this.postRequestScript);
		}
	}

	private void evalGroovyScript(SMExecutionContext context, String script) {
		Binding binding = new Binding();
		binding.setVariable("context", context);
		GroovyShell shell = new GroovyShell(binding);
		shell.evaluate(script);
	}

	@Override
	public void resolveVariables(SMExecutionContext context) { }

	@JsonIgnore
	public abstract String getDetails();
	public abstract String getExpectedStatus();
}
