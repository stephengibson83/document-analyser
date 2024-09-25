package com.sarkesa.documentanalyser.job.search.model;

import com.sarkesa.documentanalyser.job.Job;
import com.sarkesa.documentanalyser.job.JobStatus;
import com.sarkesa.documentanalyser.job.search.repository.MapOfSearchResultsConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchJob implements Job {
    @Id
    @UuidGenerator
    private UUID id;

    private JobStatus status;
    private UUID dictionaryId; // TODO Beyond MVP - this would have a defined relationship to the Dictionary table
    private boolean caseSensitive;

    @Column(length = 10000) // TODO Beyond MVP - the input should be streamed from a file rather than saved to DB
    private String inputText;

    @Convert(converter = MapOfSearchResultsConverter.class)
    @Column(name = "results", nullable = false)
    private Map<String, List<SearchResult>> results;

    // TODO Beyond MVP - add timestamps for created/updated
}
