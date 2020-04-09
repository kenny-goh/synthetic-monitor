package com.gkh.syntheticmonitor.integration

import com.gkh.syntheticmonitor.model.SMExecutionContext
import com.gkh.syntheticmonitor.model.SMTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SMActionAPIIntegrationTests {
    companion object {
        val YAML_BITCOIN_API = """
            ---
            name: "BitcoinTest"
            description: null
            scheduleTimeInSeconds: 60
            type: "Transactions"
            monitored: true
            active: true
            actions:
              - !<API>
                name: "Call bitcoins API"
                description: null
                type: "API"
                maximalResponseThreshold: 3000
                requestMethod: "GET"
                requestUrl: "https://api.coindesk.com/v1/bpi/currentprice.json"
                requestBody: null
                expectedStatus: "200"
            """.trimIndent()

        val YAML_BAD_BITCOIN_API = """
            ---
            name: "BitcoinTest"
            description: null
            scheduleTimeInSeconds: 60
            type: "Transactions"
            monitored: true
            active: true
            actions:
              - !<API>
                name: "Call bitcoins API"
                description: null
                type: "API"
                maximalResponseThreshold: 3000
                requestMethod: "GET"
                requestUrl: "https://api.coindesk.com/v1/bpi/test/currentprice.json"
                requestBody: null
                expectedStatus: "200"
        """.trimIndent()
    }

    @Test
    fun `given valid yaml can convert to API action`() {
        val apiAction = SMTest.fromYAML(YAML_BITCOIN_API)
        val context = SMExecutionContext()
        apiAction.execute(context)
        Assertions.assertEquals(context.status, "200")
    }

    @Test
    fun `given valid yaml when API action is executed will return 200 status`() {
        val apiAction = SMTest.fromYAML(YAML_BITCOIN_API)
        val context = SMExecutionContext()
        apiAction.execute(context)
        Assertions.assertEquals(context.status, "200")
    }

    @Test
    fun `given valid yaml with bad request url when API action is executed will return ERROR status`() {
        val apiAction = SMTest.fromYAML(YAML_BAD_BITCOIN_API)
        val context = SMExecutionContext()
        apiAction.execute(context)
        Assertions.assertEquals(context.status, "ERROR")
        Assertions.assertTrue(context.content?.contains("404")!!)
    }
}