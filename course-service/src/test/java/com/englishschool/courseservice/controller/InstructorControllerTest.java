package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.dto.InstructorDTO;
import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.service.InstructorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstructorController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class InstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstructorService service;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAll()).thenReturn(List.of(
                InstructorDTO.builder().id(1L).firstName("Jane").lastName("Doe").build()
        ));

        mockMvc.perform(get("/api/v1/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Jane"));
    }

    @Test
    void create_valid_returns201() throws Exception {
        InstructorDTO in = InstructorDTO.builder().firstName("John").lastName("Smith").email("john@school.com").build();
        when(service.create(any(InstructorDTO.class))).thenReturn(InstructorDTO.builder().id(10L).firstName("John").lastName("Smith").build());

        mockMvc.perform(post("/api/v1/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(service).create(any(InstructorDTO.class));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        InstructorDTO invalid = InstructorDTO.builder().firstName(" ").lastName("").build();

        mockMvc.perform(post("/api/v1/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.firstName[0]").exists())
                .andExpect(jsonPath("$.validationErrors.lastName[0]").exists());
    }

    @Test
    void update_valid_returns200() throws Exception {
        InstructorDTO in = InstructorDTO.builder().firstName("New").lastName("Name").build();
        when(service.update(eq(5L), any(InstructorDTO.class))).thenReturn(InstructorDTO.builder().id(5L).firstName("New").lastName("Name").build());

        mockMvc.perform(put("/api/v1/instructors/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.lastName").value("Name"));

        verify(service).update(eq(5L), any(InstructorDTO.class));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/instructors/4"))
                .andExpect(status().isNoContent());

        verify(service).delete(4L);
    }
}

