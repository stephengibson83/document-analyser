package com.sarkesa.documentanalyser.job.search;

import com.sarkesa.documentanalyser.dictionary.DictionaryService;
import com.sarkesa.documentanalyser.job.JobStatus;
import com.sarkesa.documentanalyser.job.search.model.SearchJob;
import com.sarkesa.documentanalyser.job.search.model.SearchJobRequest;
import com.sarkesa.documentanalyser.job.search.model.SearchResult;
import com.sarkesa.documentanalyser.job.search.repository.SearchJobRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.sleep;

@Service
@Slf4j
public class SearchJobService {

    private final SearchJobRepository searchJobRepository;
    private final DictionarySearchService dictionarySearchService;
    private final DictionaryService dictionaryService;
    private final ExecutorService executor;

    public SearchJobService(final SearchJobRepository searchJobRepository,
                            final DictionarySearchService dictionarySearchService,
                            final DictionaryService dictionaryService,
                            final ExecutorService executor) {
        this.searchJobRepository = searchJobRepository;
        this.dictionarySearchService = dictionarySearchService;
        this.dictionaryService = dictionaryService;
        this.executor = executor;
    }

    public SearchJob submitJob(final SearchJobRequest searchJobRequest) {
        // verify dictionary exists
        dictionaryService.getDictionary(searchJobRequest.dictionaryId());

        final SearchJob searchJobToSave = SearchJob.builder()
                .caseSensitive(searchJobRequest.caseSensitive())
                .dictionaryId(searchJobRequest.dictionaryId())
                .inputText(searchJobRequest.inputText())
                .status(JobStatus.SUBMITTED)
                .build();

        final SearchJob savedSearchJob = searchJobRepository.save(searchJobToSave);

        // Submitting to execute asynchronously
        log.debug("Submitted saved search job for processing: {}", savedSearchJob);
        this.executor.submit(() -> processJob(savedSearchJob));

        return savedSearchJob;
    }

    public Optional<SearchJob> getJob(final UUID jobId) {
        return searchJobRepository.findById(jobId);
    }

    @SneakyThrows
    protected void processJob(final SearchJob searchJob) {
        log.debug("Processing searchJob {}", searchJob);

        sleep(100); // This is only here to make it obvious that the initial response is returned to client and this is processing in a separate thread
        log.debug("After 1st sleep to purposely slow down service"); // TODO remove

        searchJob.setStatus(JobStatus.PROCESSING);
        SearchJob savedJob = searchJobRepository.save(searchJob);
        log.debug("Saved updated status for searchJob {}", savedJob);

        sleep(2000); // This is only here to make it obvious that the initial response is returned to client and this is processing in a separate thread
        log.debug("After 2nd sleep to purposely slow down service"); // TODO remove

        try {
            final Map<String, List<SearchResult>> searchResults =
                    dictionarySearchService.searchTextWithDictionary(savedJob.getDictionaryId(), savedJob.getInputText(), savedJob.isCaseSensitive());

            savedJob.setStatus(JobStatus.COMPLETE);
            savedJob.setResults(searchResults);
        } catch (Exception ex) {
            log.error("Error encountered when processing job ID [{}]. Error message is [{}]", savedJob.getId(), ex.getMessage());

            // If there was any issue processing the job then mark as failed
            // TODO BEYOND MVP - add more detail why the job failed so the client can decide if it should retry
            savedJob.setStatus(JobStatus.FAILED);
        }

        searchJobRepository.save(savedJob);
        log.debug("Saved updated status for searchJob {}", savedJob);
    }
}
