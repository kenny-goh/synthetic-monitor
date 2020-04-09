package com.gkh.syntheticmonitor.model

import org.junit.Test
import org.junit.jupiter.api.Assertions


class SMActionAPITests {

    @Test
    fun `given valid template text in request headers and no variable in execution context when expanded then expanded request headers will not change`() {
        val action = SMActionAPI()
        action.requestHeaders["Content-Type"] = "\$content_type"
        val context = SMExecutionContext()
        action.expandRequestHeaders(context)
        Assertions.assertEquals("\$content_type", action.requestHeadersExpanded?.get("Content-Type"))
    }

    @Test
    fun `given valid template text in request headers and valid variable in execution context when expanded then expanded request headers will have expected values`() {
        val action = SMActionAPI()
        action.requestHeaders["Content-Type"] = "\$content_type"
        val context = SMExecutionContext()
        context.vars.put("content_type","application/json")
        action.expandRequestHeaders(context)
        Assertions.assertEquals("application/json", action.requestHeadersExpanded?.get("Content-Type"))
    }

    @Test
    fun `given valid template text in request body and valid variable in execution context when expanded then expanded request body will have expected values`() {
        val action = SMActionAPI()
        action.requestBody="\$foo"

        val context = SMExecutionContext()
        context.vars.put("foo","the red fox jumps over the brown dog")
        action.expandRequestBody(context)

        Assertions.assertEquals("the red fox jumps over the brown dog", action.requestBodyExpanded)
    }

    @Test
    fun `given no template text in request body and and valid variable in execution context when expanded then expanded request body will not change`() {
        val action = SMActionAPI()
        action.requestBody="foo"

        val context = SMExecutionContext()
        context.vars.put("foo","the red fox jumps over the brown dog")
        action.expandRequestBody(context)

        Assertions.assertEquals("foo", action.requestBodyExpanded)
    }

    @Test
    fun `given null request body and and valid variable in execution context when expanded then expanded request body is null`() {
        val action = SMActionAPI()

        val context = SMExecutionContext()
        context.vars.put("foo","the red fox jumps over the brown dog")
        action.expandRequestBody(context)

        Assertions.assertNull(action.requestBodyExpanded)
    }
}