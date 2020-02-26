package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Slf4j
public class SyntheticTestActionAPI extends AbstractSyntheticTestAction {

	public final static String API = "API";
	public final static String METHOD_GET = "GET";
	public final static String METHOD_POST = "POST";

	private String method;
	private String url;
	private HashMap<String,String> headers = new HashMap<>();

	@Override
	public void execute(TestExecutionContext context) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();

		// Fixme: Try this thing called nullable
		if (headers.keySet() != null) {
			headers.keySet().forEach(key -> httpHeaders.set(key, headers.get(key)));
		}
		HttpEntity entity = new HttpEntity(httpHeaders);

		HttpMethod httpMethod = this.method.equals(METHOD_GET) ? HttpMethod.GET : HttpMethod.POST;

		Instant start = Instant.now();
		ResponseEntity<String> response = restTemplate.exchange(this.url, httpMethod, entity, String.class);
		Instant finish = Instant.now();

		String status = response.getStatusCode().toString();
		String content = response.getBody();
		long responseTime = Duration.between(start, finish).toMillis();

		log.info("Status: {}", status);
		log.debug("Content: {}", content);

		ReportSyntheticTestAction stepResult = ReportSyntheticTestAction.builder()
		.stepName(this.getName())
		.type(this.getType())
		.status(status)
		.content(content)
		.responseTime(responseTime)
		.build();

		context.getSyntheticTestResult().getTransactionResults().add(stepResult);
		context.setContent(content);
		context.setStatus(status);
	}

	@Override
	public String getType() {
		return API;
	}

}
