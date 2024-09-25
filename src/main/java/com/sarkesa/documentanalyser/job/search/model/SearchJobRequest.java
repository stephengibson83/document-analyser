package com.sarkesa.documentanalyser.job.search.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SearchJobRequest(
        @NotNull UUID dictionaryId,
        @NotNull String inputText,
        Boolean caseSensitive) {
}
