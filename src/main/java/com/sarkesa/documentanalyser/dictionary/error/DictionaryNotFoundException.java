package com.sarkesa.documentanalyser.dictionary.error;

import lombok.Getter;

import java.util.UUID;

@Getter
public class DictionaryNotFoundException extends RuntimeException {
    private UUID id;

    public DictionaryNotFoundException(final UUID id) {
        this.id = id;
    }
}
