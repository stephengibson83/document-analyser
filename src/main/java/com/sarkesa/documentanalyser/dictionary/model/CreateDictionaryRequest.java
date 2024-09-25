package com.sarkesa.documentanalyser.dictionary.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateDictionaryRequest(String name, @NotNull @NotEmpty Set<String> words) {
}
