package com.gkh.syntheticmonitor.model

import com.jayway.jsonpath.JsonPath
import lombok.Data
import java.util.*

/**
 * ExecutionContext is based on context object pattern that allows information to be
 * shared with different layers of the application.
 *
 */
class SMExecutionContext {

    val report = Report()
    var vars: HashMap<String,String> = HashMap()
    var status: String? = null
    var content: String? = null
    var contentType: String? = null

    /**
     * This is a helper function to be used by groovy script to extract value of a json attribute
     * e.g
     * String token = context.jsonPath('$.' + 'requestToken');
     * context.vars.put('token', token);
     *
     * @param path The json path of the attribute
     * @return value of the json attribute
     */
    fun jsonPath(path: String?): String {
        val jsonContext = JsonPath.parse(content)
        return jsonContext.read(path)
    }
}