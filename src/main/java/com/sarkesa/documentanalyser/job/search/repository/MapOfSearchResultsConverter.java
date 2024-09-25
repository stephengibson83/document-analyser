package com.sarkesa.documentanalyser.job.search.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarkesa.documentanalyser.job.search.model.SearchResult;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

@Converter
public class MapOfSearchResultsConverter implements AttributeConverter<Map<String, List<SearchResult>>, String> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(Map<String, List<SearchResult>> attribute) {
        return objectMapper.writeValueAsString(attribute);
    }

    @Override
    @SneakyThrows
    public Map<String, List<SearchResult>> convertToEntityAttribute(String string) {
        return objectMapper.readValue(string, new TypeReference<>() {});
    }
}
