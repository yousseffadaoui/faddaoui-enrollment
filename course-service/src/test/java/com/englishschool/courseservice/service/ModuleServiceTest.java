package com.englishschool.courseservice.service;

import com.englishschool.courseservice.dto.ModuleDTO;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Module;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.mapper.ModuleMapper;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.repository.ModuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository repository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ModuleMapper mapper;

    @InjectMocks
    private ModuleService service;

    @Captor
    private ArgumentCaptor<Module> moduleCaptor;

    @Test
    void create_setsCourseAndSaves() {
        ModuleDTO in = ModuleDTO.builder().courseId(7L).title("Module 1").orderIndex(0).build();
        Course course = new Course();
        course.setCourseId(7L);
        Module entity = new Module();
        Module saved = new Module();
        saved.setId(10L);
        saved.setCourse(course);

        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));
        when(mapper.toEntity(in)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(ModuleDTO.builder().id(10L).courseId(7L).title("Module 1").build());

        ModuleDTO res = service.create(in);

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(entity.getCourse()).isSameAs(course);
        verify(repository).save(entity);
    }

    @Test
    void create_whenCourseMissing_throwsNotFound() {
        ModuleDTO in = ModuleDTO.builder().courseId(99L).title("X").build();
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");

        verify(repository, never()).save(any());
    }

    @Test
    void getByCourseId_mapsWithIncludeLessonsFlag() {
        Module m1 = Module.builder().id(1L).title("M1").build();
        when(repository.findByCourseCourseIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(m1));
        when(mapper.toDTO(m1, true)).thenReturn(ModuleDTO.builder().id(1L).title("M1").build());

        List<ModuleDTO> res = service.getByCourseId(5L, true);

        assertThat(res).hasSize(1);
        verify(mapper).toDTO(m1, true);
    }

    @Test
    void update_whenOrderIndexNull_keepsExisting() {
        Module existing = Module.builder().id(3L).title("Old").orderIndex(5).build();
        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(ModuleDTO.builder().id(3L).orderIndex(5).build());

        ModuleDTO in = ModuleDTO.builder().title("New").orderIndex(null).build();

        ModuleDTO res = service.update(3L, in);

        assertThat(existing.getTitle()).isEqualTo("New");
        assertThat(existing.getOrderIndex()).isEqualTo(5);
        assertThat(res.getOrderIndex()).isEqualTo(5);
    }

    @Test
    void delete_whenMissing_throws() {
        when(repository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Module not found");
    }

    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(77L)).thenReturn(true);

        service.delete(77L);

        verify(repository).deleteById(77L);
    }
}

