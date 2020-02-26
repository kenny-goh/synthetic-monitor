package com.gkh.syntheticmonitor.model;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
public class YamlTests {

	@Test
	@SneakyThrows
	public void givenValidYamlCanConvertToPojo() {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("test1.yaml");
		SyntheticTest test = SyntheticTest.fromYAML(stream);
		Assertions.assertEquals(test.getName(),"Version checks");
		Assertions.assertEquals(test.getActions().get(0).getName(),"Check version of branch service");
	}

	@Test
	public void givenValidPojoCanConvertToYaml() {
		SyntheticTest test = SyntheticTest.builder()
				.name("Version checks")
				.actions(new ArrayList<>())
				.apiTestAction("Check version of branch service",
						"GET",
						"http://reecetstausys.reece.com.au/branch-services/version",
						"",
						"")
				.build();



		String yaml = SyntheticTest.toYAML(test);

		InputStream inputStream = SyntheticTest.class
				.getClassLoader()
				.getResourceAsStream("test2.yaml");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)) ) {
			String content = reader.lines()
					.collect(Collectors.joining("\n"));
			Assertions.assertEquals(yaml.strip(), content.strip());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
