package com.englishschool.enrollmentservice.controller;

import com.englishschool.enrollmentservice.dto.EnrollmentDTO;
import com.englishschool.enrollmentservice.exception.ResourceNotFoundException;
import com.englishschool.enrollmentservice.service.CertificateService;
import com.englishschool.enrollmentservice.service.EnrollmentService;
import com.englishschool.enrollmentservice.service.ProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private ProgressService progressService;

    @MockBean
    private CertificateService certificateService;

    @Test
    void postEnroll_returns201_andBody() throws Exception {
        EnrollmentDTO req = EnrollmentDTO.builder()
                .courseId(10L)
                .studentName("Alice")
                .userId(7L)
                .status("active")
                .build();

        EnrollmentDTO res = EnrollmentDTO.builder()
                .id(99L)
                .courseId(10L)
                .studentName("Alice")
                .userId(7L)
                .status("active")
                .progressPercent(0)
                .build();

        when(enrollmentService.enroll(any(EnrollmentDTO.class), any())).thenReturn(res);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "abc")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.courseId").value(10))
                .andExpect(jsonPath("$.studentName").value("Alice"));
    }

    @Test
    void postEnroll_missingCourseId_returns400() throws Exception {
        EnrollmentDTO req = EnrollmentDTO.builder()
                .studentName("Alice")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returns200() throws Exception {
        when(enrollmentService.getAll()).thenReturn(List.of(
                EnrollmentDTO.builder().id(1L).courseId(10L).build(),
                EnrollmentDTO.builder().id(2L).courseId(11L).build()
        ));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollments"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getById_notFound_returns404_withErrorBody() throws Exception {
        when(enrollmentService.getById(404L)).thenThrow(new ResourceNotFoundException("Enrollment", 404L));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/enrollments/404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void putUpdate_returns200() throws Exception {
        EnrollmentDTO req = EnrollmentDTO.builder().courseId(10L).status("active").build();
        EnrollmentDTO res = EnrollmentDTO.builder().id(5L).courseId(10L).status("completed").build();
        when(enrollmentService.update(eq(5L), any(EnrollmentDTO.class))).thenReturn(res);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/enrollments/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/enrollments/5"))
                .andExpect(status().isNoContent());
    }
}

