package com.englishschool.courseservice.service;

import com.englishschool.courseservice.dto.LessonDTO;
import com.englishschool.courseservice.entity.Lesson;
import com.englishschool.courseservice.entity.Module;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.mapper.LessonMapper;
import com.englishschool.courseservice.repository.LessonRepository;
import com.englishschool.courseservice.repository.ModuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository repository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonMapper mapper;

    @InjectMocks
    private LessonService service;

    @Test
    void create_setsModuleAndSaves() {
        LessonDTO in = LessonDTO.builder().moduleId(2L).title("Intro").build();
        Module module = Module.builder().id(2L).title("Mod").build();
        Lesson entity = new Lesson();
        Lesson saved = Lesson.builder().id(9L).module(module).title("Intro").build();

        when(moduleRepository.findById(2L)).thenReturn(Optional.of(module));
        when(mapper.toEntity(in)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(LessonDTO.builder().id(9L).moduleId(2L).title("Intro").build());

        LessonDTO res = service.create(in);

        assertThat(entity.getModule()).isSameAs(module);
        assertThat(res.getId()).isEqualTo(9L);
        verify(repository).save(entity);
    }

    @Test
    void create_whenModuleMissing_throwsNotFound() {
        LessonDTO in = LessonDTO.builder().moduleId(99L).title("Intro").build();
        when(moduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Module not found");
        verify(repository, never()).save(any());
    }

    @Test
    void getByModuleId_mapsList() {
        Lesson l = Lesson.builder().id(1L).title("L1").build();
        when(repository.findByModule_IdOrderByOrderIndexAsc(3L)).thenReturn(List.of(l));
        when(mapper.toDTO(l)).thenReturn(LessonDTO.builder().id(1L).moduleId(3L).title("L1").build());

        List<LessonDTO> res = service.getByModuleId(3L);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTitle()).isEqualTo("L1");
    }

    @Test
    void update_setsOnlyNonNullOptionalFields() {
        Lesson existing = Lesson.builder()
                .id(5L)
                .title("Old")
                .contentType(Lesson.ContentType.VIDEO)
                .contentUrl("old")
                .durationMinutes(10)
                .orderIndex(1)
                .build();
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(LessonDTO.builder().id(5L).title("New").build());

        LessonDTO in = LessonDTO.builder()
                .title("New")
                .contentType(null)
                .contentUrl(null)
                .durationMinutes(25)
                .orderIndex(null)
                .build();

        service.update(5L, in);

        assertThat(existing.getTitle()).isEqualTo("New");
        assertThat(existing.getContentType()).isEqualTo(Lesson.ContentType.VIDEO);
        assertThat(existing.getContentUrl()).isEqualTo("old");
        assertThat(existing.getDurationMinutes()).isEqualTo(25);
        assertThat(existing.getOrderIndex()).isEqualTo(1);
    }

    @Test
    void delete_whenMissing_throws() {
        when(repository.existsById(12L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(12L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lesson not found");
    }

    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(12L)).thenReturn(true);

        service.delete(12L);

        verify(repository).deleteById(12L);
    }
}

