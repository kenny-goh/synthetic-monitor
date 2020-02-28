package com.gkh.syntheticmonitor.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import wiremock.org.apache.http.HttpStatus;
import wiremock.org.apache.http.protocol.HTTP;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureWireMock
public class SyntheticTestAPIExecutionTests extends BaseSyntheticTestSpringSupport {


	public static final String MOCK_JSON_RESPONSE = "{'data':'blah'}";

	@Test
	@SneakyThrows
	public void givenValidURlCanExecuteGetTest() {

		givenGetHelloWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.getApiAction("Simple test to execute GET api call", TEST_URL + "/hello")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),"200 OK");
	}

	@Test
	@SneakyThrows
	public void givenValidUrlAndQueryParametersCanExecuteGetTest() {
		givenGetHelloWithQueryParametersWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.getApiAction("Simple test to execute GET api call with query params",
						TEST_URL + "/hello?param1=foo&param2=bar")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),"200 OK");
	}

	@Test
	@SneakyThrows
	public void givenValidUrlAndHeadersAndBodyCanExecutePostTest() {

		givenPostSubmitDataWillReturnExpectedMockedResult();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.postApiAction("Simple test to execute POST api call",
						TEST_URL + "/submit-data",
						new HashMap<String, String>() {{
							put(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE);
						}},
						MOCK_JSON_RESPONSE)
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),"200 OK");
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreStatusIntoContext() {

		givenGetHelloWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.getApiAction("Simple test to execute GET api call", TEST_URL + "/hello")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),"200 OK");
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreOutputIntoContextVariables() {

		givenGetHelloWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.getApiAction("Simple test to execute GET api call", TEST_URL + "/hello",
						"",
						"context.vars.put('var1', context.status)")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(), "200 OK");
		Assertions.assertEquals(context.getVars().get("var1"), "200 OK");
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanResolveExpressionInAPIUrlPriorToTestExecution() {

		givenGetHelloWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.getApiAction("Simple test to execute GET api call",
						TEST_URL + "/{var1}",
						"context.vars.put('var1','hello')",
						"")
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);
		Assertions.assertEquals(context.getStatus(), "200 OK");
	}

	private void givenGetHelloWillReturnOkay() {
		stubFor(
				get(urlPathEqualTo("/hello"))
						.willReturn(
								aResponse()
										.withStatus(HttpStatus.SC_OK)
										.withBody("")));
	}

	private void givenGetHelloWithQueryParametersWillReturnOkay() {
		stubFor(
				get(urlPathEqualTo("/hello"))
						.withQueryParam("param1", equalTo("foo"))
						.withQueryParam("param2", equalTo("bar"))
						.willReturn(
								aResponse()
										.withStatus(HttpStatus.SC_OK)
										.withBody("")));
	}

	private void givenPostSubmitDataWillReturnExpectedMockedResult() {
		stubFor(
				post(urlPathEqualTo("/submit-data"))
						.withRequestBody(equalTo(MOCK_JSON_RESPONSE))
						.willReturn(
								aResponse()
										.withStatus(HttpStatus.SC_OK)
										.withHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE)
										.withBody("")));
	}




}
