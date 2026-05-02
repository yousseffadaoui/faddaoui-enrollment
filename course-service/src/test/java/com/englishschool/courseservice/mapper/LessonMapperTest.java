package com.englishschool.courseservice.mapper;

import com.englishschool.courseservice.dto.LessonDTO;
import com.englishschool.courseservice.entity.Lesson;
import com.englishschool.courseservice.entity.Module;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LessonMapperTest {

    private final LessonMapper mapper = new LessonMapper();

    @Test
    void toDTO_null_returnsNull() {
        assertThat(mapper.toDTO(null)).isNull();
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDTO_mapsImportantFieldsIncludingModuleId() {
        Module m = Module.builder().id(2L).title("M").build();
        Lesson e = Lesson.builder()
                .id(10L)
                .module(m)
                .title("Intro")
                .contentType(Lesson.ContentType.VIDEO)
                .contentUrl("http://video")
                .contentText(null)
                .quizContentJson(null)
                .durationMinutes(15)
                .orderIndex(1)
                .build();

        LessonDTO dto = mapper.toDTO(e);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getModuleId()).isEqualTo(2L);
        assertThat(dto.getContentType()).isEqualTo(Lesson.ContentType.VIDEO);
        assertThat(dto.getDurationMinutes()).isEqualTo(15);
    }

    @Test
    void toEntity_setsDefaultsForNullNumbers() {
        LessonDTO dto = LessonDTO.builder()
                .id(10L)
                .moduleId(2L)
                .title("Intro")
                .contentType(Lesson.ContentType.TEXT)
                .durationMinutes(null)
                .orderIndex(null)
                .build();

        Lesson e = mapper.toEntity(dto);

        assertThat(e.getId()).isEqualTo(10L);
        assertThat(e.getTitle()).isEqualTo("Intro");
        assertThat(e.getDurationMinutes()).isEqualTo(0);
        assertThat(e.getOrderIndex()).isEqualTo(0);
    }
}

