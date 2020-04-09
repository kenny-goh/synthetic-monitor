package com.gkh.syntheticmonitor.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import wiremock.org.apache.http.protocol.HTTP;

import java.io.*;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class YamlTests extends BaseSyntheticTestSpringSupport {

//	@Value("${test.uri}")
//	private String TEST_URL;
//
//	@Test
//	@SneakyThrows
//	public void givenValidYamlCanConvertToPojo() {
//		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test1.yaml");
//		SMTest test = SMTest.fromYAML(stream);
//		Assertions.assertEquals(test.getName(),"Test");
//		Assertions.assertEquals(test.getActions().get(0).getName(),"Simple test to execute POST api call");
//	}
//
//	@Test
//	@SneakyThrows
//	public void givenValidPojoCanConvertToYaml() {
//
//		SMTest test = SMTest.builder()
//				.name("Test")
//				.action(SMActionAPI.builder()
//						.name("Simple test to execute POST api call")
//						.requestMethod(SMActionAPI.METHOD_POST)
//						.requestUrl(TEST_URL + "/submit-data")
//						.requestHeaders(new HashMap<String, String>() {{
//							put(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE);
//						}})
//						.expectedStatus("200")
//						.build())
//				.build();
//
//		String genaratedYaml = test.toYAML();
//		String yamlFromFile = loadYamlFromFile();
//
//		Assertions.assertEquals(yamlFromFile.strip(), genaratedYaml.strip());
//
//	}
//
//	public String loadYamlFromFile() {
//		InputStream inputStream = SMTest.class
//				.getClassLoader()
//				.getResourceAsStream("test1.yaml");
//
//		String content = "";
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {
//			content = reader.lines().collect(Collectors.joining("\n"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return content.strip();
//	}


}
