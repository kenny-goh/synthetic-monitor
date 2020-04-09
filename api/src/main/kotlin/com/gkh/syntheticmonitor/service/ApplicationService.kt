package com.gkh.syntheticmonitor.service

import com.gkh.syntheticmonitor.exception.SyntheticTestException
import com.gkh.syntheticmonitor.model.SMExecutionContext
import com.gkh.syntheticmonitor.model.SMTest
import com.gkh.syntheticmonitor.repository.ReportRepository
import com.gkh.syntheticmonitor.repository.SMTestRepository
import lombok.SneakyThrows
import lombok.extern.slf4j.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Timestamp
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
@Slf4j
@Transactional
open class ApplicationService(
        val repository: SMTestRepository,
        val reportRepository: ReportRepository
) {
    @Value("\${app.settings.tests_yaml_path}")
    lateinit var bootstrapYamlPath: String

    companion object {
        private val logger = LogManager.getLogger()
    }

    open fun init() { // Sync YAML and delete
        val filesInFolder = Files.walk(Paths.get(bootstrapYamlPath))
                .filter { p: Path -> p.toString().endsWith(".yml") }
                .map { obj: Path -> obj.toFile() }
                .collect(Collectors.toList())
        logger.info("List of files {}", filesInFolder)
        filesInFolder.forEach(Consumer { file: File ->
            try {
                logger.info("Reading file {}", file.name)
                val data = FileUtils.readFileToString(file, "UTF-8")
                val test = SMTest.fromYAML(data)
                test.timeLastExecuted = Timestamp(System.currentTimeMillis())
                repository.save(test)
            } catch (e: Exception) {
                logger.error(e.message, *e.stackTrace)
                e.printStackTrace()
            }
        })
    }

    open fun findAllTests(): List<SMTest> {
        return repository.selectTests()
    }

    @Throws(Exception::class)
    open fun toggleTests(testName: String): Boolean {
        val optional = repository.findById(testName)
        return if (optional.isPresent) {
            val test = optional.get()
            test.isActive = !test.isActive
            test.isActive
        } else {
            throw Exception("Test not found:$testName")
        }
    }

    @Throws(Exception::class)
    open fun toggleMonitored(testName: String): Boolean {
        val optional = repository.findById(testName)
        return if (optional.isPresent) {
            val test = optional.get()
            test.isMonitored = !test.isMonitored
            test.isMonitored
        } else {
            throw Exception("Test not found:$testName")
        }
    }

    @Throws(SyntheticTestException::class)
    open fun executeTest(testName: String): SMTest {
        val optional = repository.findById(testName)
        return if (optional.isPresent) {
            val test = optional.get()
            val context = SMExecutionContext()
            test.execute(context)
            val report = context.report
            reportRepository.save(report)
            test
        } else {
            throw SyntheticTestException("Test not found:$testName")
        }
    }

    open fun checkTestsReadyToFire() {
        repository.updateReadyToExecute()
    }

    open fun executeNextTests() {
        val tests = repository.selectReadyToExecuteTests()
        tests.forEach {
            try {
                logger.info("Firing test: ${it.name}")
                val context = SMExecutionContext()
                it.execute(context)
                val report = context.report
                it.isReadyToExecute = false
                reportRepository.save(report)
            } catch (e: Exception) {
                logger.error(e.message)
            } finally {
                it.isReadyToExecute = false
            }
        }
    }
}