package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.dto.ModuleDTO;
import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.service.ModuleService;
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

@WebMvcTest(ModuleController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ModuleService service;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void getByCourseId_returnsList() throws Exception {
        when(service.getByCourseId(5L, true)).thenReturn(List.of(
                ModuleDTO.builder().id(1L).courseId(5L).title("M1").build()
        ));

        mockMvc.perform(get("/api/v1/modules").param("courseId", "5").param("includeLessons", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].courseId").value(5));

        verify(service).getByCourseId(5L, true);
    }

    @Test
    void create_valid_returns201() throws Exception {
        ModuleDTO in = ModuleDTO.builder().courseId(2L).title("Module").orderIndex(0).build();
        when(service.create(any(ModuleDTO.class))).thenReturn(ModuleDTO.builder().id(10L).courseId(2L).title("Module").build());

        mockMvc.perform(post("/api/v1/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(service).create(any(ModuleDTO.class));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        ModuleDTO invalid = ModuleDTO.builder().title(" ").build();

        mockMvc.perform(post("/api/v1/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.courseId[0]").exists())
                .andExpect(jsonPath("$.validationErrors.title[0]").exists());
    }

    @Test
    void update_valid_returns200() throws Exception {
        ModuleDTO in = ModuleDTO.builder().courseId(1L).title("New").build();
        when(service.update(eq(9L), any(ModuleDTO.class))).thenReturn(ModuleDTO.builder().id(9L).courseId(1L).title("New").build());

        mockMvc.perform(put("/api/v1/modules/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.title").value("New"));

        verify(service).update(eq(9L), any(ModuleDTO.class));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/modules/4"))
                .andExpect(status().isNoContent());

        verify(service).delete(4L);
    }
}

