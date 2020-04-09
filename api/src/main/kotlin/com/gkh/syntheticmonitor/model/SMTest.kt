package com.gkh.syntheticmonitor.model

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.gkh.syntheticmonitor.model.converter.ActionYamlConverter
import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.LogManager
import org.hibernate.annotations.*
import org.hibernate.annotations.Parameter
import java.io.InputStream
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.OrderBy

const val DEFAULT_SCHEDULE_TIME_IN_SECONDS = 15

@Entity
@Slf4j
class SMTest(
        @Id var name: String? = null,
        var description: String? = null,
        var type: String? = null,
        var tags: String? = null,
        var scheduleTimeInSeconds: Int = DEFAULT_SCHEDULE_TIME_IN_SECONDS,
        var isMonitored: Boolean = false,
        var continueActionOnFail: Boolean = false,
        var isActive: Boolean = false
) {

    @Type(type = "org.hibernate.type.SerializableToBlobType",
          parameters = [Parameter(name = "classname", value = "java.util.HashMap")])
    var variables: Map<String, String>? = HashMap()

    @JsonIgnore var isReadyToExecute: Boolean = false
    var timeLastExecuted: Timestamp? = null

    @Convert(converter = ActionYamlConverter::class)
    @Lob
    var actions: List<AbstractSMAction> = arrayListOf()

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumns(value = [JoinColumn(name = "NAME", updatable = false, insertable = false)], foreignKey = ForeignKey(name = "none"))
    @Fetch(FetchMode.SUBSELECT)
    @Where(clause = "DATEDIFF('hour', timestamp, current_timestamp()) < 24")
    @OrderBy(value = "timestamp DESC")
    var reports: MutableList<Report> = arrayListOf()

    companion object {
        var logger = LogManager.getLogger()

        fun fromYAML(yaml: String?): SMTest {
            val mapper = ObjectMapper(YAMLFactory())
            return mapper.readValue(yaml, SMTest::class.java)
        }

        fun fromYAML(stream: InputStream?): SMTest? {
            val mapper = ObjectMapper(YAMLFactory())
            return mapper.readValue(stream, SMTest::class.java)
        }
    }

    fun toYAML(): String {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.writeValueAsString(this)
    }

    @get:JsonGetter
    val ratio24Hour: Double
        get() {
            if (reports.isNotEmpty()) {
                val passed = reports.stream().filter { p: Report -> p.isPassed }.count()
                val total = reports.size.toLong()
                return passed * 1.0 / (total * 1.0) * 100
            }
            return 100.0
        }

    @get:JsonGetter
    val statisticsTestsUnderMaxResponseTime: Long
        get() = reports!!.stream().filter { p: Report -> p.isAllResponseTimeUnderMax }.count()

    @get:JsonGetter
    val statisticsTestsOverMaxResponseTime: Long
        get() = reports!!.stream().filter { p: Report -> !p.isAllResponseTimeUnderMax }.count()

    @get:JsonGetter
    val statisticsTestsPassed: Long
        get() = reports!!.stream().filter { p: Report -> p.isPassed }.count()

    @get:JsonGetter
    val statisticsTestsNotMatchStatusCode: Long
        get() = reports!!.stream().filter { p: Report -> !p.isAllStatusCodeMatching }.count()

    @get:JsonGetter
    val statisticsTestsFailed: Long
        get() = reports!!.stream().filter { p: Report -> !p.isPassed }.count()

    @get:JsonGetter
    val averageResponseTime: Double
        get() = reports!!.stream().mapToDouble { r: Report -> r.sumResponseTime.toDouble() }.summaryStatistics().average

    @get:JsonGetter
    val totalRuns: Double
        get() = reports!!.size.toDouble()

    @get:JsonGetter
    val numberOfActions: Int
        get() = actions!!.size

    @JsonGetter
    fun status(): String {
        // Fixme: "Stable", "Unstable", "Bad", "Critical", "Healthy"
        if (reports.size > 0) {
            val passed = reports[reports.size - 1].isPassed
            return if (passed) {
                "passed"
            } else {
                "failed"
            }
        }
        return ""
    }

    fun execute(context: SMExecutionContext) {

        logger.info("Executing test: ${this.name}")
        context.report.name = name

        // Bind environment variables
        variables?.forEach { (key, value) -> context.vars[key] = value }

        var prevStepFailed = false

        actions.forEach {
            val start = Instant.now()
            var responseTime: Long = 0
            try {
                if (!prevStepFailed || continueActionOnFail) {
                    logger.info("Firing action {}", it.name)
                    if (it.prePauseTimeMillis > 0) {
                        sleep(it.prePauseTimeMillis)
                    }

                    it.preExecuteScript(context)
                    it.expandInputParameters(context)
                    it.execute(context)
                    it.postExecuteScript(context)

                    if (it.postPauseTimeMillis > 0) {
                        sleep(it.postPauseTimeMillis)
                    }

                    val finish = Instant.now()
                    responseTime = Duration.between(start, finish).toMillis()
                    createReport(context, it, responseTime)
                }
            } catch (e: Exception) {
                prevStepFailed = true
                val finish = Instant.now()
                responseTime = Duration.between(start, finish).toMillis()
                val content = e.message
                val status = "ERROR"
                createReport(context, it, responseTime)
                context.content = content
                context.status = status
            }

        }

        logger.info("Status: $context.status Content: $context.content")
        timeLastExecuted = Timestamp(System.currentTimeMillis())
    }

    private fun createReport(context: SMExecutionContext,
                             each: AbstractSMAction,
                             responseTime: Long ) {
        val actionReport = ReportDetail(
                name = each.name,
                details = each.details,
                type = each.type,
                status = context.status,
                content = context.content,
                maximumResponseThreshold = each.maximalResponseThreshold,
                expectedStatus = each.expectedStatus,
                responseTime = responseTime
        )
        context.report.reportDetails.add(actionReport)
    }

    private fun sleep(prePauseTimeMillis: Long) {
        try {
            TimeUnit.MILLISECONDS.sleep(prePauseTimeMillis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


}