package com.sarkesa.documentanalyser.job.search.model;

import lombok.Builder;

@Builder
public record SearchResult(int startIndex, int endIndex) {
}
