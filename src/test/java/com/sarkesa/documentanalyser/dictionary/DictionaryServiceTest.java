package com.sarkesa.documentanalyser.dictionary;

import com.sarkesa.documentanalyser.dictionary.error.DictionaryNotFoundException;
import com.sarkesa.documentanalyser.dictionary.model.AddWordsRequest;
import com.sarkesa.documentanalyser.dictionary.model.CreateDictionaryRequest;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.dictionary.model.RemoveWordsRequest;
import com.sarkesa.documentanalyser.dictionary.repository.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DictionaryServiceTest {

    @Mock
    private DictionaryRepository dictionaryRepository;

    private DictionaryService dictionaryService;

    private static final UUID DICTIONARY_ID = UUID.randomUUID();
    private static final Set<String> WORDS = Set.of("John", "Mary", "Paul");
    private Dictionary dictionaryFromDb;

    @BeforeEach
    void setUp() {
        openMocks(this);
        dictionaryService = new DictionaryService(dictionaryRepository);

        dictionaryFromDb = Dictionary.builder()
                .id(DICTIONARY_ID)
                .name("Test dictionary")
                .words(WORDS)
                .build();
    }

    @Test
    void createDictionary_shouldProcessSuccessfully() {
        CreateDictionaryRequest request = new CreateDictionaryRequest("Test dictionary", WORDS);

        when(dictionaryRepository.save(any(Dictionary.class))).thenReturn(dictionaryFromDb);

        Dictionary result = dictionaryService.createDictionary(request);

        assertNotNull(result);
        assertEquals(dictionaryFromDb.getName(), result.getName());
        assertEquals(dictionaryFromDb.getWords(), result.getWords());
        assertEquals(DICTIONARY_ID, result.getId());
        verify(dictionaryRepository, times(1)).save(any());
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void getAllDictionaries_shouldReturnListFromDb() {
        when(dictionaryRepository.findAll()).thenReturn(List.of(dictionaryFromDb));

        List<Dictionary> result = dictionaryService.getAllDictionaries();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dictionaryFromDb.getName(), result.get(0).getName());
        assertEquals(dictionaryFromDb.getWords(), result.get(0).getWords());
        assertEquals(DICTIONARY_ID, result.get(0).getId());
        verify(dictionaryRepository, times(1)).findAll();
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void getAllDictionaries_shouldReturnEmptyListIfNothingFoundInDb() {
        when(dictionaryRepository.findAll()).thenReturn(Collections.emptyList());

        List<Dictionary> result = dictionaryService.getAllDictionaries();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(dictionaryRepository, times(1)).findAll();
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void getDictionary_shouldReturnSingleDictionary() {
        when(dictionaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(dictionaryFromDb));

        Dictionary result = dictionaryService.getDictionary(DICTIONARY_ID);

        assertNotNull(result);
        assertEquals(dictionaryFromDb.getName(), result.getName());
        assertEquals(dictionaryFromDb.getWords(), result.getWords());
        assertEquals(DICTIONARY_ID, result.getId());
        verify(dictionaryRepository, times(1)).findById(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void getDictionary_shouldThrowExceptionIfNotFoundInDb() {
        when(dictionaryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(DictionaryNotFoundException.class, () -> dictionaryService.getDictionary(DICTIONARY_ID));

        verify(dictionaryRepository, times(1)).findById(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void addWordsToDictionary_shouldAddWordsSuccessfully() {
        when(dictionaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(dictionaryFromDb));
        when(dictionaryRepository.save(any(Dictionary.class))).thenReturn(dictionaryFromDb);

        AddWordsRequest addWordsRequest = new AddWordsRequest(List.of("Eric", "Margot"));

        Dictionary result = dictionaryService.addWordsToDictionary(DICTIONARY_ID, addWordsRequest);

        assertNotNull(result);
        assertEquals(dictionaryFromDb.getName(), result.getName());
        assertEquals(5, result.getWords().size());
        assertTrue(result.getWords().contains("Eric"));
        assertTrue(result.getWords().contains("Margot"));
        verify(dictionaryRepository, times(1)).findById(eq(DICTIONARY_ID));
        verify(dictionaryRepository, times(1)).save(any());
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void removeWordsFromDictionary_shouldRemoveWordsSuccessfully() {
        when(dictionaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(dictionaryFromDb));
        when(dictionaryRepository.save(any(Dictionary.class))).thenReturn(dictionaryFromDb);

        RemoveWordsRequest removeWordsRequest = new RemoveWordsRequest(List.of("Eric", "John"));

        Dictionary result = dictionaryService.removeWordsFromDictionary(DICTIONARY_ID, removeWordsRequest);

        assertNotNull(result);
        assertEquals(dictionaryFromDb.getName(), result.getName());
        assertEquals(2, result.getWords().size());
        assertTrue(result.getWords().contains("Mary"));
        assertTrue(result.getWords().contains("Paul"));
        verify(dictionaryRepository, times(1)).findById(eq(DICTIONARY_ID));
        verify(dictionaryRepository, times(1)).save(any());
        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void deleteDictionary_shouldProcessSuccessfully() {
        when(dictionaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(dictionaryFromDb));

        dictionaryService.deleteDictionary(DICTIONARY_ID);

        verify(dictionaryRepository, times(1)).findById(eq(DICTIONARY_ID));
        verify(dictionaryRepository, times(1)).deleteById(eq(DICTIONARY_ID));
        verifyNoMoreInteractions(dictionaryRepository);
    }
}