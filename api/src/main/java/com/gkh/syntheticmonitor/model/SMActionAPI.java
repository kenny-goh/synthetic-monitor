package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gkh.syntheticmonitor.exception.SyntheticTestException;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Slf4j
public class SMActionAPI extends AbstractSMAction {

	public final static String API = "API";
	public final static String METHOD_GET = "GET";
	public final static String METHOD_POST = "POST";
	public final static String METHOD_PUT = "PUT";
	public final static String METHOD_DELETE = "DELETE";

	@Singular
	private Map<String,String> requestHeaders = new HashMap<>();
	private String requestMethod;
	private String requestUrl;
	private String requestBody;
	private transient String requestBodyExpanded;
	private String expectedStatus;

	@JsonIgnore
	private transient String expandedUrl;

	@Override
	@SneakyThrows
	public void resolveVariables(SMExecutionContext context) {
		this.expandedUrl =  UriComponentsBuilder.fromHttpUrl(this.requestUrl)
				.buildAndExpand(context.getVars())
				.toUriString();

		log.info("Expanded URL {}", this.expandedUrl);

		if (this.requestBody != null && this.requestBody.contains("$")) {
			// Fixme: put this to abstract class
			SimpleTemplateEngine engine = new groovy.text.SimpleTemplateEngine();
			Template template = engine.createTemplate(this.requestBody);
			Writable textTemplate = template.make(context.getVars());
			this.requestBodyExpanded  = textTemplate.toString();
		} else {
			this.requestBodyExpanded = this.requestBody;
		}
	}

	@Override
	public void execute(SMExecutionContext context) throws SyntheticTestException {


		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();

		if (requestHeaders != null) {
			requestHeaders.keySet().forEach(key -> httpHeaders.set(key, requestHeaders.get(key)));
		}

		String status, content;
		long responseTime = 0;
		Instant start = Instant.now();
		try {
			HttpEntity entity = new HttpEntity(this.requestBodyExpanded, httpHeaders);
			HttpMethod httpMethod = getHttpMethod(requestMethod);

			ResponseEntity<String> response = restTemplate.exchange(this.expandedUrl, httpMethod, entity, String.class);

			status = Integer.toString(response.getStatusCode().value());
			content = response.getBody();

			log.info("Status: {}", status);
			log.info("Content: {}", content);
		} catch (ResourceAccessException e ) {
			status = "TIMEOUT";
			content = e.getMessage();
		} finally {
			Instant finish = Instant.now();
			responseTime = Duration.between(start, finish).toMillis();
		}

		ReportDetail actionReport = ReportDetail.builder()
		.name(this.getName())
		.details(this.getRequestMethod() + " " + this.requestUrl)
		.type(this.getType())
		.status(status)
		.content(content)
		.optimalResponseThreshold(this.getOptimalResponseThreshold())
		.maximumResponseThreshold(this.getMaximalResponseThreshold())
		.expectedStatus(this.getExpectedStatus())
		.responseTime(responseTime)
		.build();

		context.getReport().getReportDetails().add(actionReport);
		context.setContent(content);
		context.setStatus(status);

		log.info("Status: {} Content: {}", status, content);


	}

	private static HttpMethod getHttpMethod(String method) throws SyntheticTestException {
		if (method.equals(METHOD_GET)) return HttpMethod.GET;
		if (method.equals(METHOD_POST)) return HttpMethod.POST;
		if (method.equals(METHOD_PUT)) return HttpMethod.PUT;
		if (method.equals(METHOD_DELETE)) return HttpMethod.DELETE;
		throw new SyntheticTestException("API Method not supported:" + method);
	}

	@Override
	public String getType() {
		return API;
	}

}
