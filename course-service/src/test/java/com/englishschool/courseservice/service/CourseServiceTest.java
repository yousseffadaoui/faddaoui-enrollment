package com.englishschool.courseservice.service;

import com.englishschool.courseservice.dto.CourseDTO;
import com.englishschool.courseservice.dto.CourseSearchRequest;
import com.englishschool.courseservice.dto.PageResponse;
import com.englishschool.courseservice.entity.Category;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Instructor;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.mapper.CourseMapper;
import com.englishschool.courseservice.repository.CategoryRepository;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.repository.InstructorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private CourseMapper mapper;

    @InjectMocks
    private CourseService service;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void create_withoutCategoryAndInstructor_savesEntity() {
        CourseDTO in = CourseDTO.builder()
                .name("English A1 - Basics")
                .level("A1")
                .description("Basics of English")
                .price(BigDecimal.valueOf(49.99))
                .build();
        Course entity = new Course();
        Course saved = new Course();
        saved.setCourseId(10L);
        CourseDTO out = CourseDTO.builder().courseId(10L).name("English A1 - Basics").level("A1").build();

        when(mapper.toEntity(in)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(out);

        CourseDTO res = service.create(in);

        assertThat(res.getCourseId()).isEqualTo(10L);
        verify(categoryRepository, never()).findById(any());
        verify(instructorRepository, never()).findById(any());
        verify(repository).save(entity);
    }

    @Test
    void create_withMissingCategory_throwsNotFound() {
        CourseDTO in = CourseDTO.builder()
                .name("English A2")
                .level("A2")
                .categoryId(99L)
                .build();
        when(mapper.toEntity(in)).thenReturn(new Course());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(repository, never()).save(any());
    }

    @Test
    void create_withMissingInstructor_throwsNotFound() {
        CourseDTO in = CourseDTO.builder()
                .name("English B1")
                .level("B1")
                .instructorId(55L)
                .build();
        when(mapper.toEntity(in)).thenReturn(new Course());
        when(instructorRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor not found");

        verify(repository, never()).save(any());
    }

    @Test
    void getById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void update_setsNullAssociationsWhenIdsNull_andUpdatesFields() {
        Course existing = new Course();
        existing.setCourseId(7L);
        existing.setCategory(Category.builder().id(1L).build());
        existing.setInstructor(Instructor.builder().id(2L).build());
        existing.setIsPublished(false);

        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(CourseDTO.builder().courseId(7L).build());

        CourseDTO update = CourseDTO.builder()
                .name("Updated")
                .level("B2")
                .description("Desc")
                .price(BigDecimal.valueOf(10))
                .thumbnailUrl("http://img")
                .isPublished(null)
                .categoryId(null)
                .instructorId(null)
                .build();

        service.update(7L, update);

        assertThat(existing.getName()).isEqualTo("Updated");
        assertThat(existing.getLevel()).isEqualTo("B2");
        assertThat(existing.getCategory()).isNull();
        assertThat(existing.getInstructor()).isNull();
        assertThat(existing.getIsPublished()).isFalse();
        verify(categoryRepository, never()).findById(any());
        verify(instructorRepository, never()).findById(any());
    }

    @Test
    void update_withCategoryAndInstructor_fetchesAndSets() {
        Course existing = new Course();
        existing.setCourseId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(CourseDTO.builder().courseId(7L).build());

        Category category = Category.builder().id(3L).name("Grammar").build();
        Instructor instructor = Instructor.builder().id(4L).firstName("John").lastName("Doe").build();
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(instructorRepository.findById(4L)).thenReturn(Optional.of(instructor));

        CourseDTO update = CourseDTO.builder()
                .name("Updated")
                .level("C1")
                .isPublished(true)
                .categoryId(3L)
                .instructorId(4L)
                .build();

        service.update(7L, update);

        assertThat(existing.getCategory()).isSameAs(category);
        assertThat(existing.getInstructor()).isSameAs(instructor);
        assertThat(existing.getIsPublished()).isTrue();
        verify(categoryRepository).findById(3L);
        verify(instructorRepository).findById(4L);
    }

    @Test
    void delete_whenMissing_throws() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(9L)).thenReturn(true);

        service.delete(9L);

        verify(repository).deleteById(9L);
    }

    @Test
    void search_clampsPageAndSize_andBuildsSort() {
        CourseSearchRequest req = CourseSearchRequest.builder()
                .page(-5)
                .size(0)
                .sortBy(" ")
                .sortDir("desc")
                .build();

        Course course = new Course();
        course.setCourseId(1L);
        when(repository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1);
                    return new PageImpl<>(List.of(course), p, 1);
                });
        when(mapper.toDTO(course)).thenReturn(CourseDTO.builder().courseId(1L).name("X").level("A1").build());

        PageResponse<CourseDTO> resp = service.search(req);

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("courseId")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("courseId").getDirection().name()).isEqualTo("DESC");
        assertThat(resp.getContent()).hasSize(1);
    }

    @Test
    void publish_setsPublishedTrue() {
        Course existing = new Course();
        existing.setCourseId(5L);
        existing.setIsPublished(false);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(CourseDTO.builder().courseId(5L).isPublished(true).build());

        CourseDTO res = service.publish(5L);

        assertThat(existing.getIsPublished()).isTrue();
        verify(repository).save(existing);
        assertThat(res.getCourseId()).isEqualTo(5L);
    }

    @Test
    void getAll_mapsAllCourses() {
        Course a = new Course();
        a.setCourseId(1L);
        Course b = new Course();
        b.setCourseId(2L);
        when(repository.findAll()).thenReturn(List.of(a, b));
        when(mapper.toDTO(a)).thenReturn(CourseDTO.builder().courseId(1L).name("A").level("A1").build());
        when(mapper.toDTO(b)).thenReturn(CourseDTO.builder().courseId(2L).name("B").level("A2").build());

        List<CourseDTO> res = service.getAll();

        assertThat(res).extracting(CourseDTO::getCourseId).containsExactly(1L, 2L);
    }
}

