package com.sarkesa.documentanalyser.dictionary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarkesa.documentanalyser.dictionary.error.DictionaryNotFoundException;
import com.sarkesa.documentanalyser.dictionary.model.AddWordsRequest;
import com.sarkesa.documentanalyser.dictionary.model.CreateDictionaryRequest;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.dictionary.model.RemoveWordsRequest;
import com.sarkesa.documentanalyser.error.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
class DictionaryControllerTest {

    @MockBean
    private DictionaryService dictionaryService;

    @Autowired
    private MockMvc mockMvc;

    private Dictionary dictionaryFromService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    private static final String BASE_PATH = "/v1/dictionary";
    private static final UUID DICTIONARY_ID = UUID.randomUUID();
    private static final Set<String> WORDS = Set.of("John", "Mary", "Paul");

    @BeforeEach
    void setUp() {
        openMocks(this);

        dictionaryFromService = Dictionary.builder()
                .id(DICTIONARY_ID)
                .name("Test dictionary")
                .words(WORDS)
                .build();
    }

    @Test
    void createDictionary_shouldReturn201() throws Exception {
        when(dictionaryService.createDictionary(any(CreateDictionaryRequest.class))).thenReturn(dictionaryFromService);

        CreateDictionaryRequest request = new CreateDictionaryRequest("Test dictionary", WORDS);

        final MvcResult result = this.mockMvc
                .perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        Dictionary dictionaryResult = objectMapper.readValue(result.getResponse().getContentAsString(), Dictionary.class);

        assertEquals(dictionaryFromService, dictionaryResult);
        verify(dictionaryService, times(1)).createDictionary(any(CreateDictionaryRequest.class));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void createDictionary_shouldReturn400WhenMissingRequiredFields() throws Exception {
        final MvcResult result = this.mockMvc
                .perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(400));
        assertThat(apiError.path(), is(BASE_PATH));
        verifyNoInteractions(dictionaryService);
    }

    @Test
    void getAllDictionaries_shouldReturn200() throws Exception {
        when(dictionaryService.getAllDictionaries()).thenReturn(List.of(dictionaryFromService));

        final MvcResult result = this.mockMvc
                .perform(get(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<Dictionary> dictionaries = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(1, dictionaries.size());
        assertEquals(dictionaryFromService, dictionaries.get(0));
        verify(dictionaryService, times(1)).getAllDictionaries();
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void getDictionary_shouldReturn200() throws Exception {
        when(dictionaryService.getDictionary(any(UUID.class))).thenReturn(dictionaryFromService);

        final MvcResult result = this.mockMvc
                .perform(get(BASE_PATH + "/" + DICTIONARY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Dictionary dictionaryResult = objectMapper.readValue(result.getResponse().getContentAsString(), Dictionary.class);

        assertEquals(dictionaryFromService, dictionaryResult);
        verify(dictionaryService, times(1)).getDictionary(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void getDictionary_shouldReturn404WhenNotFound() throws Exception {
        when(dictionaryService.getDictionary(any(UUID.class))).thenThrow(new DictionaryNotFoundException(DICTIONARY_ID));

        final MvcResult result = this.mockMvc
                .perform(get(BASE_PATH + "/" + DICTIONARY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(404));
        assertThat(apiError.path(), is(BASE_PATH + "/" + DICTIONARY_ID));
        verify(dictionaryService, times(1)).getDictionary(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void addWordsToDictionary_shouldReturn200() throws Exception {
        Set<String> newWords = new HashSet<>(WORDS);
        newWords.add("Joe");
        dictionaryFromService.setWords(newWords);

        AddWordsRequest addWordsRequest = new AddWordsRequest(List.of("Joe"));

        when(dictionaryService.addWordsToDictionary(any(UUID.class), any(AddWordsRequest.class))).thenReturn(dictionaryFromService);

        final MvcResult result = this.mockMvc
                .perform(patch(BASE_PATH + "/" + DICTIONARY_ID + "/add-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addWordsRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Dictionary dictionaryResult = objectMapper.readValue(result.getResponse().getContentAsString(), Dictionary.class);

        assertEquals(dictionaryFromService, dictionaryResult);
        verify(dictionaryService, times(1)).addWordsToDictionary(eq(DICTIONARY_ID), eq(addWordsRequest));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void addWordsToDictionary_shouldReturn404WhenNotFound() throws Exception {
        AddWordsRequest addWordsRequest = new AddWordsRequest(List.of("Joe"));

        when(dictionaryService.addWordsToDictionary(any(UUID.class), any(AddWordsRequest.class))).thenThrow(new DictionaryNotFoundException(DICTIONARY_ID));

        final MvcResult result = this.mockMvc
                .perform(patch(BASE_PATH + "/" + DICTIONARY_ID + "/add-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addWordsRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(404));
        assertThat(apiError.path(), is(BASE_PATH + "/" + DICTIONARY_ID + "/add-words"));
        verify(dictionaryService, times(1)).addWordsToDictionary(eq(DICTIONARY_ID), eq(addWordsRequest));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void addWordsToDictionary_shouldReturn400MissingFields() throws Exception {
        final MvcResult result = this.mockMvc
                .perform(patch(BASE_PATH + "/" + DICTIONARY_ID + "/add-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(400));
        assertThat(apiError.path(), is(BASE_PATH + "/" + DICTIONARY_ID + "/add-words"));
        verifyNoInteractions(dictionaryService);
    }

    @Test
    void removeWordsFromDictionary_shouldReturn200() throws Exception {
        Set<String> newWords = new HashSet<>(WORDS);
        newWords.remove("John");
        dictionaryFromService.setWords(newWords);

        RemoveWordsRequest removeWordsRequest = new RemoveWordsRequest(List.of("John"));

        when(dictionaryService.removeWordsFromDictionary(any(UUID.class), any(RemoveWordsRequest.class))).thenReturn(dictionaryFromService);

        final MvcResult result = this.mockMvc
                .perform(patch(BASE_PATH + "/" + DICTIONARY_ID + "/remove-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeWordsRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Dictionary dictionaryResult = objectMapper.readValue(result.getResponse().getContentAsString(), Dictionary.class);

        assertEquals(dictionaryFromService, dictionaryResult);
        verify(dictionaryService, times(1)).removeWordsFromDictionary(eq(DICTIONARY_ID), eq(removeWordsRequest));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void removeWordsFromDictionary_shouldReturn404WhenNotFound() throws Exception {
        RemoveWordsRequest removeWordsRequest = new RemoveWordsRequest(List.of("John"));

        when(dictionaryService.removeWordsFromDictionary(any(UUID.class), any(RemoveWordsRequest.class)))
                .thenThrow(new DictionaryNotFoundException(DICTIONARY_ID));

        final MvcResult result = this.mockMvc
                .perform(patch(BASE_PATH + "/" + DICTIONARY_ID + "/remove-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeWordsRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(404));
        assertThat(apiError.path(), is(BASE_PATH + "/" + DICTIONARY_ID + "/remove-words"));
        verify(dictionaryService, times(1)).removeWordsFromDictionary(eq(DICTIONARY_ID), eq(removeWordsRequest));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void removeWordsFromDictionary_shouldReturn400MissingFields() throws Exception {
        final MvcResult result = this.mockMvc
                .perform(patch(BASE_PATH + "/" + DICTIONARY_ID + "/remove-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(400));
        assertThat(apiError.path(), is(BASE_PATH + "/" + DICTIONARY_ID + "/remove-words"));
        verifyNoInteractions(dictionaryService);
    }

    @Test
    void deleteDictionary_shouldReturn204() throws Exception {
        this.mockMvc
                .perform(delete(BASE_PATH + "/" + DICTIONARY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        verify(dictionaryService, times(1)).deleteDictionary(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryService);
    }

    @Test
    void deleteDictionary_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new DictionaryNotFoundException(DICTIONARY_ID)).when(dictionaryService).deleteDictionary(eq(DICTIONARY_ID));

        final MvcResult result = this.mockMvc
                .perform(delete(BASE_PATH + "/" + DICTIONARY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        final ApiError apiError = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
        assertNotNull(UUID.fromString(apiError.id()));
        assertThat(apiError.status(), is(404));
        assertThat(apiError.path(), is(BASE_PATH + "/" + DICTIONARY_ID));
        verify(dictionaryService, times(1)).deleteDictionary(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryService);
    }

}