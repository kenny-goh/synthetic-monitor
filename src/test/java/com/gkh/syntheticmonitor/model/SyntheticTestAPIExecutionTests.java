package com.gkh.syntheticmonitor.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import wiremock.org.apache.http.HttpStatus;
import wiremock.org.apache.http.protocol.HTTP;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureWireMock
public class SyntheticTestAPIExecutionTests extends BaseSyntheticTestSpringSupport {


	public static final String MOCK_JSON_REQUEST = "{'data':'blah'}";
	public static final String STATUS_200 = "200";

	@Test
	@SneakyThrows
	public void givenValidURlCanExecuteGetTest() {

		givenGetHelloRequestWillReturnOkay();

		SyntheticTest test = this.buildSimpleGetAPITestAction();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	private SyntheticTest buildSimpleGetAPITestAction() {
		return SyntheticTest.builder()
				.name("Test")
				.action(TestActionAPI.builder()
						.name("Simple test to execute GET api call")
						.requestMethod(TestActionAPI.METHOD_GET)
						.requestUrl(TEST_URL + "/hello")
						.expectedStatus(STATUS_200)
						.build())
				.build();
	}

	@Test
	@SneakyThrows
	public void givenValidUrlAndQueryParametersCanExecuteGetTest() {

		givenGetHelloRequestWithQueryParametersWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.action(TestActionAPI.builder()
						.name("Simple test to execute GET api call with query params")
						.requestMethod(TestActionAPI.METHOD_GET)
						.requestUrl(TEST_URL + "/hello?param1=foo&param2=bar")
						.expectedStatus(STATUS_200)
						.build())
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	@Test
	@SneakyThrows
	public void givenValidUrlAndHeadersAndBodyCanExecutePostTest() {

		givenPostRequestWillReturnExpectedMockedResult();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.action(TestActionAPI.builder()
						.name("Simple test to execute POST api call")
						.requestMethod(TestActionAPI.METHOD_POST)
						.requestUrl(TEST_URL + "/submit-data")
						.requestHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.requestBody(MOCK_JSON_REQUEST)
						.expectedStatus(STATUS_200)
						.build())
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreStatusIntoContext() {

		givenGetHelloRequestWillReturnOkay();

		SyntheticTest test = buildSimpleGetAPITestAction();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreOutputIntoContextVariables() {

		givenGetHelloRequestWillReturnOkay();

		SyntheticTest test = this.buildSimpleGetAPITestAction();
		test.getActions().get(0).setPostRequestScript("context.vars.put('var1', context.status)");

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(), STATUS_200);
		Assertions.assertEquals(context.getVars().get("var1"), STATUS_200);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanResolveExpressionInAPIUrlPriorToTestExecution() {

		givenGetHelloRequestWillReturnOkay();

		SyntheticTest test = SyntheticTest.builder()
				.name("Test")
				.action(TestActionAPI.builder()
						.name("Simple test to execute GET api call with pre request script")
						.requestMethod(TestActionAPI.METHOD_GET)
						.requestUrl(TEST_URL + "/{var1}")
						.expectedStatus(STATUS_200)
						.preRequestScript("context.vars.put('var1','hello')")
						.build())
				.build();

		TestExecutionContext context = new TestExecutionContext();
		test.execute(context);
		Assertions.assertEquals(context.getStatus(), STATUS_200);
	}

	private void givenGetHelloRequestWillReturnOkay() {
		stubFor(
			get(urlPathEqualTo("/hello"))
				.willReturn(
					aResponse()
					.withStatus(HttpStatus.SC_OK)
					.withBody("")));
	}

	private void givenGetHelloRequestWithQueryParametersWillReturnOkay() {
		stubFor(
			get(urlPathEqualTo("/hello"))
				.withQueryParam("param1", equalTo("foo"))
				.withQueryParam("param2", equalTo("bar"))
				.willReturn(
					aResponse()
						.withStatus(HttpStatus.SC_OK)
						.withBody("")));
	}

	private void givenPostRequestWillReturnExpectedMockedResult() {
		stubFor(
			post(urlPathEqualTo("/submit-data"))
				.withRequestBody(equalTo(MOCK_JSON_REQUEST))
				.willReturn(
					aResponse()
						.withStatus(HttpStatus.SC_OK)
						.withHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("")));
	}




}
