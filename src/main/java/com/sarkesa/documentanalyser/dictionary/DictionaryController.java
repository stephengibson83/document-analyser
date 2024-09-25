package com.sarkesa.documentanalyser.dictionary;


import com.sarkesa.documentanalyser.dictionary.model.AddWordsRequest;
import com.sarkesa.documentanalyser.dictionary.model.CreateDictionaryRequest;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.dictionary.model.DictionaryResponse;
import com.sarkesa.documentanalyser.dictionary.model.RemoveWordsRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping(value = "/v1/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(final DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DictionaryResponse createDictionary(@Valid @RequestBody final CreateDictionaryRequest request) {
        log.trace("Creating new dictionary: {}", request);
        final Dictionary dictionary = dictionaryService.createDictionary(request);
        log.trace("New dictionary: {}", dictionary);
        return convertToDictionaryResponse(dictionary);
    }

    @GetMapping
    public List<Dictionary> getAllDictionaries() {
        log.trace("Getting all dictionaries");
        final List<Dictionary> dictionaries = dictionaryService.getAllDictionaries();
        log.trace("All dictionaries: {}", dictionaries);
        return dictionaries;
    }

    @GetMapping("/{id}")
    public Dictionary getDictionary(@PathVariable final UUID id) {
        log.trace("Getting dictionary with id: {}", id);
        final Dictionary dictionary = dictionaryService.getDictionary(id);
        log.trace("Dictionary retrieved: {}", dictionary);
        return dictionary;
    }

    @PatchMapping("/{id}/add-words")
    public Dictionary addWordsToDictionary(@PathVariable final UUID id, @RequestBody @Valid final AddWordsRequest addWordsRequest) {
        log.trace("Adding words [{}] to the dictionary with id: {}", addWordsRequest.wordsToAdd(), id);
        final Dictionary dictionary = dictionaryService.addWordsToDictionary(id, addWordsRequest);
        log.trace("Dictionary updated: {}", dictionary);
        return dictionary;
    }

    @PatchMapping("/{id}/remove-words")
    public Dictionary removeWordsFromDictionary(@PathVariable final UUID id, @RequestBody @Valid final RemoveWordsRequest removeWordsRequest) {
        log.trace("Removing words [{}] from the dictionary with id: {}", removeWordsRequest.wordsToRemove(), id);
        final Dictionary dictionary = dictionaryService.removeWordsFromDictionary(id, removeWordsRequest);
        log.trace("Dictionary updated: {}", dictionary);
        return dictionary;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDictionary(@PathVariable final UUID id) {
        log.trace("Deleting dictionary with id: {}", id);
        dictionaryService.deleteDictionary(id);
    }

    private DictionaryResponse convertToDictionaryResponse(final Dictionary dictionary) {
        return DictionaryResponse.builder()
                .id(dictionary.getId())
                .name(dictionary.getName())
                .words(dictionary.getWords())
                .build();
    }
}
