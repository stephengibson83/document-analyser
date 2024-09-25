package com.sarkesa.documentanalyser.dictionary.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RemoveWordsRequest(@NotNull @NotEmpty List<String> wordsToRemove) {
}
