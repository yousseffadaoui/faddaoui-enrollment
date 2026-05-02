package com.englishschool.enrollmentservice.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void enrollmentDto_missingCourseId_hasViolation() {
        EnrollmentDTO dto = EnrollmentDTO.builder()
                .studentName("Alice")
                .build();

        var violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("courseId"));
    }

    @Test
    void enrollmentDto_withCourseId_noViolations() {
        EnrollmentDTO dto = EnrollmentDTO.builder()
                .courseId(10L)
                .studentName("Alice")
                .build();

        assertThat(validator.validate(dto)).isEmpty();
    }
}

