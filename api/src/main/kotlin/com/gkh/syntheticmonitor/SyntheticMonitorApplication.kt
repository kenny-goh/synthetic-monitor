package com.gkh.syntheticmonitor

import com.gkh.syntheticmonitor.service.ApplicationService
import lombok.extern.slf4j.Slf4j
import org.apache.logging.log4j.LogManager
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import javax.annotation.PostConstruct

// Todo: Agent to execute action remotely?
// Todo: Plugin architecture
@SpringBootApplication
@Slf4j
open class SyntheticMonitorApplication(val appService: ApplicationService) {

    @PostConstruct
    private fun init() {
        logger.info("Initializing SyntheticMonitor ...")
        appService.init()
    }

    companion object {
        private val logger = LogManager.getLogger()
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SyntheticMonitorApplication::class.java, *args)
        }
    }
}

