package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gkh.syntheticmonitor.exception.SyntheticTestException;
import com.gkh.syntheticmonitor.model.converter.ActionYamlConverter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SyntheticTest {

	public static final int DEFAULT_SCHEDULE_TIME_IN_SECONDS = 15;
	public static final int PAUSE_TIME_BETWEEN_ACTIONS_IN_SECONDS = 5;

	@Id
	private String name;

	private String description;

	private int scheduleTimeInSeconds = DEFAULT_SCHEDULE_TIME_IN_SECONDS ;
	private int pauseTimeBetweenActionsInSeconds= PAUSE_TIME_BETWEEN_ACTIONS_IN_SECONDS;
	private boolean active;

	@JsonIgnore
	private boolean readyToExecute;

	@CreationTimestamp
	@JsonIgnore
	private Date timeLastExecuted;

	@Convert(converter = ActionYamlConverter.class)
	@Lob
	@Singular
	private List<AbstractTestAction> actions;

	@SneakyThrows
	public String toYAML() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		String result = mapper.writeValueAsString(this);
		return result;
	}

	public static class SyntheticTestBuilder {

		private int scheduleTimeInSeconds = DEFAULT_SCHEDULE_TIME_IN_SECONDS;
		private int pauseTimeBetweenActionsInSeconds= PAUSE_TIME_BETWEEN_ACTIONS_IN_SECONDS;

		public SyntheticTestBuilder getApiAction(String name,
		                                         String url,
		                                         HashMap headers,
		                                         String preRequestScript,
		                                         String postRequestScript) {
			this.initActionsIfApplicable();
			this.actions.add(TestActionAPI.builder()
					.name(name)
					.requestMethod(TestActionAPI.METHOD_GET)
					.requestUrl(url)
					.requestHeaders(headers)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SyntheticTestBuilder getApiAction(String name,
		                                         String url,
		                                         String preRequestScript,
		                                         String postRequestScript) {
			this.getApiAction(name, url, new HashMap<>(), preRequestScript, postRequestScript);
			return this;
		}

		public SyntheticTestBuilder getApiAction(String name,
		                                         String url) {
			log.info("***{}", this.pauseTimeBetweenActionsInSeconds);
			this.getApiAction(name, url, new HashMap<>(), "", "");
			return this;
		}


		public SyntheticTestBuilder postApiAction(String name,
		                                          String url,
		                                          HashMap headers,
		                                          String body,
		                                          String preRequestScript,
		                                          String postRequestScript) {
			this.initActionsIfApplicable();
			this.actions.add(TestActionAPI.builder()
					.name(name)
					.requestMethod(TestActionAPI.METHOD_POST)
					.requestUrl(url)
					.requestHeaders(headers)
					.requestBody(body)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SyntheticTestBuilder postApiAction(String name, String url, HashMap headers, String body) {
			this.postApiAction(name, url, headers, body, "","");
			return this;
		}

		private void initActionsIfApplicable() {
			if (this.actions == null) {
				this.actions = new ArrayList<>();
			}
		}
	}

	public void execute(TestExecutionContext context) throws SyntheticTestException {
		context.getReport().setName(this.name);
		var size = actions.size();
		for (var each : actions) {
			log.debug("Executing action {}", each.getName());
				each.preExecuteScript(context);
				each.resolveVariables(context);
				each.execute(context);
				each.postExecuteScript(context);
				var index = actions.indexOf(each);
				if (index < size - 1) {
					try {
						TimeUnit.SECONDS.sleep(this.pauseTimeBetweenActionsInSeconds);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		}
	}

	@SneakyThrows
	public static SyntheticTest fromYAML(InputStream stream)  {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		SyntheticTest result = mapper.readValue(stream, SyntheticTest.class);
		return result;
	}

	@SneakyThrows
	public static SyntheticTest fromYAML(String yaml)  {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		SyntheticTest result = mapper.readValue(yaml, SyntheticTest.class);
		return result;
	}


}
