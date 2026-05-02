package com.englishschool.enrollmentservice.client;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientDtoTest {

    @Test
    void courseDto_gettersSetters_toString() {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(1L);
        dto.setName("English A1");
        dto.setLevel("A1");
        dto.setDescription("Desc");
        dto.setCategoryId(2L);
        dto.setInstructorId(3L);
        dto.setPrice(new BigDecimal("19.99"));
        dto.setThumbnailUrl("http://img");
        dto.setIsPublished(true);
        dto.setRatingAvg(new BigDecimal("4.5"));
        dto.setRatingCount(10);

        assertThat(dto.getCourseId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("English A1");
        assertThat(dto.toString()).contains("English A1");
    }

    @Test
    void courseDto_equalsHashCode() {
        CourseDTO a = new CourseDTO();
        a.setCourseId(1L);
        a.setName("A");
        CourseDTO b = new CourseDTO();
        b.setCourseId(1L);
        b.setName("A");
        CourseDTO c = new CourseDTO();
        c.setCourseId(2L);
        c.setName("A");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("x");
    }

    @Test
    void lessonDto_gettersSetters_equalsAndHashCode() {
        LessonDTO a = new LessonDTO();
        a.setId(1L);
        a.setModuleId(2L);
        a.setTitle("Lesson 1");

        LessonDTO b = new LessonDTO();
        b.setId(1L);
        b.setModuleId(2L);
        b.setTitle("Lesson 1");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void moduleDto_withLessons() {
        LessonDTO l1 = new LessonDTO();
        l1.setId(1L);
        l1.setTitle("L1");

        ModuleDTO module = new ModuleDTO();
        module.setId(10L);
        module.setCourseId(5L);
        module.setTitle("Module");
        module.setLessons(List.of(l1));

        assertThat(module.getLessons()).hasSize(1);
        assertThat(module.toString()).contains("Module");
    }
}

