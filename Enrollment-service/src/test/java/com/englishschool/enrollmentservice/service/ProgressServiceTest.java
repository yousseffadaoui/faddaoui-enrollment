package com.englishschool.enrollmentservice.service;

import com.englishschool.enrollmentservice.client.CourseFeignClient;
import com.englishschool.enrollmentservice.client.ModuleDTO;
import com.englishschool.enrollmentservice.dto.ProgressDTO;
import com.englishschool.enrollmentservice.entity.Enrollment;
import com.englishschool.enrollmentservice.entity.Progress;
import com.englishschool.enrollmentservice.entity.StudentProgress;
import com.englishschool.enrollmentservice.exception.ResourceNotFoundException;
import com.englishschool.enrollmentservice.repository.EnrollmentRepository;
import com.englishschool.enrollmentservice.repository.ProgressRepository;
import com.englishschool.enrollmentservice.repository.StudentProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private StudentProgressRepository studentProgressRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseFeignClient courseFeignClient;

    private ProgressService progressService;

    @BeforeEach
    void setUp() {
        progressService = new ProgressService(progressRepository, studentProgressRepository, enrollmentRepository, courseFeignClient);
    }

    @Test
    void markLessonComplete_enrollmentNotFound_throwsResourceNotFound() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.markLessonComplete(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markLessonComplete_nonActiveEnrollment_throwsIllegalState() {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).status("cancelled").build();
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> progressService.markLessonComplete(1L, 2L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markLessonComplete_whenAlreadyExists_returnsExistingDto() {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).status("active").build();
        Progress existing = Progress.builder().id(5L).enrollment(enrollment).lessonId(2L).build();

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(progressRepository.existsByEnrollment_IdAndLessonId(1L, 2L)).thenReturn(true);
        when(progressRepository.findByEnrollment_Id(1L)).thenReturn(List.of(existing));

        ProgressDTO dto = progressService.markLessonComplete(1L, 2L);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getEnrollmentId()).isEqualTo(1L);
        assertThat(dto.getLessonId()).isEqualTo(2L);
        verify(progressRepository, never()).save(any());
    }

    @Test
    void getLessonProgress_returnsList() {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).status("active").build();
        when(progressRepository.findByEnrollment_Id(1L)).thenReturn(List.of(
                Progress.builder().id(1L).enrollment(enrollment).lessonId(1L).build(),
                Progress.builder().id(2L).enrollment(enrollment).lessonId(2L).build()
        ));

        List<ProgressDTO> list = progressService.getLessonProgress(1L);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getLessonId()).isEqualTo(1L);
        assertThat(list.get(1).getLessonId()).isEqualTo(2L);
    }

    @Test
    void getCourseProgressPercentCached_whenAbsent_returns0() {
        when(studentProgressRepository.findByEnrollment_Id(1L)).thenReturn(Optional.empty());
        assertThat(progressService.getCourseProgressPercentCached(1L)).isEqualTo(0);
    }

    @Test
    void getCourseProgressPercentCached_whenPresent_returnsPercent() {
        when(studentProgressRepository.findByEnrollment_Id(1L))
                .thenReturn(Optional.of(StudentProgress.builder().progressPercent(77).build()));
        assertThat(progressService.getCourseProgressPercentCached(1L)).isEqualTo(77);
    }

    @Test
    void getCourseProgressPercent_whenPresent_returnsExisting_withoutCompute() {
        when(studentProgressRepository.findByEnrollment_Id(1L))
                .thenReturn(Optional.of(StudentProgress.builder().progressPercent(33).build()));

        int percent = progressService.getCourseProgressPercent(1L);

        assertThat(percent).isEqualTo(33);
        verifyNoInteractions(enrollmentRepository);
        verifyNoInteractions(progressRepository);
    }

    @Test
    void getCourseProgressPercent_whenAbsent_computesAndSaves_andCompletesEnrollmentAt100() {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).status("active").build();

        when(studentProgressRepository.findByEnrollment_Id(1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        // Make Feign fail -> totalLessons falls back to 1
        when(courseFeignClient.getModulesByCourseId(anyLong(), anyBoolean())).thenThrow(new RuntimeException("down"));

        // One completed lesson -> 100% when totalLessons=1
        when(progressRepository.findByEnrollment_Id(1L)).thenReturn(List.of(
                Progress.builder().id(1L).enrollment(enrollment).lessonId(99L).build()
        ));

        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        int percent = progressService.getCourseProgressPercent(1L);

        assertThat(percent).isEqualTo(100);
        assertThat(enrollment.getStatus()).isEqualTo("completed");
        assertThat(enrollment.getCompletedAt()).isNotNull();
        verify(enrollmentRepository).save(enrollment);
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void getCourseProgressPercent_whenAbsent_computesUsingModules() {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).status("active").build();

        ModuleDTO m1 = new ModuleDTO();
        m1.setLessons(List.of(new com.englishschool.enrollmentservice.client.LessonDTO(), new com.englishschool.enrollmentservice.client.LessonDTO()));

        when(studentProgressRepository.findByEnrollment_Id(1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(courseFeignClient.getModulesByCourseId(10L, true)).thenReturn(List.of(m1));
        when(progressRepository.findByEnrollment_Id(1L)).thenReturn(List.of(
                Progress.builder().id(1L).enrollment(enrollment).lessonId(1L).build()
        ));
        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        int percent = progressService.getCourseProgressPercent(1L);

        assertThat(percent).isEqualTo(50);
        verify(studentProgressRepository).save(any(StudentProgress.class));
        verify(enrollmentRepository, never()).save(any());
    }
}

