package com.gkh.syntheticmonitor.model

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.text.SimpleTemplateEngine
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

    var requestHeaders: Map<String, String> = HashMap()
    var requestMethod: String? = null
    var requestUrl: String? = null
    var requestBody: String? = null

    override var type: String? = null
        get() = "API"

    @Transient
    private var requestBodyExpanded: String? = null
    override val expectedStatus: String? = null

    @JsonIgnore
    @Transient
    private var expandedUrl: String? = null

    @JsonIgnore
    @Transient
    private var expandedRequestHeaders: MutableMap<String, String>? = null

    override fun resolveVariables(context: SMExecutionContext) {
        expandedUrl = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .buildAndExpand(context.vars)
                .toUriString()

        logger.info("Expanded URL {}", expandedUrl)
        val engine = SimpleTemplateEngine()
        if (requestBody != null && requestBody!!.contains("$")) {
            // Fixme: put this to abstract class
            val template = engine.createTemplate(requestBody)
            val textTemplate = template.make(context.vars)
            requestBodyExpanded = textTemplate.toString()
        } else {
            requestBodyExpanded = requestBody
        }
        expandedRequestHeaders = HashMap<String, String>(requestHeaders)
        // fixme: There is more elegant way of doing this in kotlin!
        (expandedRequestHeaders as HashMap<String, String>).forEach { (k, v) ->
            try {
                if (v.contains("$")) {
                    val template = engine.createTemplate(v)
                    val textTemplate = template.make(context.vars)
                    val expandedValue = textTemplate.toString()
                    (expandedRequestHeaders as HashMap<String, String>)[k] = expandedValue
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override val details: String?
        get() = this.requestMethod + " " + requestUrl

    @Throws(Exception::class)
    override fun execute(context: SMExecutionContext) {
        logger.info("API Test Execute")
        val restTemplate = RestTemplate()
        val httpHeaders = HttpHeaders()
        // fixme
        if (this.expandedRequestHeaders != null) {
            this.expandedRequestHeaders!!.keys.forEach { httpHeaders[it] = expandedRequestHeaders!![it] }
        }
        var status: String?
        var content: String?
        try {
            val entity: HttpEntity<*> = HttpEntity<Any?>(requestBodyExpanded, httpHeaders)
            val httpMethod = getHttpMethod(requestMethod)
            val response = restTemplate.exchange(expandedUrl, httpMethod, entity, String::class.java)
            status = Integer.toString(response.statusCode.value())
            content = response.body
            logger.info("Status: {}", status)
            logger.info("Content: {}", content)
        } catch (e: ResourceAccessException) {
            status = "TIMEOUT"
            content = e.message.toString()
        }
        context.content = content
        context.status = status
    }


}