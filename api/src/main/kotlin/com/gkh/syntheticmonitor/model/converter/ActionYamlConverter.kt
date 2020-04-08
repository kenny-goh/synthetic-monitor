package com.gkh.syntheticmonitor.model.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.gkh.syntheticmonitor.model.AbstractSMAction
import lombok.SneakyThrows
import javax.persistence.AttributeConverter

class ActionYamlConverter : AttributeConverter<List<AbstractSMAction?>?, String> {
    @SneakyThrows
    override fun convertToDatabaseColumn(actions: List<AbstractSMAction?>?): String {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.writeValueAsString(actions)
    }

    @SneakyThrows
    override fun convertToEntityAttribute(yaml: String): List<AbstractSMAction?>? {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue<List<AbstractSMAction?>>(yaml,
                object : TypeReference<List<AbstractSMAction?>?>() {})
    }
}