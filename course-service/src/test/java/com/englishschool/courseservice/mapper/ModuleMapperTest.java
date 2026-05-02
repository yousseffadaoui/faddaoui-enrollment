package com.englishschool.courseservice.mapper;

import com.englishschool.courseservice.dto.ModuleDTO;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Lesson;
import com.englishschool.courseservice.entity.Module;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleMapperTest {

    private final LessonMapper lessonMapper = new LessonMapper();
    private final ModuleMapper mapper = new ModuleMapper(lessonMapper);

    @Test
    void toDTO_null_returnsNull() {
        assertThat(mapper.toDTO(null, false)).isNull();
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDTO_whenIncludeLessonsFalse_lessonsNull() {
        Course c = new Course();
        c.setCourseId(7L);
        Module e = Module.builder().id(1L).course(c).title("M1").orderIndex(2).lessons(List.of()).build();

        ModuleDTO dto = mapper.toDTO(e, false);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCourseId()).isEqualTo(7L);
        assertThat(dto.getLessons()).isNull();
    }

    @Test
    void toDTO_whenIncludeLessonsTrue_mapsLessons() {
        Course c = new Course();
        c.setCourseId(7L);
        Lesson l = Lesson.builder().id(10L).title("L1").orderIndex(0).durationMinutes(5).build();
        Module e = Module.builder().id(1L).course(c).title("M1").orderIndex(2).lessons(List.of(l)).build();

        ModuleDTO dto = mapper.toDTO(e, true);

        assertThat(dto.getLessons()).hasSize(1);
        assertThat(dto.getLessons().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getLessons().get(0).getTitle()).isEqualTo("L1");
    }

    @Test
    void toEntity_setsDefaultOrderIndex() {
        ModuleDTO dto = ModuleDTO.builder().id(3L).courseId(1L).title("M").orderIndex(null).build();

        Module e = mapper.toEntity(dto);

        assertThat(e.getId()).isEqualTo(3L);
        assertThat(e.getTitle()).isEqualTo("M");
        assertThat(e.getOrderIndex()).isEqualTo(0);
    }
}

