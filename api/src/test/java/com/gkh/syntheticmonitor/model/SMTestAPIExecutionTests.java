package com.gkh.syntheticmonitor.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Profile;
import wiremock.org.apache.http.HttpStatus;
import wiremock.org.apache.http.protocol.HTTP;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureWireMock
@Profile("test")
public class SMTestAPIExecutionTests extends BaseSyntheticTestSpringSupport {


	public static final String MOCK_JSON_REQUEST = "{'data':'blah'}";
	public static final String STATUS_200 = "200";

	@Test
	@SneakyThrows
	public void givenValidURlCanExecuteGetTest() {

		givenGetHelloRequestWillReturnOkay();

		SMTest test = this.buildSimpleGetAPITestAction();

		SMExecutionContext context = new SMExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	private SMTest buildSimpleGetAPITestAction() {
		return SMTest.builder()
				.name("Test")
				.action(SMActionAPI.builder()
						.name("Simple test to execute GET api call")
						.requestMethod(SMActionAPI.METHOD_GET)
						.requestUrl(TEST_URL + "/hello")
						.expectedStatus(STATUS_200)
						.build())
				.build();
	}

	@Test
	@SneakyThrows
	public void givenValidUrlAndQueryParametersCanExecuteGetTest() {

		givenGetHelloRequestWithQueryParametersWillReturnOkay();

		SMTest test = SMTest.builder()
				.name("Test")
				.action(SMActionAPI.builder()
						.name("Simple test to execute GET api call with query params")
						.requestMethod(SMActionAPI.METHOD_GET)
						.requestUrl(TEST_URL + "/hello?param1=foo&param2=bar")
						.expectedStatus(STATUS_200)
						.build())
				.build();

		SMExecutionContext context = new SMExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	@Test
	@SneakyThrows
	public void givenValidUrlAndHeadersAndBodyCanExecutePostTest() {

		givenPostRequestWillReturnExpectedMockedResult();

		SMTest test = SMTest.builder()
				.name("Test")
				.action(SMActionAPI.builder()
						.name("Simple test to execute POST api call")
						.requestMethod(SMActionAPI.METHOD_POST)
						.requestUrl(TEST_URL + "/submit-data")
						.requestHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.requestBody(MOCK_JSON_REQUEST)
						.expectedStatus(STATUS_200)
						.build())
				.build();

		SMExecutionContext context = new SMExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreStatusIntoContext() {

		givenGetHelloRequestWillReturnOkay();

		SMTest test = buildSimpleGetAPITestAction();

		SMExecutionContext context = new SMExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(),STATUS_200);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanStoreOutputIntoContextVariables() {

		givenGetHelloRequestWillReturnOkay();

		SMTest test = this.buildSimpleGetAPITestAction();
		test.getActions().get(0).setPostRequestScript("context.vars.put('var1', context.status)");

		SMExecutionContext context = new SMExecutionContext();
		test.execute(context);

		Assertions.assertEquals(context.getStatus(), STATUS_200);
		Assertions.assertEquals(context.getVars().get("var1"), STATUS_200);
	}

	@Test
	@SneakyThrows
	public void syntheticTestCanResolveExpressionInAPIUrlPriorToTestExecution() {

		givenGetHelloRequestWillReturnOkay();

		SMTest test = SMTest.builder()
				.name("Test")
				.action(SMActionAPI.builder()
						.name("Simple test to execute GET api call with pre request script")
						.requestMethod(SMActionAPI.METHOD_GET)
						.requestUrl(TEST_URL + "/{var1}")
						.expectedStatus(STATUS_200)
						.preRequestScript("context.vars.put('var1','hello')")
						.build())
				.build();

		SMExecutionContext context = new SMExecutionContext();
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
