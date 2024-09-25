package com.sarkesa.documentanalyser.integration;

import com.sarkesa.documentanalyser.GetContentsOfSampleBlog;
import com.sarkesa.documentanalyser.dictionary.model.CreateDictionaryRequest;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.job.JobStatus;
import com.sarkesa.documentanalyser.job.search.model.SearchJob;
import com.sarkesa.documentanalyser.job.search.model.SearchJobRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DocumentAnalyserIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private Integer port;

    private String dictionaryBaseUrl;
    private String jobBaseUrl;

    private static final Set<String> WORDS = Set.of("John", "Mary", "Paul", "classification");

    @BeforeEach
    void setUp() {
        dictionaryBaseUrl = String.format("http://localhost:%d/v1/dictionary", port);
        jobBaseUrl = String.format("http://localhost:%d/v1/job", port);
    }

    @Test
    @Order(1)
    void shouldCreateDictionary() {
        CreateDictionaryRequest request = new CreateDictionaryRequest("Test dictionary", WORDS);

        Dictionary result = testRestTemplate.postForObject(dictionaryBaseUrl, request, Dictionary.class);

        assertEquals(WORDS, result.getWords());
        assertEquals("Test dictionary", result.getName());
        assertNotNull(result.getId());
    }

    @Test
    @Order(2)
    void shouldGetDictionary() {
        // Create another dictionary first
        CreateDictionaryRequest request = new CreateDictionaryRequest("Test dictionary 2", WORDS);
        Dictionary dictionary = testRestTemplate.postForObject(dictionaryBaseUrl, request, Dictionary.class);

        Dictionary result = testRestTemplate.getForObject(dictionaryBaseUrl + "/" + dictionary.getId(), Dictionary.class);

        assertNotNull(result);
        assertEquals(dictionary, result);
    }

    @Test
    @Order(3)
    void shouldDeleteDictionary() {
        // Create another dictionary first
        CreateDictionaryRequest request = new CreateDictionaryRequest("Dictionary to delete", WORDS);
        Dictionary dictionary = testRestTemplate.postForObject(dictionaryBaseUrl, request, Dictionary.class);

        testRestTemplate.delete(dictionaryBaseUrl + "/" + dictionary.getId());

        ResponseEntity<Dictionary> response =
                testRestTemplate.exchange(dictionaryBaseUrl + "/" + dictionary.getId(), HttpMethod.GET, null, Dictionary.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(4)
    void shouldGetAllDictionaries() {
        List<Dictionary> result = getDictionaries();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @Order(5)
    void shouldSubmitJobAndCheckForStatusChange() throws FileNotFoundException, InterruptedException {
        // Create another dictionary first
        CreateDictionaryRequest dictionaryRequest = new CreateDictionaryRequest("Dictionary to job", WORDS);
        Dictionary dictionary = testRestTemplate.postForObject(dictionaryBaseUrl, dictionaryRequest, Dictionary.class);

        SearchJobRequest jobRequest = SearchJobRequest.builder()
                .caseSensitive(true)
                .inputText(GetContentsOfSampleBlog.getContentsFromBlogFile())
                .dictionaryId(dictionary.getId())
                .build();

        SearchJob searchJob = testRestTemplate.postForObject(jobBaseUrl + "/search/submit", jobRequest, SearchJob.class);

        assertNotNull(searchJob);
        assertEquals(dictionary.getId(), searchJob.getDictionaryId());
        assertEquals(JobStatus.SUBMITTED, searchJob.getStatus());
        assertNull(searchJob.getResults());

        // Get the job again and see it is still hasn't been picked up for processing
        searchJob = testRestTemplate.getForObject(jobBaseUrl + "/" + searchJob.getId(), SearchJob.class);
        assertEquals(dictionary.getId(), searchJob.getDictionaryId());
        assertEquals(JobStatus.SUBMITTED, searchJob.getStatus());
        assertNull(searchJob.getResults());

        // Get the job again and see it is still processing
        sleep(200);
        searchJob = testRestTemplate.getForObject(jobBaseUrl + "/" + searchJob.getId(), SearchJob.class);
        assertEquals(dictionary.getId(), searchJob.getDictionaryId());
        assertEquals(JobStatus.PROCESSING, searchJob.getStatus());
        assertNull(searchJob.getResults());

        // Get the job again and see it should have finished
        sleep(2500);
        searchJob = testRestTemplate.getForObject(jobBaseUrl + "/" + searchJob.getId(), SearchJob.class);
        assertEquals(dictionary.getId(), searchJob.getDictionaryId());
        assertEquals(JobStatus.COMPLETE, searchJob.getStatus());

        // Verify the results
        assertEquals(4, searchJob.getResults().size());
        assertEquals(0, searchJob.getResults().get("John").size());
        assertEquals(0, searchJob.getResults().get("Mary").size());
        assertEquals(0, searchJob.getResults().get("Paul").size());
        assertEquals(3, searchJob.getResults().get("classification").size());
    }

    private List<Dictionary> getDictionaries() {
        return testRestTemplate.exchange(
                dictionaryBaseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Dictionary>>() {}
        ).getBody();
    }
}
