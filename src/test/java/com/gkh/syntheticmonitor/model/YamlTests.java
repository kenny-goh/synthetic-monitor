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

	@Value("${test.uri}")
	private String TEST_URL;

	@Test
	@SneakyThrows
	public void givenValidYamlCanConvertToPojo() {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test1.yaml");
		SyntheticTest test = SyntheticTest.fromYAML(stream);
		Assertions.assertEquals(test.getName(),"Test");
		Assertions.assertEquals(test.getActions().get(0).getName(),"Simple test to execute POST api call");
	}

	@Test
	@SneakyThrows
	public void givenValidPojoCanConvertToYaml() {

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.postApiAction("Simple test to execute POST api call",
						TEST_URL + "/submit-data",
						new HashMap<String, String>() {{
							put(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE);
						}},
						"")
				.build();

		String genaratedYaml = SyntheticTest.toYAML(test);
		String yamlFromFile = loadYamlFromFile();

		Assertions.assertEquals(yamlFromFile.strip(), genaratedYaml.strip());

	}

	public String loadYamlFromFile() {
		InputStream inputStream = SyntheticTest.class
				.getClassLoader()
				.getResourceAsStream("test1.yaml");

		String content = "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {
			content = reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.strip();
	}


}
