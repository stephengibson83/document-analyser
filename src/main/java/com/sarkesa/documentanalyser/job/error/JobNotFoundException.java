package com.sarkesa.documentanalyser.job.error;

import lombok.Getter;

import java.util.UUID;

@Getter
public class JobNotFoundException extends RuntimeException {
    private UUID id;

    public JobNotFoundException(final UUID id) {
        this.id = id;
    }
}
