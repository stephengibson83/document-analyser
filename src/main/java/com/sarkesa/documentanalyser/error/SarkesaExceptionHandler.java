package com.sarkesa.documentanalyser.error;

import com.sarkesa.documentanalyser.dictionary.error.DictionaryNotFoundException;
import com.sarkesa.documentanalyser.job.error.JobNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class SarkesaExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    @ResponseStatus(BAD_REQUEST)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatusCode status,
                                                                  final WebRequest request) {

        final StringBuilder sb = new StringBuilder("The following fields in the request were invalid. ");

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                sb.append(String.format("Field [%s], error message [%s]. ", fieldError.getField(), fieldError.getDefaultMessage())));

        final ApiError apiError = constructApiError(BAD_REQUEST, sb.toString(), ((ServletWebRequest) request).getRequest());
        final ResponseEntity<Object> httpResponse = new ResponseEntity<>(apiError, BAD_REQUEST);

        logError(ex, apiError);
        return httpResponse;
    }

    @Override
    @ResponseStatus(BAD_REQUEST)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatusCode status,
                                                                  final WebRequest request) {
        final ApiError apiError = constructApiError(BAD_REQUEST, "Input message could not be read.", ((ServletWebRequest) request).getRequest());
        final ResponseEntity<Object> httpResponse = new ResponseEntity<>(apiError, BAD_REQUEST);

        logError(ex, apiError);
        return httpResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpectedException(final Exception ex, final HttpServletRequest httpServletRequest) {
        log.warn("Unexpected exception caught. Type was [{}], message was [{}]", ex.getClass().getName(), ex.getLocalizedMessage());

        final ApiError apiError = constructApiError(INTERNAL_SERVER_ERROR, "Unexpected internal error.", httpServletRequest);
        logError(ex, apiError);
        return apiError;
    }

    @ExceptionHandler(JobNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ApiError handleJobNotFoundException(final JobNotFoundException ex, final HttpServletRequest httpServletRequest) {
        final ApiError apiError = constructApiError(NOT_FOUND, "Job " + ex.getId() + " does not exist", httpServletRequest);
        logError(ex, apiError);
        return apiError;
    }

    @ExceptionHandler(DictionaryNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ApiError handleDictionaryNotFoundException(final DictionaryNotFoundException ex, final HttpServletRequest httpServletRequest) {
        final ApiError apiError = constructApiError(NOT_FOUND, "Dictionary " + ex.getId() + " does not exist", httpServletRequest);
        logError(ex, apiError);
        return apiError;
    }

    protected ApiError constructApiError(final HttpStatus httpStatus,
                                         final String message,
                                         final HttpServletRequest httpServletRequest) {
        String requestId = MDC.get("requestId");
        if (isNull(requestId)) {
            requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);
        }

        return ApiError.builder()
            .id(requestId)
            .timestamp(Instant.now().toString())
            .status(httpStatus.value())
            .error(httpStatus.getReasonPhrase())
            .message(message)
            .path(httpServletRequest.getRequestURI())
            .build();
    }

    protected void logError(final Exception exception,
                            final ApiError apiError) {
        log.error("Error ID [{}] generated for exception [{}] caught when executing path [{}]. Exception message: [{}]",
            apiError.id(), exception.getClass().getName(), apiError.path(), exception.toString());
        log.error("Caught exception details:", exception);
        MDC.clear();
    }
}
