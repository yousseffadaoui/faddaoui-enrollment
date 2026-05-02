package com.englishschool.enrollmentservice.service;

import com.englishschool.enrollmentservice.client.CourseFeignClient;
import com.englishschool.enrollmentservice.dto.EnrollmentDTO;
import com.englishschool.enrollmentservice.entity.Enrollment;
import com.englishschool.enrollmentservice.entity.IdempotencyKey;
import com.englishschool.enrollmentservice.exception.ResourceNotFoundException;
import com.englishschool.enrollmentservice.repository.CertificateRepository;
import com.englishschool.enrollmentservice.repository.EnrollmentRepository;
import com.englishschool.enrollmentservice.repository.IdempotencyKeyRepository;
import com.englishschool.enrollmentservice.repository.ProgressRepository;
import com.englishschool.enrollmentservice.repository.StudentProgressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository repository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private StudentProgressRepository studentProgressRepository;
    @Mock private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock private CourseFeignClient courseFeignClient;
    @Mock private ProgressService progressService;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(
                repository,
                certificateRepository,
                progressRepository,
                studentProgressRepository,
                idempotencyKeyRepository,
                courseFeignClient,
                progressService,
                new ObjectMapper()
        );
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_mapsToDtos() {
        Enrollment e1 = Enrollment.builder().id(1L).userId(1L).courseId(10L).status("active").build();
        Enrollment e2 = Enrollment.builder().id(2L).userId(2L).courseId(11L).status("active").build();
        when(repository.findAll()).thenReturn(List.of(e1, e2));
        when(progressService.getCourseProgressPercentCached(anyLong())).thenReturn(0);

        List<EnrollmentDTO> all = enrollmentService.getAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isEqualTo(1L);
        assertThat(all.get(1).getCourseId()).isEqualTo(11L);
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.update(5L, EnrollmentDTO.builder().courseId(10L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.delete(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enroll_savesNewEnrollment_andReturnsDto() {
        when(repository.findByUserIdAndCourseId(7L, 10L)).thenReturn(Optional.empty());
        when(progressService.getCourseProgressPercentCached(anyLong())).thenReturn(0);
        when(repository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });

        EnrollmentDTO req = EnrollmentDTO.builder()
                .userId(7L)
                .studentName("Alice")
                .courseId(10L)
                .status("active")
                .build();

        EnrollmentDTO res = enrollmentService.enroll(req, null);

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getUserId()).isEqualTo(7L);
        assertThat(res.getCourseId()).isEqualTo(10L);
        verify(repository).save(any(Enrollment.class));
    }

    @Test
    void cancel_notFound_throwsResourceNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.cancel(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_success_setsCancelled() {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).status("active").build();
        when(repository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(repository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(progressService.getCourseProgressPercentCached(anyLong())).thenReturn(0);

        EnrollmentDTO dto = enrollmentService.cancel(1L);

        assertThat(dto.getStatus()).isEqualTo("cancelled");
        verify(repository).save(enrollment);
    }

    @Test
    void delete_success_deletesDependents_thenEnrollment() {
        when(repository.existsById(5L)).thenReturn(true);

        enrollmentService.delete(5L);

        verify(certificateRepository).deleteByEnrollmentId(5L);
        verify(progressRepository).deleteByEnrollmentId(5L);
        verify(studentProgressRepository).deleteByEnrollmentId(5L);
        verify(repository).deleteById(5L);
    }

    @Test
    void enroll_whenExistingActiveEnrollment_returnsExisting_withoutSavingNew() {
        Enrollment existing = Enrollment.builder()
                .id(1L)
                .userId(7L)
                .studentName("Alice")
                .courseId(10L)
                .status("active")
                .build();

        when(repository.findByUserIdAndCourseId(7L, 10L)).thenReturn(Optional.of(existing));
        when(progressService.getCourseProgressPercentCached(1L)).thenReturn(25);

        EnrollmentDTO req = EnrollmentDTO.builder()
                .userId(7L)
                .studentName("Alice")
                .courseId(10L)
                .build();

        EnrollmentDTO res = enrollmentService.enroll(req, null);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getProgressPercent()).isEqualTo(25);
        verify(repository, never()).save(any(Enrollment.class));
    }

    @Test
    void enroll_withIdempotencyKey_returnsStoredResponse_whenPresent() throws Exception {
        // pre-stored idempotent response
        EnrollmentDTO stored = EnrollmentDTO.builder().id(9L).courseId(10L).studentName("Alice").build();
        String json = new ObjectMapper().writeValueAsString(stored);

        when(idempotencyKeyRepository.findByKeyHash(anyString()))
                .thenReturn(Optional.of(IdempotencyKey.builder().keyHash("x").responseBody(json).build()));

        EnrollmentDTO req = EnrollmentDTO.builder()
                .userId(7L)
                .studentName("Alice")
                .courseId(10L)
                .build();

        EnrollmentDTO res = enrollmentService.enroll(req, "idem-key");

        assertThat(res.getId()).isEqualTo(9L);
        verifyNoInteractions(repository);
    }
}

