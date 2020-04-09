package com.gkh.syntheticmonitor.integration

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.gkh.syntheticmonitor.model.SMExecutionContext
import com.gkh.syntheticmonitor.model.SMTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@AutoConfigureWireMock(port = 8090)
class SMActionAPIMockedIntegrationTests {

    companion object {
        var port: Int = 8090
        val YAML_MOCKED_API = """
            ---
            name: MockAPITest
            description: null
            scheduleTimeInSeconds: 60
            type: "Transactions"
            monitored: true
            active: true
            actions:
              - !<API>
                name: "Call mock API"
                type: "API"
                maximalResponseThreshold: 3000
                requestMethod: "GET"
                requestUrl: http://localhost:$port/test
                requestBody: null
                expectedStatus: 200
            """.trimIndent()
    }

    private fun createStubForTestAPI() {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200)))
    }


    @Test
    fun `given valid yaml when API action is executed will return 200 status`() {
        createStubForTestAPI()
        val apiAction = SMTest.fromYAML(YAML_MOCKED_API)
        val context = SMExecutionContext()
        apiAction.execute(context)
        Assertions.assertEquals( "200", context.status)
    }

}