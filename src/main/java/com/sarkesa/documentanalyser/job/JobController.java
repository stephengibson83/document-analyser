package com.sarkesa.documentanalyser.job;

import com.sarkesa.documentanalyser.job.error.JobNotFoundException;
import com.sarkesa.documentanalyser.job.search.SearchJobService;
import com.sarkesa.documentanalyser.job.search.model.SearchJob;
import com.sarkesa.documentanalyser.job.search.model.SearchJobRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping(value = "/v1/job")
public class JobController {

    private final SearchJobService searchJobService;

    public JobController(final SearchJobService searchJobService) {
        this.searchJobService = searchJobService;
    }

    @PostMapping("/search/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public SearchJob submitSearchJob(@RequestBody @Valid final SearchJobRequest searchJobRequest) {
        log.trace("Submitting search job request: {}", searchJobRequest);
        final SearchJob searchJob = searchJobService.submitJob(searchJobRequest);
        log.trace("New search job: {}", searchJob);
        return searchJob;
    }

    @GetMapping("/{jobId}")
    public Job getJob(@PathVariable final UUID jobId) {
        log.trace("Getting job: {}", jobId);
        final Job job = searchJobService.getJob(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
        log.trace("Found job: {}", job);
        return job;
    }
}
