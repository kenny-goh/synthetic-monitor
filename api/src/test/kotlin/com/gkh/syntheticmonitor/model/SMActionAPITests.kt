package com.gkh.syntheticmonitor.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SMActionAPITests {

    @Test
    fun `given valid preRequestScript when executing preExecutionScript shall return expected result`() {
        val action = SMActionAPI()
        action.preRequestScript="""
            context.vars.put("result",true)
        """.trimIndent()

        val context = SMExecutionContext()
        action.preExecuteScript(context)

        Assertions.assertEquals(true, context.vars["result"])
    }

    @Test
    fun `given valid postRequestScript when executing postExecutionScript shall return expected result`() {
        val action = SMActionAPI()
        action.postRequestScript="""
            context.vars.put("result",true)
        """.trimIndent()

        val context = SMExecutionContext()
        action.postExecuteScript(context)

        Assertions.assertEquals(true, context.vars["result"])
    }


    @Test
    fun `given valid template text in request headers and no variable in execution context when expanded then headers will not change`() {
        val action = SMActionAPI()
        action.requestHeaders["Content-Type"] = "\$content_type"
        val context = SMExecutionContext()
        action.expandRequestHeaders(context)
        Assertions.assertEquals("\$content_type", action.requestHeadersExpanded?.get("Content-Type"))
    }

    @Test
    fun `given valid template text in request headers and valid variable in execution context when expanded then headers will have expected values`() {
        val action = SMActionAPI()
        action.requestHeaders["Content-Type"] = "\$content_type"
        val context = SMExecutionContext()
        context.vars.put("content_type","application/json")
        action.expandRequestHeaders(context)
        Assertions.assertEquals("application/json", action.requestHeadersExpanded?.get("Content-Type"))
    }

    @Test
    fun `given valid template text in request body and valid variable in execution context when expanded then body will have expected values`() {
        val action = SMActionAPI()
        action.requestBody="\$foo"

        val context = SMExecutionContext()
        context.vars.put("foo","the red fox jumps over the brown dog")
        action.expandRequestBody(context)

        Assertions.assertEquals("the red fox jumps over the brown dog", action.requestBodyExpanded)
    }

    @Test
    fun `given no template text in request body and and valid variable in execution context when expanded then body will not change`() {
        val action = SMActionAPI()
        action.requestBody="foo"

        val context = SMExecutionContext()
        context.vars.put("foo","the red fox jumps over the brown dog")
        action.expandRequestBody(context)

        Assertions.assertEquals("foo", action.requestBodyExpanded)
    }

    @Test
    fun `given null request body and valid variable in execution context when expanded then body is null`() {
        val action = SMActionAPI()
        val context = SMExecutionContext()

        context.vars.put("foo","the red fox jumps over the brown dog")
        action.expandRequestBody(context)

        Assertions.assertNull(action.requestBodyExpanded)
    }

    @Test
    fun `given valid input when executing expandInputParameters will return expected result`() {
        val action = SMActionAPI()
        action.requestBody="\$body"
        action.requestHeaders["header"] = "\$header"
        action.requestUrl = "http://test.com/{url}"

        val context = SMExecutionContext()
        context.vars["body"] = "test"
        context.vars["header"] = "test"
        context.vars["url"] = "test"

        action.expandInputParameters(context)

        Assertions.assertEquals("http://test.com/test", action.requestUrlExpanded)
        Assertions.assertEquals("test", action.requestBodyExpanded)
        Assertions.assertEquals("test", action.requestHeadersExpanded?.get("header"))
    }
}