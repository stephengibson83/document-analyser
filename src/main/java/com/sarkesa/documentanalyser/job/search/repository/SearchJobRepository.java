package com.sarkesa.documentanalyser.job.search.repository;

import com.sarkesa.documentanalyser.job.search.model.SearchJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SearchJobRepository extends JpaRepository<SearchJob, UUID> {
}
