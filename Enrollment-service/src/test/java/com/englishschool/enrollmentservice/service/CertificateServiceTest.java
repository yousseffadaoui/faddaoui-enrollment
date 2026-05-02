package com.englishschool.enrollmentservice.service;

import com.englishschool.enrollmentservice.client.CourseDTO;
import com.englishschool.enrollmentservice.client.CourseFeignClient;
import com.englishschool.enrollmentservice.entity.Enrollment;
import com.englishschool.enrollmentservice.exception.ResourceNotFoundException;
import com.englishschool.enrollmentservice.repository.CertificateRepository;
import com.englishschool.enrollmentservice.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseFeignClient courseFeignClient;

    private CertificateService certificateService;

    @BeforeEach
    void setUp() {
        certificateService = new CertificateService(certificateRepository, enrollmentRepository, courseFeignClient);
    }

    @Test
    void generateCertificatePdf_enrollmentNotFound_throwsResourceNotFound() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.generateCertificatePdf(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateCertificatePdf_notCompleted_throwsIllegalState() {
        Enrollment enrollment = Enrollment.builder()
                .id(1L)
                .userId(7L)
                .courseId(10L)
                .status("active")
                .studentName("Alice")
                .build();
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> certificateService.generateCertificatePdf(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateCertificatePdf_completed_returnsPdfBytes() {
        Enrollment enrollment = Enrollment.builder()
                .id(1L)
                .userId(7L)
                .courseId(10L)
                .status("completed")
                .studentName("Alice")
                .build();
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(certificateRepository.findByEnrollment_Id(1L)).thenReturn(Optional.empty());
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("English A1");
        when(courseFeignClient.getCourseById(10L)).thenReturn(courseDTO);

        byte[] pdf = certificateService.generateCertificatePdf(1L);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(10);
        verify(certificateRepository).save(any());
    }
}

