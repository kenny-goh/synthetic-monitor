package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Slf4j
public class SMActionAPI extends AbstractSMAction {

	// Todo: SSL certificate support?

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

	@JsonIgnore
	private transient Map<String,String> expandedRequestHeaders;

	@Override
	@SneakyThrows
	public void resolveVariables(SMExecutionContext context) {
		this.expandedUrl =  UriComponentsBuilder.fromHttpUrl(this.requestUrl)
				.buildAndExpand(context.getVars())
				.toUriString();

		log.info("Expanded URL {}", this.expandedUrl);

		SimpleTemplateEngine engine = new groovy.text.SimpleTemplateEngine();
		if (this.requestBody != null && this.requestBody.contains("$")) {
			// Fixme: put this to abstract class
			Template template = engine.createTemplate(this.requestBody);
			Writable textTemplate = template.make(context.getVars());
			this.requestBodyExpanded  = textTemplate.toString();
		}
		else {
			this.requestBodyExpanded = this.requestBody;
		}

		this.expandedRequestHeaders = new HashMap<String,String>(this.requestHeaders);
		this.expandedRequestHeaders.forEach((k, v)->{
			try {
				if (v.contains("$")) {
					Template template = engine.createTemplate(v);
					Writable textTemplate = template.make(context.getVars());
					String expandedValue  = textTemplate.toString();
					this.expandedRequestHeaders.put(k,expandedValue);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		});

	}

	@Override
	public String getDetails() {
		return this.getRequestMethod() + " " + this.requestUrl;
	}


	@Override
	public void execute(SMExecutionContext context) throws Exception  {

		log.info("API Test Execute");

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders httpHeaders = new HttpHeaders();

		// fixme
		if (expandedRequestHeaders!= null) {
			expandedRequestHeaders.keySet().forEach(key -> httpHeaders.set(key, expandedRequestHeaders.get(key)));
		}

		String status, content;

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
		}
		context.setContent(content);
		context.setStatus(status);
	}

	private static HttpMethod getHttpMethod(String method) throws Exception {
		if (method.equals(METHOD_GET)) return HttpMethod.GET;
		if (method.equals(METHOD_POST)) return HttpMethod.POST;
		if (method.equals(METHOD_PUT)) return HttpMethod.PUT;
		if (method.equals(METHOD_DELETE)) return HttpMethod.DELETE;
		throw new Exception("API Method not supported:" + method);
	}

	@Override
	public String getType() {
		return API;
	}

}
