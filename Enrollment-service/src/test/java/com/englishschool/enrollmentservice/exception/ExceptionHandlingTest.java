package com.englishschool.enrollmentservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExceptionHandlingTest {

    @Test
    void resourceNotFoundException_messageFormat() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Enrollment", 5L);
        assertThat(ex.getMessage()).isEqualTo("Enrollment not found with id: 5");
    }

    @Test
    void duplicateEnrollmentException_isRuntimeException() {
        DuplicateEnrollmentException ex = new DuplicateEnrollmentException("dup");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("dup");
    }

    @Test
    void globalExceptionHandler_notFound_returns404() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/x");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> res =
                handler.handleNotFound(new ResourceNotFoundException("x"), req);

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getStatus()).isEqualTo(404);
        assertThat(res.getBody().getPath()).isEqualTo("/x");
    }

    @Test
    void globalExceptionHandler_validation_returns400() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/x");

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> res = handler.handleValidation(ex, req);

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getStatus()).isEqualTo(400);
        assertThat(res.getBody().getError()).isEqualTo("Bad Request");
    }

    @Test
    void globalExceptionHandler_generic_returns500() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/x");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> res =
                handler.handleGeneric(new RuntimeException("boom"), req);

        assertThat(res.getStatusCode().value()).isEqualTo(500);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getStatus()).isEqualTo(500);
        assertThat(res.getBody().getMessage()).isEqualTo("boom");
    }
}

