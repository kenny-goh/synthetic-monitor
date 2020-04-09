package com.gkh.syntheticmonitor.model

import com.jayway.jsonpath.JsonPath
import lombok.Data
import java.util.*

class SMExecutionContext {

    val report = Report()
    var vars: HashMap<String,String> = HashMap()
    var status: String? = null
    var content: String? = null
    var contentType: String? = null

    /**
     *
     * @param path
     * @return
     */
    fun jsonPath(path: String?): String {
        val jsonContext = JsonPath.parse(content)
        return jsonContext.read(path)
    }
}