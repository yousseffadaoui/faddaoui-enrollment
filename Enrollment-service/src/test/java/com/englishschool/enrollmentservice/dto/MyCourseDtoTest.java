package com.englishschool.enrollmentservice.dto;

import com.englishschool.enrollmentservice.client.CourseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyCourseDtoTest {

    @Test
    void builder_andDataMethods() {
        CourseDTO course = new CourseDTO();
        course.setCourseId(10L);
        course.setName("English A1");

        MyCourseDTO dto = MyCourseDTO.builder()
                .enrollmentId(1L)
                .courseId(10L)
                .status("active")
                .progressPercent(50)
                .course(course)
                .build();

        assertThat(dto.getEnrollmentId()).isEqualTo(1L);
        assertThat(dto.getCourse().getName()).isEqualTo("English A1");
        assertThat(dto.toString()).contains("active");
    }
}

