package com.sarkesa.documentanalyser.dictionary.model;

import com.sarkesa.documentanalyser.dictionary.repository.StringSetConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dictionary {

    @Id
    @UuidGenerator
    private UUID id;
    private String name;

    // This could/should have a separate table because we are limited by the max size of the column
    // but for simplicity we will have a simple converter for the MVP
    @Convert(converter = StringSetConverter.class)
    @Column(name = "words", nullable = false)
    private Set<String> words = new HashSet<>();
}
