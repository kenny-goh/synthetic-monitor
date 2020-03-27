package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gkh.syntheticmonitor.model.converter.ActionYamlConverter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.OrderBy;
import java.io.InputStream;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SMTest {

	public static final int DEFAULT_SCHEDULE_TIME_IN_SECONDS = 15;

	@Id
	private String name;
	private String description;
	private String tags;

	@org.hibernate.annotations.Type(
			type = "org.hibernate.type.SerializableToBlobType",
			parameters = { @Parameter( name = "classname", value = "java.util.HashMap" ) }
	)
	@Singular
	private Map<String,String> variables = new HashMap<>();

	private int scheduleTimeInSeconds = DEFAULT_SCHEDULE_TIME_IN_SECONDS ;
	private boolean active;
	private String type;
	private boolean continueActionOnFail;
	private boolean monitored;

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
	public long getStatisticsTestsUnderMaxResponseTime() {
		return this.reports.stream().filter(p->p.isAllResponseTimeUnderMax()).count();
	}

	@JsonGetter
	public long getStatisticsTestsOverMaxResponseTime() {
		return this.reports.stream().filter(p->!p.isAllResponseTimeUnderMax()).count();
	}

	@JsonGetter
	public long getStatisticsTestsPassed() {
		return this.reports.stream().filter(p->p.isPassed()).count();
	}


	@JsonGetter
	public long getStatisticsTestsNotMatchStatusCode() {
		return this.reports.stream().filter(p->!p.isAllStatusCodeMatching()).count();
	}

	@JsonGetter
	public long getStatisticsTestsFailed() {
		return this.reports.stream().filter(p->!p.isPassed()).count();
	}

	@JsonGetter
	public double getAverageResponseTime() {
		return this.reports.stream().mapToDouble(r->r.getSumResponseTime()).summaryStatistics().getAverage();
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
		// Fixme: "Stable", "Unstable", "Bad", "Critical", "Healthy"
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


	public void execute(SMExecutionContext context) {
		log.info("Executing test: {}", this.getName());
		context.getReport().setName(this.name);

		// Bind environment variables
		if (this.variables != null) {
			this.variables.forEach((key, value) -> {
				context.getVars().put(key, value);
			});
		}

		boolean previousStepFailed = false;
		for (var each : actions) {
				Instant start = Instant.now();
				long responseTime = 0;
				try {
					if (!previousStepFailed || this.continueActionOnFail) {

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

						Instant finish = Instant.now();
						responseTime = Duration.between(start, finish).toMillis();

						createReport(context, each, responseTime, context.getContent(), context.getStatus());

					}
				}
				catch(Exception e) {
					previousStepFailed = true;

					Instant finish = Instant.now();
					responseTime = Duration.between(start, finish).toMillis();

					String content = e.getMessage();
					String status = "ERROR";

					createReport(context, each, responseTime, content, status);

					context.setContent(content);
					context.setStatus(status);
				}
			}


		log.info("Status: {} Content: {}", context.getStatus(), context.getContent());

		this.timeLastExecuted = new Timestamp(System.currentTimeMillis());
	}

	private void createReport(SMExecutionContext context, AbstractSMAction each, long responseTime, String content, String status) {
		ReportDetail actionReport = ReportDetail.builder()
				.name(each.getName())
				.details(each.getDetails())
				.type(each.getType())
				.status(status)
				.content(content)
				.maximumResponseThreshold(each.getMaximalResponseThreshold())
				.expectedStatus(each.getExpectedStatus())
				.responseTime(responseTime)
				.build();
		context.getReport().getReportDetails().add(actionReport);
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
