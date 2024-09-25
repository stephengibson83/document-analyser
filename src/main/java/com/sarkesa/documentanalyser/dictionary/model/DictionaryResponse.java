package com.sarkesa.documentanalyser.dictionary.model;

import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record DictionaryResponse(UUID id, String name, Set<String> words) {
}
