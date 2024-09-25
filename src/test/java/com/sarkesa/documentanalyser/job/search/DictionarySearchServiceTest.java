package com.sarkesa.documentanalyser.job.search;

import com.sarkesa.documentanalyser.GetContentsOfSampleBlog;
import com.sarkesa.documentanalyser.dictionary.DictionaryService;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.job.search.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DictionarySearchServiceTest {

    @Mock
    private DictionaryService dictionaryService;

    private DictionarySearchService dictionarySearchService;
    private String inputText;

    private static final UUID DICTIONARY_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws FileNotFoundException {
        openMocks(this);

        dictionarySearchService = new DictionarySearchService(dictionaryService);

        inputText = GetContentsOfSampleBlog.getContentsFromBlogFile();
    }

    @ParameterizedTest
    @CsvSource({
            "true , Ohalo     , 3 , 434 , 439",
            "false, Ohalo     , 3 , 434 , 439",
            "true , The       , 8 , 1255, 1258",
            "false, The       , 53, 224 , 227",
            "true , fintech   , 14, 693 , 700",
            "false, fintech   , 17, 693 , 700",
            "true,  Data X-Ray, 5 , 2093, 2103",
            "false, Data X-Ray, 5 , 2093, 2103",
            "true , word      , 0 ,     , ",
            "false, word      , 0 ,     , ",
            "true , Joe       , 0 ,     , ",
            "false, Joe       , 0 ,     , ",
    })
    void searchTextWithDictionary(final boolean caseSensitive,
                                  final String wordBeingTested,
                                  final int expectedMatchesCount,
                                  final Integer startIndexForFirstMatch,
                                  final Integer endIndexForFirstMatch)  {
        Dictionary dictionary = Dictionary.builder()
                .id(DICTIONARY_ID)
                .words(Set.of("Ohalo", "The", "fintech", "Data X-Ray", "word", "Joe"))
                .build();

        when(dictionaryService.getDictionary(any(UUID.class))).thenReturn(dictionary);

        Map<String, List<SearchResult>> result = dictionarySearchService.searchTextWithDictionary(DICTIONARY_ID, inputText, caseSensitive);

        assertNotNull(result);
        assertEquals(6, result.size());

        assertEquals(expectedMatchesCount, result.get(wordBeingTested).size());

        if (expectedMatchesCount > 1) {
            assertEquals(startIndexForFirstMatch, result.get(wordBeingTested).get(0).startIndex());
            assertEquals(endIndexForFirstMatch, result.get(wordBeingTested).get(0).endIndex());
        }
    }
}