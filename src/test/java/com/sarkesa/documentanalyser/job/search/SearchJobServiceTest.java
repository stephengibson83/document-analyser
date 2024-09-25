package com.sarkesa.documentanalyser.job.search;

import com.sarkesa.documentanalyser.dictionary.DictionaryService;
import com.sarkesa.documentanalyser.dictionary.error.DictionaryNotFoundException;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.job.JobStatus;
import com.sarkesa.documentanalyser.job.search.model.SearchJob;
import com.sarkesa.documentanalyser.job.search.model.SearchJobRequest;
import com.sarkesa.documentanalyser.job.search.model.SearchResult;
import com.sarkesa.documentanalyser.job.search.repository.SearchJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class SearchJobServiceTest {

    @Mock
    private SearchJobRepository searchJobRepository;
    @Mock
    private DictionarySearchService dictionarySearchService;
    @Mock
    private DictionaryService dictionaryService;
    @Mock
    private ExecutorService executor;
    @Captor
    ArgumentCaptor<SearchJob> searchJobCaptor;

    private SearchJobService searchJobService;
    private Dictionary dictionary;

    private static final UUID JOB_ID = UUID.randomUUID();
    private static final UUID DICTIONARY_ID = UUID.randomUUID();
    private static final Set<String> WORDS = Set.of("John", "Mary", "Paul");

    @BeforeEach
    void setUp() {
        openMocks(this);
        searchJobService = new SearchJobService(searchJobRepository, dictionarySearchService, dictionaryService, executor);

        dictionary = Dictionary.builder()
                .id(DICTIONARY_ID)
                .name("Test dictionary")
                .words(WORDS)
                .build();
    }

    @Test
    void submitJob_shouldProcessSuccessfully() {
        when(dictionaryService.getDictionary(any(UUID.class))).thenReturn(dictionary);

        SearchJobRequest searchJobRequest = SearchJobRequest.builder()
                .caseSensitive(false)
                .dictionaryId(DICTIONARY_ID)
                .inputText("Mary had a little lamb")
                .build();

        final SearchJob searchJobToSave = SearchJob.builder()
                .caseSensitive(searchJobRequest.caseSensitive())
                .dictionaryId(searchJobRequest.dictionaryId())
                .inputText(searchJobRequest.inputText())
                .status(JobStatus.SUBMITTED)
                .build();

        Map<String, List<SearchResult>> wordResults = new HashMap<>();
        wordResults.put("Mary", List.of(SearchResult.builder().startIndex(0).endIndex(4).build()));

        final SearchJob searchJobFromDb = SearchJob.builder()
                .id(JOB_ID)
                .caseSensitive(searchJobRequest.caseSensitive())
                .dictionaryId(searchJobRequest.dictionaryId())
                .inputText(searchJobRequest.inputText())
                .status(JobStatus.SUBMITTED)
                .results(wordResults)
                .build();

        when(searchJobRepository.save(any(SearchJob.class))).thenReturn(searchJobFromDb);

        SearchJob result = searchJobService.submitJob(searchJobRequest);

        assertNotNull(result);
        assertEquals(searchJobFromDb, result);
        verify(searchJobRepository, times(1)).save(searchJobToSave);
        verify(dictionaryService, times(1)).getDictionary(DICTIONARY_ID);
        verify(executor, times(1)).submit(any(Runnable.class));
        verifyNoMoreInteractions(searchJobRepository, searchJobRepository, dictionarySearchService, dictionaryService);
    }

    @Test
    void submitJob_shouldThrowExceptionWhenDictionaryNotFound() {
        when(dictionaryService.getDictionary(any(UUID.class))).thenThrow(new DictionaryNotFoundException(DICTIONARY_ID));

        SearchJobRequest searchJobRequest = SearchJobRequest.builder()
                .dictionaryId(DICTIONARY_ID)
                .build();

        assertThrows(DictionaryNotFoundException.class, () -> searchJobService.submitJob(searchJobRequest));
        verify(dictionaryService, times(1)).getDictionary(DICTIONARY_ID);
        verifyNoMoreInteractions(searchJobRepository, searchJobRepository, dictionarySearchService, dictionaryService);
    }

    @Test
    void getJob_shouldReturnJob() {
        SearchJob searchJob = SearchJob.builder().id(JOB_ID).build();

        when(searchJobRepository.findById(any(UUID.class))).thenReturn(Optional.of(searchJob));

        Optional<SearchJob> result =  searchJobService.getJob(JOB_ID);

        assertNotNull(result);
        assertEquals(searchJob, result.get());
        verify(searchJobRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(searchJobRepository, searchJobRepository, dictionarySearchService, dictionaryService);
    }

    @Test
    void processJob_shouldProcessSuccessfully() {
        Map<String, List<SearchResult>> searchResults = new HashMap<>();
        searchResults.put("test", List.of(new SearchResult(1, 2), new SearchResult(3, 4)));
        searchResults.put("Christmas", List.of(new SearchResult(5, 6), new SearchResult(7, 8)));

        final SearchJob requestSearchJob = SearchJob.builder().id(JOB_ID).caseSensitive(false).inputText("hello").status(JobStatus.SUBMITTED).build();
        final SearchJob processingSearchJob = SearchJob.builder().id(JOB_ID).caseSensitive(false).inputText("hello").status(JobStatus.PROCESSING).build();
        final SearchJob completeSearchJob =
                SearchJob.builder().id(JOB_ID).caseSensitive(false).inputText("hello").results(searchResults).status(JobStatus.COMPLETE).build();
        when(searchJobRepository.save(eq(processingSearchJob))).thenReturn(processingSearchJob);
        when(searchJobRepository.save(eq(completeSearchJob))).thenReturn(completeSearchJob);
        when(dictionarySearchService.searchTextWithDictionary(any(), anyString(), anyBoolean())).thenReturn(searchResults);

        searchJobService.processJob(requestSearchJob);

        verify(dictionarySearchService, times(1)).searchTextWithDictionary(any(), anyString(), eq(false));
        verify(searchJobRepository, times(2)).save(searchJobCaptor.capture());
        verifyNoMoreInteractions(searchJobRepository, searchJobRepository, dictionarySearchService, dictionaryService);

        assertEquals(2, searchJobCaptor.getAllValues().size());
        assertEquals(JobStatus.PROCESSING, searchJobCaptor.getAllValues().get(0).getStatus());
        assertEquals(JobStatus.COMPLETE, searchJobCaptor.getAllValues().get(1).getStatus());
    }

    @Test
    void processJob_shouldHandleDownstreamException() {
        final SearchJob requestSearchJob = SearchJob.builder().id(JOB_ID).caseSensitive(false).inputText("hello").status(JobStatus.SUBMITTED).build();
        final SearchJob processingSearchJob = SearchJob.builder().id(JOB_ID).caseSensitive(false).inputText("hello").status(JobStatus.PROCESSING).build();
        final SearchJob failedSearchJob = SearchJob.builder().id(JOB_ID).caseSensitive(false).inputText("hello").status(JobStatus.FAILED).build();
        when(searchJobRepository.save(eq(processingSearchJob))).thenReturn(processingSearchJob);
        when(searchJobRepository.save(eq(failedSearchJob))).thenReturn(failedSearchJob);
        when(dictionarySearchService.searchTextWithDictionary(any(), anyString(), anyBoolean())).thenThrow(new RuntimeException());

        searchJobService.processJob(requestSearchJob);

        verify(dictionarySearchService, times(1)).searchTextWithDictionary(any(), anyString(), eq(false));
        verify(searchJobRepository, times(2)).save(searchJobCaptor.capture());
        verifyNoMoreInteractions(searchJobRepository, searchJobRepository, dictionarySearchService, dictionaryService);

        assertEquals(2, searchJobCaptor.getAllValues().size());
        assertEquals(JobStatus.PROCESSING, searchJobCaptor.getAllValues().get(0).getStatus());
        assertEquals(JobStatus.FAILED, searchJobCaptor.getAllValues().get(1).getStatus());
    }
}