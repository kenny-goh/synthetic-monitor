package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gkh.syntheticmonitor.exception.SyntheticTestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

//@Entity
//@Table(name="ACTION_API")
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Slf4j
public class TestActionAPI extends AbstractTestAction {

	public final static String API = "API";
	public final static String METHOD_GET = "GET";
	public final static String METHOD_POST = "POST";

	@Singular
	private Map<String,String> requestHeaders = new HashMap<>();
	private String requestMethod;
	private String requestUrl;
	private String requestBody;
	private String expectedStatus;

	@JsonIgnore
	private transient String expandedUrl;


	@Override
	public void resolveVariables(TestExecutionContext context) {
		//UriTemplate template = new UriTemplate(this.requestUrl);
		//this.expandedUrl = template.expand(context.getVars()).toString();

		this.expandedUrl =  UriComponentsBuilder.fromHttpUrl(this.requestUrl)
				.buildAndExpand(context.getVars())
				.toUriString();
	}

	@Override
	public void execute(TestExecutionContext context) throws SyntheticTestException {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();

		// Fixme: Use UriComponentsbuilder

		if (requestHeaders != null) {
			requestHeaders.keySet().forEach(key -> httpHeaders.set(key, requestHeaders.get(key)));
		}


		HttpEntity entity = new HttpEntity(this.requestBody, httpHeaders);
		HttpMethod httpMethod = getHttpMethod(requestMethod);

		Instant start = Instant.now();
		ResponseEntity<String> response = restTemplate.exchange(this.expandedUrl, httpMethod, entity, String.class);
		Instant finish = Instant.now();

		String status = Integer.toString(response.getStatusCode().value());
		String content = response.getBody();
		long responseTime = Duration.between(start, finish).toMillis();

		log.info("Status: {}", status);
		log.debug("Content: {}", content);

		ReportTestAction actionReport = ReportTestAction.builder()
		.name(this.getName())
		.type(this.getType())
		.status(status)
		.content(content)
		.optimalResponseThreshold(this.getOptimalResponseThreshold())
		.maximumResponseThreshold(this.getMaximalResponseThreshold())
		.expectedStatus(this.getExpectedStatus())
		.responseTime(responseTime)
		.build();

		context.getReport().getTransactionReports().add(actionReport);
		context.setContent(content);
		context.setStatus(status);


	}

	private static HttpMethod getHttpMethod(String method) throws SyntheticTestException {
		if (method.equals(METHOD_GET)) return HttpMethod.GET;
		if (method.equals(METHOD_POST)) return HttpMethod.POST;
		throw new SyntheticTestException("API Method not supported:" + method);
	}

	@Override
	public String getType() {
		return API;
	}

}
