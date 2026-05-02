package com.englishschool.courseservice.exception;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_buildsBadRequestResponse() {
        WebRequest request = Mockito.mock(WebRequest.class);
        Mockito.when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp =
                handler.handleIllegalArgument(new IllegalArgumentException("boom"), request);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getError()).isEqualTo("Bad Request");
        assertThat(resp.getBody().getMessage()).isEqualTo("boom");
        assertThat(resp.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    void handleResourceNotFound_buildsNotFoundResponse() {
        WebRequest request = Mockito.mock(WebRequest.class);
        Mockito.when(request.getDescription(false)).thenReturn("uri=/api/v1/courses/99");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp =
                handler.handleResourceNotFound(new ResourceNotFoundException("Course", 99L), request);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getError()).isEqualTo("Not Found");
        assertThat(resp.getBody().getPath()).isEqualTo("/api/v1/courses/99");
    }

    @Test
    void handleGeneric_buildsInternalServerError_andFallbackMessage() {
        WebRequest request = Mockito.mock(WebRequest.class);
        Mockito.when(request.getDescription(false)).thenReturn("uri=/api/v1/x");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp =
                handler.handleGeneric(new Exception((String) null), request);

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(resp.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleValidation_groupsFieldErrors() {
        WebRequest request = Mockito.mock(WebRequest.class);
        Mockito.when(request.getDescription(false)).thenReturn("uri=/api/v1/courses");

        var target = new Object();
        var binding = new BeanPropertyBindingResult(target, "target");
        binding.addError(new FieldError("target", "name", "required"));
        binding.addError(new FieldError("target", "name", "size"));
        binding.addError(new FieldError("target", "level", "invalid"));

        var methodParam = new org.springframework.core.MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethods()[0], -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParam, binding);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleValidation(ex, request);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(resp.getBody().getValidationErrors()).containsKeys("name", "level");
        assertThat(resp.getBody().getValidationErrors().get("name")).containsExactlyInAnyOrder("required", "size");
        assertThat(resp.getBody().getValidationErrors().get("level")).isEqualTo(List.of("invalid"));
    }
}

