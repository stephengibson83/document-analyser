package com.sarkesa.documentanalyser.job.search;

import com.sarkesa.documentanalyser.dictionary.DictionaryService;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.job.search.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DictionarySearchService {
    private final DictionaryService dictionaryService;

    public DictionarySearchService(final DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public Map<String, List<SearchResult>> searchTextWithDictionary(final UUID dictionaryId,
                                                                    final String searchText,
                                                                    final boolean caseSensitive) {
        final Dictionary dictionary = dictionaryService.getDictionary(dictionaryId);
        log.debug("For ID [{}], found Dictionary [{}]", dictionaryId, dictionary);

        final Map<String, List<SearchResult>> results = new ConcurrentHashMap<>();

        // Making all the text the same case if the request is not case sensitive
        final String searchTextToUse = caseSensitive ? searchText : searchText.toLowerCase();

        dictionary.getWords().parallelStream().forEach(word -> {
            log.debug("Processing Word [{}]", word);

            // Making the word lower case if request was not case sensitive
            final String wordToUse = caseSensitive ? word : word.toLowerCase();

            int wordLength = wordToUse.length();

            final List<SearchResult> resultsForWord = new ArrayList<>();

            int index = searchTextToUse.indexOf(wordToUse);
            log.debug("First index for word [{}] is: [{}]", word, index);

            while (index != -1) {
                int endOfWordIndex = index + wordLength;
                resultsForWord.add(new SearchResult(index, endOfWordIndex));

                index = searchTextToUse.indexOf(wordToUse, endOfWordIndex + 1);
                log.debug("Next index for word [{}] is: [{}]", word, index);
            }

            log.debug("Results for word [{}] are: [{}]", word, resultsForWord);
            results.put(word, resultsForWord);
        });

        return results;
    }
}
