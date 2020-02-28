package com.gkh.syntheticmonitor.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {
	public String dumpAsJSON(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		return jsonOutput;
	}
}
