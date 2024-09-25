package com.sarkesa.documentanalyser.job.search.repository;

import com.sarkesa.documentanalyser.job.search.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapOfResultsConverterTest {

    private MapOfSearchResultsConverter converter;

    private final String jsonString =
            "{\"Christmas\":[{\"startIndex\":5,\"endIndex\":6},{\"startIndex\":7,\"endIndex\":8}]"
                    + ",\"test\":[{\"startIndex\":1,\"endIndex\":2},{\"startIndex\":3,\"endIndex\":4}]}";
    private Map<String, List<SearchResult>> searchResults;

    @BeforeEach
    void setUp() {
        converter = new MapOfSearchResultsConverter();
        searchResults = new HashMap<>();
        searchResults.put("test", List.of(new SearchResult(1, 2), new SearchResult(3, 4)));
        searchResults.put("Christmas", List.of(new SearchResult(5, 6), new SearchResult(7, 8)));
    }

    @Test
    void convertToDatabaseColumn_shouldConvertToStringCorrectly() {
        String result = converter.convertToDatabaseColumn(searchResults);

        assertEquals(jsonString, result);
    }

    @Test
    void convertToEntityAttribute() {
        Map<String, List<SearchResult>> result = converter.convertToEntityAttribute(jsonString);

        assertEquals(searchResults, result);
    }
}