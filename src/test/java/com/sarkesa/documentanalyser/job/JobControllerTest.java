package com.sarkesa.documentanalyser.job;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarkesa.documentanalyser.error.ApiError;
import com.sarkesa.documentanalyser.job.search.SearchJobService;
import com.sarkesa.documentanalyser.job.search.model.SearchJob;
import com.sarkesa.documentanalyser.job.search.model.SearchJobRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(JobController.class)
class JobControllerTest {

    @MockBean
    private SearchJobService searchJobService;

    @Autowired
    private MockMvc mockMvc;

    private SearchJob searchJobFromService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    private static final String BASE_PATH = "/v1/job";
    private static final String SUBMIT_PATH = BASE_PATH + "/search/submit";
    private static final UUID JOB_ID = UUID.randomUUID();
    private static final UUID DICTIONARY_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        openMocks(this);

        searchJobFromService = SearchJob.builder()
                .id(JOB_ID)
                .dictionaryId(DICTIONARY_ID)
                .inputText("hello")
                .caseSensitive(false)
                .build();
    }

    @Test
    void submitSearchJob_shouldReturn201() throws Exception {
        when(searchJobService.submitJob(any(SearchJobRequest.class))).thenReturn(searchJobFromService);

        SearchJobRequest request = SearchJobRequest.builder()
                .dictionaryId(DICTIONARY_ID)
                .inputText("hello")
                .caseSensitive(false)
                .build();

        final MvcResult result = this.mockMvc
                .perform(post(SUBMIT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        SearchJob searchResult = objectMapper.readValue(result.getResponse().getContentAsString(), SearchJob.class);

        assertNotNull(searchResult);
        assertEquals(searchJobFromService, searchResult);
        verify(searchJobService, times(1)).submitJob(eq(request));
        verifyNoMoreInteractions(searchJobService);
    }

    @Test
    void submitSearchJob_shouldReturn400WhenMissingRequiredFields() throws Exception {

        SearchJobRequest request = SearchJobRequest.builder().build();

        final MvcResult result = this.mockMvc
                .perform(post(SUBMIT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(400));
        assertThat(apiError.path(), is(SUBMIT_PATH));
        verifyNoInteractions(searchJobService);
    }

    @Test
    void getJob_shouldReturn200() throws Exception {
        when(searchJobService.getJob(any(UUID.class))).thenReturn(Optional.of(searchJobFromService));

        final MvcResult result = this.mockMvc
                .perform(get(BASE_PATH + "/" + JOB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        SearchJob searchResult = objectMapper.readValue(result.getResponse().getContentAsString(), SearchJob.class);

        assertNotNull(searchResult);
        assertEquals(searchJobFromService, searchResult);
        verify(searchJobService, times(1)).getJob(eq(JOB_ID));
        verifyNoMoreInteractions(searchJobService);
    }

    @Test
    void getJob_shouldReturn404WhenNotFound() throws Exception {
        when(searchJobService.getJob(any(UUID.class))).thenReturn(Optional.empty());

        final MvcResult result = this.mockMvc
                .perform(get(BASE_PATH + "/" + JOB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(404));
        assertThat(apiError.path(), is(BASE_PATH + "/" + JOB_ID));
        verify(searchJobService, times(1)).getJob(eq(JOB_ID));
        verifyNoMoreInteractions(searchJobService);
    }
}