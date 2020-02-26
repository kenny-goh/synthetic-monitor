package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
// Fixme: Use mock web response
public class SyntheticTestAPIExecutionTests {

	@Test
	@SneakyThrows
	public void givenTestActionApiActionWithValidParametersSyntheticTestCanExecute() {

		SyntheticTest test = SyntheticTest.builder()
				.name("Onboard process")
				.actions(new ArrayList<>())
				.apiTestAction("Step 1: Request token",
						"POST",
						"http://reecetstausys.reece.com.au/au/customer-application-onboarding-gateway/request-token",
						new HashMap<String, String>() {{
							put("consumer-key", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiUFZUVkVORE9SIiwiZW52Ijoic3RhZ2UifQ.Y5tRmPOLiz8UgUusVRBXVMTPl8TYbtOhPVHdslSNhm0");
						}},
						"",
						"")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		ObjectMapper mapper = new ObjectMapper();
		String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context.getSyntheticTestResult());
		log.info(jsonOutput);

		String yaml = SyntheticTest.toYAML(test);
		System.out.println(yaml);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreResultIntoContext() {

		SyntheticTest test = SyntheticTest.builder()
				.name("Version checks")
				.actions(new ArrayList<>())
				.apiTestAction("Check version of branch service",
						"GET",
						"http://reecetstausys.reece.com.au/branch-services/version",
						"",
						"")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),"200 OK");

		String yaml = SyntheticTest.toYAML(test);
		System.out.println(yaml);

	}


	@Test
	@SneakyThrows
	public void syntheticTestCanStoreOutputIntoContextVariables() {

		SyntheticTest test = SyntheticTest.builder()
				.name("Version checks")
				.actions(new ArrayList<>())
				.apiTestAction("Check version of branch service",
						"GET",
						"http://reecetstausys.reece.com.au/branch-services/version",
						"",
						"context.vars.put('var1', context.status);\ncontext.vars.put('var2', context.content)")
				.build();


		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),"200 OK");
		Assertions.assertEquals(context.getVars().get("var1"),"200 OK");
		Assertions.assertTrue(!context.getVars().get("var2").toString().isBlank());

		String yaml = SyntheticTest.toYAML(test);
		System.out.println(yaml);
	}


}
