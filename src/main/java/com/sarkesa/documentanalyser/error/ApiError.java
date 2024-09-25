package com.sarkesa.documentanalyser.error;

import lombok.Builder;

@Builder
public record ApiError(String id, String timestamp, String message, Integer status, String error, String path) {
}
