package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gkh.syntheticmonitor.exception.SyntheticTestException;
import com.gkh.syntheticmonitor.model.converter.ActionYamlConverter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.OrderBy;
import java.io.InputStream;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SMTest {

	public static final int DEFAULT_SCHEDULE_TIME_IN_SECONDS = 15;
	public static final int PAUSE_TIME_BETWEEN_ACTIONS_MILLIS = 100;

	@Id
	private String name;

	private String description;

	private String tags;

	@Lob
	private HashMap<String,String> envVariables;

	private int scheduleTimeInSeconds = DEFAULT_SCHEDULE_TIME_IN_SECONDS ;
	private int pauseTimeBetweenActionsMillis = PAUSE_TIME_BETWEEN_ACTIONS_MILLIS;
	private boolean active;
	private String type;

	@JsonIgnore
	private boolean readyToExecute;

	private Timestamp timeLastExecuted;

	@Convert(converter = ActionYamlConverter.class)
	@Lob
	@Singular
	private List<AbstractSMAction> actions;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumns(value = {
			@JoinColumn(name = "NAME", updatable = false, insertable =  false)
		}, foreignKey = @ForeignKey(name="none"))
	@Fetch(FetchMode.SUBSELECT)
	@Where(clause = "DATEDIFF('hour', timestamp, current_timestamp()) < 24")
	@OrderBy(value = "timestamp DESC")
	private List<Report> reports;

	@SneakyThrows
	public String toYAML() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		String result = mapper.writeValueAsString(this);
		return result;
	}

	@JsonGetter
	public double getRatio24Hour() {
		if (this.reports.size() > 0) {
			long passed = this.reports.stream().filter(p -> p.isPassed()).count();
			long total = this.reports.size();
			return ((passed * 1.0) / (total * 1.0)) * 100;
		}
		return 100.0;
	}

	@JsonGetter
	public long getTotalTestsOptimalResponseTime() {
		return this.reports.stream().filter(p->p.isAllResponseTimeOptimal()).count();
	}

	@JsonGetter
	public long getTotalTestsUnderMaxResponseTime() {
		return (this.reports.stream().filter(p->p.isAllResponseTimeUnderMax()).count()) -
				this.getTotalTestsOptimalResponseTime();
	}

	@JsonGetter
	public long getTotalTestsOverMaxResponseTime() {
		return this.reports.stream().filter(p->!p.isAllResponseTimeUnderMax()).count();
	}

	@JsonGetter
	public long getTotalTestsPassed() {
		return this.reports.stream().filter(p->p.isPassed()).count();
	}


	@JsonGetter
	public long getTotalTestsNotMatchStatusCode() {
		return this.reports.stream().filter(p->!p.isAllStatusCodeMatching()).count();
	}

	@JsonGetter
	public long getTotalTestsFailed() {
		return this.reports.stream().filter(p->!p.isPassed()).count();
	}

	@JsonGetter
	public double getAverageResponseTime() {
		return this.reports.stream().mapToDouble(r->r.getSumResponseTime()).summaryStatistics().getAverage();
	}

	@JsonGetter
	public long getMaxTimeThreshold() {
		if (this.reports.size() > 0) {
			return this.reports.get(0).getSumMaxTimeThreshold();
		}
		return 0;
	}

	@JsonGetter
	public long getOptimalTimeThreshold() {
		if (this.reports.size() > 0) {
			return this.reports.get(0).getSumOptimalTimeThreshold();
		}
		return 0;
	}

	@JsonGetter
	public double getTotalRuns() {
		return this.reports.size();
	}

	@JsonGetter
	public int getNumberOfActions() {
		return this.actions.size();
	}

	@JsonGetter
	public String status() {
		if (this.reports.size() > 0) {
			boolean passed = this.reports.get(this.reports.size() - 1).isPassed();
			if (passed) {
				return "passed";
			}
			else  {
				return "failed";
			}
		}
		return "";
	}


	public static class SMTestBuilder {

		private int scheduleTimeInSeconds = DEFAULT_SCHEDULE_TIME_IN_SECONDS;
		private int pauseTimeBetweenActionsInSeconds= PAUSE_TIME_BETWEEN_ACTIONS_MILLIS;

		public SMTestBuilder getApiAction(String name,
		                                  String url,
		                                  HashMap headers,
		                                  String preRequestScript,
		                                  String postRequestScript) {
			this.initActionsIfApplicable();
			this.actions.add(SMActionAPI.builder()
					.name(name)
					.requestMethod(SMActionAPI.METHOD_GET)
					.requestUrl(url)
					.requestHeaders(headers)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SMTestBuilder getApiAction(String name,
		                                  String url,
		                                  String preRequestScript,
		                                  String postRequestScript) {
			this.getApiAction(name, url, new HashMap<>(), preRequestScript, postRequestScript);
			return this;
		}

		public SMTestBuilder getApiAction(String name,
		                                  String url) {
			log.info("***{}", this.pauseTimeBetweenActionsInSeconds);
			this.getApiAction(name, url, new HashMap<>(), "", "");
			return this;
		}


		public SMTestBuilder postApiAction(String name,
		                                   String url,
		                                   HashMap headers,
		                                   String body,
		                                   String preRequestScript,
		                                   String postRequestScript) {
			this.initActionsIfApplicable();
			this.actions.add(SMActionAPI.builder()
					.name(name)
					.requestMethod(SMActionAPI.METHOD_POST)
					.requestUrl(url)
					.requestHeaders(headers)
					.requestBody(body)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SMTestBuilder postApiAction(String name, String url, HashMap headers, String body) {
			this.postApiAction(name, url, headers, body, "","");
			return this;
		}

		private void initActionsIfApplicable() {
			if (this.actions == null) {
				this.actions = new ArrayList<>();
			}
		}
	}

	public void execute(SMExecutionContext context) throws SyntheticTestException {
		log.info("Executing test: {}", this.getName());
		context.getReport().setName(this.name);
		var size = actions.size();

		// Bind environment variables
		if (this.envVariables != null) {
			this.envVariables.forEach((key, value) -> {
				context.getVars().put(key, value);
			});
		}

		for (var each : actions) {
				log.info("Firing action {}", each.getName());
				if (each.getPrePauseTimeMillis() > 0) {
					sleep(each.getPrePauseTimeMillis());
				}
				each.preExecuteScript(context);
				each.resolveVariables(context);
				each.execute(context);
				each.postExecuteScript(context);
				if (each.getPostPauseTimeMillis() > 0) {
					sleep(each.getPostPauseTimeMillis());
				}
				var index = actions.indexOf(each);
				if (index < size - 1) {
					sleep(this.pauseTimeBetweenActionsMillis);
				}
		}
		this.timeLastExecuted = new Timestamp(System.currentTimeMillis());
	}

	private void sleep(long prePauseTimeMillis) {
		try {
			TimeUnit.MILLISECONDS.sleep(prePauseTimeMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SneakyThrows
	public static SMTest fromYAML(InputStream stream)  {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		SMTest result = mapper.readValue(stream, SMTest.class);
		return result;
	}

	@SneakyThrows
	public static SMTest fromYAML(String yaml)  {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		SMTest result = mapper.readValue(yaml, SMTest.class);
		return result;
	}


}
