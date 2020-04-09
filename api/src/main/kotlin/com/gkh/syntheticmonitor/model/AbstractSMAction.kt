package com.gkh.syntheticmonitor.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import groovy.lang.Binding
import groovy.lang.GroovyShell
import groovy.text.SimpleTemplateEngine
import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.LogManager
import org.springframework.util.StringUtils
import javax.persistence.Id

@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(JsonSubTypes.Type(value = SMActionAPI::class, name = "API"))
abstract class AbstractSMAction : SMActionInterface {

    companion object {
        var logger = LogManager.getLogger()
    }

    @Id
    override var name: String? = null
    override var type: String? = null

    var description: String? = null
    var postRequestScript: String? = null
    var preRequestScript: String? = null
    var maximalResponseThreshold: Long = 0

    @Transient var prePauseTimeMillis: Long = 0
    @Transient var postPauseTimeMillis: Long = 0

    override fun preExecuteScript(context: SMExecutionContext) {
        if (!StringUtils.isEmpty(preRequestScript)) {
            logger.debug("Executing pre script \"$preRequestScript\"" )
            evalGroovyScript(context, preRequestScript)
        }
    }

    override fun postExecuteScript(context: SMExecutionContext) {
        if (!StringUtils.isEmpty(postRequestScript)) {
            logger.debug("Executing post script \"$postRequestScript\"")
            evalGroovyScript(context, postRequestScript)
        }
    }

    private fun evalGroovyScript(context: SMExecutionContext, script: String?) {
        val binding = Binding()
        binding.setVariable("context", context)
        val shell = GroovyShell(binding)
        shell.evaluate(script)
    }

    fun evalTemplate(context: SMExecutionContext, templateString: String): String {
        val engine = SimpleTemplateEngine()
        val template = engine.createTemplate(templateString)
        val textTemplate = template.make(context.vars)
        return textTemplate.toString()
    }

    override fun expandInputParameters(context: SMExecutionContext) {}

    @get:JsonIgnore abstract val details: String?
    abstract val expectedStatus: String?
}