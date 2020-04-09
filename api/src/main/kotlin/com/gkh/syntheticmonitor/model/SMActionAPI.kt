package com.gkh.syntheticmonitor.model

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Slf4j
class SMActionAPI : AbstractSMAction() {

    companion object {
        var logger = LogManager.getLogger()

        // Todo: SSL certificate support?
        const val type = "API"
        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_DELETE = "DELETE"

        @Throws(Exception::class)
        private fun getHttpMethod(method: String?): HttpMethod {
            if (method == METHOD_GET) return HttpMethod.GET
            if (method == METHOD_POST) return HttpMethod.POST
            if (method == METHOD_PUT) return HttpMethod.PUT
            if (method == METHOD_DELETE) return HttpMethod.DELETE
            throw Exception("API Method not supported:$method")
        }

    }

    var requestHeaders: MutableMap<String, String> = HashMap()
    var requestMethod: String? = null
    var requestUrl: String? = null
    var requestBody: String? = null

    override var type: String? = null
        get() = "API"

    @Transient
    var requestBodyExpanded: String? = null

    @JsonIgnore
    @Transient
    var urlExpanded: String? = null

    @JsonIgnore
    @Transient
    var requestHeadersExpanded: MutableMap<String, String>? = null

    override val expectedStatus: String? = null

    override fun expandInputParameters(context: SMExecutionContext) {
        this.expandURL(context)
        this.expandRequestBody(context)
        this.expandRequestHeaders(context)
    }

    fun expandRequestHeaders(context: SMExecutionContext) {
        val expandedRequestHeaders = HashMap<String, String>(requestHeaders)
        expandedRequestHeaders.forEach { (key, value) ->
            try {
                if (value.contains("$"))
                    expandedRequestHeaders[key] = evalTemplate(context, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        this.requestHeadersExpanded = expandedRequestHeaders
    }

    private fun expandURL(context: SMExecutionContext) {
        urlExpanded = UriComponentsBuilder.fromHttpUrl(this.requestUrl!!)
                .buildAndExpand(context.vars)
                .toUriString()
        logger.info("Expanded URL $urlExpanded")
    }

    fun expandRequestBody(context: SMExecutionContext) {
        requestBodyExpanded = if (this.requestBody?.contains("$") == true) {
            evalTemplate(context, requestBody!!)
        } else {
            this.requestBody
        }
        logger.info("Expanded request body $requestBodyExpanded")
    }

    override val details: String?
        get() = this.requestMethod + " " + requestUrl

    @Throws(Exception::class)
    override fun execute(context: SMExecutionContext) {
        logger.info("API Test Execute")
        val restTemplate = RestTemplate()
        val httpHeaders = HttpHeaders()

        this.requestHeadersExpanded?.let {
            this.requestHeadersExpanded!!.keys.forEach {
                httpHeaders[it] = requestHeadersExpanded!![it]
            }
        }

        var status: String?
        var content: String?

        try {
            val entity: HttpEntity<*> = HttpEntity<Any?>(requestBodyExpanded, httpHeaders)
            val httpMethod = getHttpMethod(requestMethod)
            val response = restTemplate.exchange(urlExpanded!!, httpMethod, entity, String::class.java)

            status = Integer.toString(response.statusCode.value())
            content = response.body

            logger.info("Status: $status")
            logger.info("Content: $content")
        } catch (e: ResourceAccessException) {
            status = "TIMEOUT"
            content = e.message.toString()
        }
        context.content = content
        context.status = status
    }


}