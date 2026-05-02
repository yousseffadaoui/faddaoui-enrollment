package com.englishschool.courseservice.mapper;

import com.englishschool.courseservice.dto.CourseDTO;
import com.englishschool.courseservice.entity.Category;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Instructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CourseMapperTest {

    private final CourseMapper mapper = new CourseMapper();

    @Test
    void toDTO_null_returnsNull() {
        assertThat(mapper.toDTO(null)).isNull();
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDTO_mapsImportantFieldsAndAssociationIds() {
        Category cat = Category.builder().id(3L).name("Grammar").build();
        Instructor inst = Instructor.builder().id(4L).firstName("Jane").lastName("Doe").build();
        Course course = new Course();
        course.setCourseId(10L);
        course.setName("English A1");
        course.setLevel("A1");
        course.setDescription("Basics");
        course.setCategory(cat);
        course.setInstructor(inst);
        course.setPrice(BigDecimal.valueOf(12.50));
        course.setThumbnailUrl("/img");
        course.setIsPublished(true);
        course.setRatingAvg(BigDecimal.valueOf(4.25));
        course.setRatingCount(8);

        CourseDTO dto = mapper.toDTO(course);

        assertThat(dto.getCourseId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("English A1");
        assertThat(dto.getCategoryId()).isEqualTo(3L);
        assertThat(dto.getInstructorId()).isEqualTo(4L);
        assertThat(dto.getIsPublished()).isTrue();
        assertThat(dto.getRatingCount()).isEqualTo(8);
    }

    @Test
    void toEntity_mapsDefaults_whenNullBooleansOrCounts() {
        CourseDTO dto = CourseDTO.builder()
                .courseId(10L)
                .name("English A1")
                .level("A1")
                .description("Basics")
                .price(BigDecimal.valueOf(0))
                .thumbnailUrl("/img")
                .isPublished(null)
                .ratingAvg(BigDecimal.valueOf(0))
                .ratingCount(null)
                .build();

        Course entity = mapper.toEntity(dto);

        assertThat(entity.getCourseId()).isEqualTo(10L);
        assertThat(entity.getIsPublished()).isFalse();
        assertThat(entity.getRatingCount()).isEqualTo(0);
    }
}

