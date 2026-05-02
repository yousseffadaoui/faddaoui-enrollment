package com.englishschool.enrollmentservice.controller;

import com.englishschool.enrollmentservice.dto.ProgressDTO;
import com.englishschool.enrollmentservice.exception.ResourceNotFoundException;
import com.englishschool.enrollmentservice.service.CertificateService;
import com.englishschool.enrollmentservice.service.EnrollmentService;
import com.englishschool.enrollmentservice.service.ProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProgressService progressService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private CertificateService certificateService;

    @Test
    void markLessonComplete_returns200() throws Exception {
        ProgressDTO dto = ProgressDTO.builder()
                .id(1L)
                .enrollmentId(10L)
                .lessonId(99L)
                .build();
        when(progressService.markLessonComplete(10L, 99L)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/progress/enrollments/10/lessons/99/complete"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enrollmentId").value(10))
                .andExpect(jsonPath("$.lessonId").value(99));
    }

    @Test
    void markLessonComplete_notFound_returns404() throws Exception {
        when(progressService.markLessonComplete(10L, 99L))
                .thenThrow(new ResourceNotFoundException("Enrollment", 10L));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/progress/enrollments/10/lessons/99/complete"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getLessonProgress_returns200() throws Exception {
        when(progressService.getLessonProgress(10L)).thenReturn(List.of(
                ProgressDTO.builder().id(1L).enrollmentId(10L).lessonId(1L).build(),
                ProgressDTO.builder().id(2L).enrollmentId(10L).lessonId(2L).build()
        ));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/progress/enrollments/10/lessons"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getCourseProgressPercent_returns200_andMap() throws Exception {
        when(progressService.getCourseProgressPercent(10L)).thenReturn(75);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/progress/enrollments/10/percent"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.progressPercent").value(75));
    }
}

