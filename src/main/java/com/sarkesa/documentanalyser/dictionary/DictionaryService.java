package com.sarkesa.documentanalyser.dictionary;

import com.sarkesa.documentanalyser.dictionary.error.DictionaryNotFoundException;
import com.sarkesa.documentanalyser.dictionary.model.AddWordsRequest;
import com.sarkesa.documentanalyser.dictionary.model.CreateDictionaryRequest;
import com.sarkesa.documentanalyser.dictionary.model.Dictionary;
import com.sarkesa.documentanalyser.dictionary.model.RemoveWordsRequest;
import com.sarkesa.documentanalyser.dictionary.repository.DictionaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    public DictionaryService(final DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public Dictionary createDictionary(final CreateDictionaryRequest request) {
        log.trace("Creating new dictionary: {}", request);
        final Dictionary dictionary = Dictionary.builder()
                .words(request.words())
                .name(request.name())
                .build();
        final Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        log.trace("New dictionary: {}", savedDictionary);
        return savedDictionary;
    }

    public List<Dictionary> getAllDictionaries() {
        log.trace("Getting all dictionaries");
        final List<Dictionary> dictionaries = dictionaryRepository.findAll();
        log.trace("All dictionaries: {}", dictionaries);
        return dictionaries;
    }

    public Dictionary getDictionary(final UUID id) {
        log.trace("Getting dictionary with id: {}", id);
        final Dictionary dictionary = dictionaryRepository.findById(id).orElseThrow(() -> new DictionaryNotFoundException(id));
        log.trace("Dictionary retrieved: {}", dictionary);
        return dictionary;
    }

    public Dictionary addWordsToDictionary(final UUID id, final AddWordsRequest addWordsRequest) {
        log.trace("Adding words [{}] to the dictionary with id: {}", addWordsRequest.wordsToAdd(), id);
        final Dictionary dictionary = getDictionary(id);

        final Set<String> combinedWords = new HashSet<>();
        combinedWords.addAll(dictionary.getWords());
        combinedWords.addAll(addWordsRequest.wordsToAdd());
        dictionary.setWords(combinedWords);

        final Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        log.trace("Dictionary updated: {}", savedDictionary);
        return savedDictionary;
    }

    public Dictionary removeWordsFromDictionary(final UUID id, final RemoveWordsRequest removeWordsRequest) {
        log.trace("Removing words [{}] from the dictionary with id: {}", removeWordsRequest.wordsToRemove(), id);
        final Dictionary dictionary = getDictionary(id);

        final Set<String> combinedWords = new HashSet<>(dictionary.getWords());
        removeWordsRequest.wordsToRemove().forEach(combinedWords::remove);
        dictionary.setWords(combinedWords);

        final Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        log.trace("Dictionary updated: {}", savedDictionary);
        return savedDictionary;
    }

    public void deleteDictionary(final UUID id) {
        log.trace("Deleting dictionary with id: {}", id);

        // Checking the Dictionary exists
        getDictionary(id);

        // TODO BEYOND MVP - check if the dictionary is being used in any in progress job. If it is, return an error.

        dictionaryRepository.deleteById(id);
    }
}
