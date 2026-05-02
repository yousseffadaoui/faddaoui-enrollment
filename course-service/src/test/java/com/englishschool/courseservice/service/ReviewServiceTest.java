package com.englishschool.courseservice.service;

import com.englishschool.courseservice.dto.ReviewDTO;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Review;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.mapper.ReviewMapper;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository repository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ReviewMapper mapper;

    @InjectMocks
    private ReviewService service;

    @Captor
    private ArgumentCaptor<Course> courseCaptor;

    @Test
    void create_whenCourseMissing_throwsNotFound() {
        ReviewDTO in = ReviewDTO.builder().courseId(1L).userId(2L).rating(5).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenAlreadyReviewed_throwsIllegalArgument() {
        Course course = new Course();
        course.setCourseId(3L);
        ReviewDTO in = ReviewDTO.builder().courseId(3L).userId(9L).rating(4).build();
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(repository.existsByCourseCourseIdAndUserId(3L, 9L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already reviewed");

        verify(repository, never()).save(any());
    }

    @Test
    void create_recalculatesRating_whenNoReviews_setsNullAndZero() {
        Course course = new Course();
        course.setCourseId(3L);
        course.setRatingAvg(BigDecimal.valueOf(4.00));
        course.setRatingCount(10);

        ReviewDTO in = ReviewDTO.builder().courseId(3L).userId(9L).rating(5).comment("Great").build();
        Review entity = new Review();
        Review saved = Review.builder().id(100L).course(course).userId(9L).rating(5).comment("Great").build();

        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(repository.existsByCourseCourseIdAndUserId(3L, 9L)).thenReturn(false);
        when(mapper.toEntity(in)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(ReviewDTO.builder().id(100L).courseId(3L).userId(9L).rating(5).build());
        when(repository.findByCourseCourseId(3L)).thenReturn(List.of());
        when(courseRepository.save(courseCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO res = service.create(in);

        assertThat(res.getId()).isEqualTo(100L);
        Course updated = courseCaptor.getValue();
        assertThat(updated.getRatingAvg()).isNull();
        assertThat(updated.getRatingCount()).isEqualTo(0);
    }

    @Test
    void update_recalculatesRating_withAverageRounded() {
        Course course = new Course();
        course.setCourseId(7L);
        Review existing = Review.builder().id(5L).course(course).userId(1L).rating(2).comment("meh").build();

        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(ReviewDTO.builder().id(5L).courseId(7L).rating(4).build());

        when(repository.findByCourseCourseId(7L)).thenReturn(List.of(
                Review.builder().id(1L).course(course).rating(4).build(),
                Review.builder().id(2L).course(course).rating(5).build()
        ));
        when(courseRepository.save(courseCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO in = ReviewDTO.builder().rating(4).comment("better").build();

        ReviewDTO res = service.update(5L, in);

        assertThat(existing.getRating()).isEqualTo(4);
        assertThat(res.getId()).isEqualTo(5L);
        Course updated = courseCaptor.getValue();
        assertThat(updated.getRatingCount()).isEqualTo(2);
        assertThat(updated.getRatingAvg()).isEqualTo(new BigDecimal("4.50"));
    }

    @Test
    void delete_removesReview_andRecalculates() {
        Course course = new Course();
        course.setCourseId(7L);
        Review existing = Review.builder().id(5L).course(course).userId(1L).rating(2).build();

        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByCourseCourseId(7L)).thenReturn(List.of());
        when(courseRepository.save(courseCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.delete(5L);

        verify(repository).delete(existing);
        Course updated = courseCaptor.getValue();
        assertThat(updated.getRatingAvg()).isNull();
        assertThat(updated.getRatingCount()).isEqualTo(0);
    }

    @Test
    void getById_notFound_throws() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void getById_success_mapsToDto() {
        Course course = new Course();
        course.setCourseId(1L);
        Review r = Review.builder().id(10L).course(course).userId(5L).rating(4).build();
        when(repository.findById(10L)).thenReturn(Optional.of(r));
        when(mapper.toDTO(r)).thenReturn(ReviewDTO.builder().id(10L).courseId(1L).userId(5L).rating(4).build());

        ReviewDTO dto = service.getById(10L);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getCourseId()).isEqualTo(1L);
    }

    @Test
    void getByCourseId_buildsPageResponse() {
        Course course = new Course();
        course.setCourseId(5L);
        Review r = Review.builder().id(1L).course(course).userId(2L).rating(5).build();
        var pageable = PageRequest.of(0, 20);
        when(repository.findByCourseCourseId(eq(5L), any())).thenReturn(new PageImpl<>(List.of(r), pageable, 1));
        when(mapper.toDTO(r)).thenReturn(ReviewDTO.builder().id(1L).courseId(5L).userId(2L).rating(5).build());

        var resp = service.getByCourseId(5L, 0, 20);

        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getTotalElements()).isEqualTo(1);
        assertThat(resp.isFirst()).isTrue();
        assertThat(resp.isLast()).isTrue();
    }
}

