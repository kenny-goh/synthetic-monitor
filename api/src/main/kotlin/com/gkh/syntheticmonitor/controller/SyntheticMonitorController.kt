package com.gkh.syntheticmonitor.controller

import com.gkh.syntheticmonitor.model.SMTest
import com.gkh.syntheticmonitor.service.ApplicationService
import lombok.SneakyThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SyntheticMonitorController(val appService: ApplicationService) {

    @GetMapping(value = ["/find_all_tests"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTests(): ResponseEntity<List<SMTest>> {
        val results = appService.findAllTests()
        val httpHeaders = HttpHeaders()
        httpHeaders["Access-Control-Allow-Origin"] = "*"
        return ResponseEntity(results, httpHeaders, HttpStatus.OK)
    }

    @PostMapping(value = ["toggle_test"])
    @SneakyThrows
    fun toggleTest(@RequestParam testName: String?): ResponseEntity<String> {
        val result = appService.toggleTests(testName!!)
        val httpHeaders = HttpHeaders()
        httpHeaders["Access-Control-Allow-Origin"] = "*"
        return ResponseEntity(result.toString(), httpHeaders, HttpStatus.OK)
    }

    @PostMapping(value = ["toggle_monitored"])
    @SneakyThrows
    fun toggleMonitored(@RequestParam testName: String?): ResponseEntity<String> {
        val result = appService.toggleMonitored(testName!!)
        val httpHeaders = HttpHeaders()
        httpHeaders["Access-Control-Allow-Origin"] = "*"
        return ResponseEntity(result.toString(), httpHeaders, HttpStatus.OK)
    }

    @PostMapping(value = ["execute_test"])
    @SneakyThrows
    fun executeTest(@RequestParam testName: String?): ResponseEntity<SMTest> {
        val test = appService.executeTest(testName!!)
        val httpHeaders = HttpHeaders()
        httpHeaders["Access-Control-Allow-Origin"] = "*"
        return ResponseEntity(test, httpHeaders, HttpStatus.OK)
    }
}