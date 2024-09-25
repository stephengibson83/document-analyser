package com.sarkesa.documentanalyser.dictionary.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddWordsRequest(@NotNull @NotEmpty List<String> wordsToAdd) {
}
