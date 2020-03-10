package com.gkh.syntheticmonitor.model.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gkh.syntheticmonitor.model.AbstractSMAction;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import java.util.List;

public class ActionYamlConverter implements AttributeConverter<List<AbstractSMAction>, String> {

	@Override
	@SneakyThrows
	public String convertToDatabaseColumn(List<AbstractSMAction> actions) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		String result = mapper.writeValueAsString(actions);
		return result;
	}

	@Override
	@SneakyThrows
	public List<AbstractSMAction> convertToEntityAttribute(String yaml) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		List<AbstractSMAction> actions = mapper.readValue(yaml,
				new TypeReference<List<AbstractSMAction>>() { });
		return actions;
	}
}
