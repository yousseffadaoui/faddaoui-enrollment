package com.englishschool.enrollmentservice.dto;

import com.englishschool.enrollmentservice.client.CourseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DtoDataMethodsTest {

    @Test
    void enrollmentDto_equalsHashCode_toString() {
        EnrollmentDTO a = EnrollmentDTO.builder()
                .id(1L)
                .userId(2L)
                .studentName("Alice")
                .courseId(10L)
                .status("active")
                .progressPercent(0)
                .build();

        EnrollmentDTO b = EnrollmentDTO.builder()
                .id(1L)
                .userId(2L)
                .studentName("Alice")
                .courseId(10L)
                .status("active")
                .progressPercent(0)
                .build();

        EnrollmentDTO c = EnrollmentDTO.builder()
                .id(2L)
                .userId(2L)
                .studentName("Alice")
                .courseId(10L)
                .status("active")
                .progressPercent(0)
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("x");
        assertThat(a.toString()).contains("Alice");
    }

    @Test
    void progressDto_equalsHashCode_toString() {
        ProgressDTO a = ProgressDTO.builder().id(1L).enrollmentId(2L).lessonId(3L).build();
        ProgressDTO b = ProgressDTO.builder().id(1L).enrollmentId(2L).lessonId(3L).build();
        ProgressDTO c = ProgressDTO.builder().id(1L).enrollmentId(2L).lessonId(4L).build();

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("lessonId=3");
    }

    @Test
    void myCourseDto_equalsHashCode_toString() {
        CourseDTO course = new CourseDTO();
        course.setCourseId(10L);
        course.setName("English A1");

        MyCourseDTO a = MyCourseDTO.builder()
                .enrollmentId(1L)
                .courseId(10L)
                .status("active")
                .progressPercent(10)
                .course(course)
                .build();

        MyCourseDTO b = MyCourseDTO.builder()
                .enrollmentId(1L)
                .courseId(10L)
                .status("active")
                .progressPercent(10)
                .course(course)
                .build();

        MyCourseDTO c = MyCourseDTO.builder()
                .enrollmentId(2L)
                .courseId(10L)
                .status("active")
                .progressPercent(10)
                .course(course)
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("progressPercent=10");
    }
}

