package com.sarkesa.documentanalyser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class DocumentAnalyserConfig {

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(4); // TODO BEYOND MVP - read thread count from properties
    }

}
