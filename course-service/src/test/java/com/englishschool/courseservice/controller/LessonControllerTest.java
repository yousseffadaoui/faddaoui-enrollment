package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.dto.LessonDTO;
import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.service.LessonService;
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

@WebMvcTest(LessonController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService service;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void getByModuleId_returnsList() throws Exception {
        when(service.getByModuleId(2L)).thenReturn(List.of(
                LessonDTO.builder().id(1L).moduleId(2L).title("Intro").build()
        ));

        mockMvc.perform(get("/api/v1/lessons").param("moduleId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].moduleId").value(2));
    }

    @Test
    void create_valid_returns201() throws Exception {
        LessonDTO in = LessonDTO.builder().moduleId(2L).title("Intro").build();
        when(service.create(any(LessonDTO.class))).thenReturn(LessonDTO.builder().id(10L).moduleId(2L).title("Intro").build());

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(service).create(any(LessonDTO.class));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        LessonDTO invalid = LessonDTO.builder().title("").build();

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.moduleId[0]").exists())
                .andExpect(jsonPath("$.validationErrors.title[0]").exists());
    }

    @Test
    void update_valid_returns200() throws Exception {
        LessonDTO in = LessonDTO.builder().moduleId(1L).title("New").build();
        when(service.update(eq(9L), any(LessonDTO.class))).thenReturn(LessonDTO.builder().id(9L).moduleId(1L).title("New").build());

        mockMvc.perform(put("/api/v1/lessons/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.title").value("New"));

        verify(service).update(eq(9L), any(LessonDTO.class));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/4"))
                .andExpect(status().isNoContent());

        verify(service).delete(4L);
    }
}

