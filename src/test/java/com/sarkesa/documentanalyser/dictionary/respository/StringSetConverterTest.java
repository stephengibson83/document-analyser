package com.sarkesa.documentanalyser.dictionary.respository;

import com.sarkesa.documentanalyser.dictionary.repository.StringSetConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringSetConverterTest {

    private StringSetConverter stringSetConverter;

    @BeforeEach
    void setUp() {
        stringSetConverter = new StringSetConverter();
    }

    @Test
    void convertToDatabaseColumn_shouldConvertToStringCorrectly() {
        Set<String> input = Set.of("apple", "banana", "cola");

        String result = stringSetConverter.convertToDatabaseColumn(input);

        // The order the set is processed in isn't guaranteed so cannot assert on the whole string
        assertTrue(result.contains("apple"));
        assertTrue(result.contains("banana"));
        assertTrue(result.contains("cola"));

        char targetChar = ';';
        long count = result.chars().filter(c -> c == targetChar).count();
        assertEquals(2, count);

    }

    @Test
    void convertToEntityAttribute_shouldConvertBackFromStringCorrectly() {
        String input = "banana;apple;cola";

        Set<String> result = stringSetConverter.convertToEntityAttribute(input);

        assertEquals(3, result.size());
        assertTrue(result.contains("apple"));
        assertTrue(result.contains("banana"));
        assertTrue(result.contains("cola"));
    }
}