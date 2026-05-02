package com.englishschool.courseservice.mapper;

import com.englishschool.courseservice.dto.ReviewDTO;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Review;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewMapperTest {

    private final ReviewMapper mapper = new ReviewMapper();

    @Test
    void toDTO_null_returnsNull() {
        assertThat(mapper.toDTO(null)).isNull();
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDTO_mapsImportantFieldsIncludingCourseId() {
        Course c = new Course();
        c.setCourseId(7L);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        Review e = Review.builder()
                .id(10L)
                .course(c)
                .userId(3L)
                .rating(5)
                .comment("Great")
                .createdAt(now)
                .build();

        ReviewDTO dto = mapper.toDTO(e);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getCourseId()).isEqualTo(7L);
        assertThat(dto.getUserId()).isEqualTo(3L);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_mapsImportantFields() {
        ReviewDTO dto = ReviewDTO.builder()
                .id(10L)
                .courseId(7L)
                .userId(3L)
                .rating(4)
                .comment("Ok")
                .build();

        Review e = mapper.toEntity(dto);

        assertThat(e.getId()).isEqualTo(10L);
        assertThat(e.getUserId()).isEqualTo(3L);
        assertThat(e.getRating()).isEqualTo(4);
        assertThat(e.getComment()).isEqualTo("Ok");
        assertThat(e.getCourse()).isNull();
    }
}

