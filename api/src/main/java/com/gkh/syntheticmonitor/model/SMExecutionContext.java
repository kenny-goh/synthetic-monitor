package com.gkh.syntheticmonitor.model;

import com.jayway.jsonpath.JsonPath;
import lombok.Data;

import java.util.HashMap;

@Data
public class SMExecutionContext {
	private Report report = new Report();
	private HashMap vars = new HashMap();
	private String status;
	private String content;
	private String contentType;

	/**
	 *
	 * @param path
	 * @return
	 */
	public String jsonPath(String path) {
		var jsonContext =  JsonPath.parse(this.content);
		String value = jsonContext.read(path);
		return value;
	}

}