package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.dto.CourseDTO;
import com.englishschool.courseservice.dto.CourseSearchRequest;
import com.englishschool.courseservice.dto.PageResponse;
import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService service;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void test_healthcheck() throws Exception {
        mockMvc.perform(get("/api/v1/courses/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Course Service is working"));
    }

    @Test
    void getAll_withoutSearchParams_returnsList() throws Exception {
        when(service.getAll()).thenReturn(List.of(
                CourseDTO.builder().courseId(1L).name("A").level("A1").build(),
                CourseDTO.builder().courseId(2L).name("B").level("A2").build()
        ));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").value(1))
                .andExpect(jsonPath("$[1].courseId").value(2));

        verify(service).getAll();
    }

    @Test
    void getAll_withSearchParam_delegatesToSearch() throws Exception {
        PageResponse<CourseDTO> response = PageResponse.<CourseDTO>builder()
                .content(List.of(CourseDTO.builder().courseId(1L).name("English").level("A1").build()))
                .page(0).size(20).totalElements(1).totalPages(1).first(true).last(true)
                .build();
        when(service.search(any(CourseSearchRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/courses").param("search", "English"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].courseId").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).search(any(CourseSearchRequest.class));
    }

    @Test
    void getById_returnsDto() throws Exception {
        when(service.getById(5L)).thenReturn(CourseDTO.builder().courseId(5L).name("X").level("B1").build());

        mockMvc.perform(get("/api/v1/courses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(5))
                .andExpect(jsonPath("$.level").value("B1"));
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Course", 99L));

        mockMvc.perform(get("/api/v1/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Course not found with id: 99"));
    }

    @Test
    void create_valid_returns201() throws Exception {
        CourseDTO in = CourseDTO.builder()
                .name("English A1")
                .level("A1")
                .price(BigDecimal.valueOf(10))
                .build();
        when(service.create(any(CourseDTO.class))).thenReturn(CourseDTO.builder().courseId(10L).name("English A1").level("A1").build());

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(10))
                .andExpect(jsonPath("$.name").value("English A1"));

        verify(service).create(any(CourseDTO.class));
    }

    @Test
    void create_invalid_returns400_withValidationErrors() throws Exception {
        CourseDTO invalid = CourseDTO.builder().level("A1").build();

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.name[0]").exists());
    }

    @Test
    void update_valid_returns200() throws Exception {
        CourseDTO in = CourseDTO.builder().name("New").level("A2").build();
        when(service.update(eq(7L), any(CourseDTO.class))).thenReturn(CourseDTO.builder().courseId(7L).name("New").level("A2").build());

        mockMvc.perform(put("/api/v1/courses/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(7))
                .andExpect(jsonPath("$.level").value("A2"));

        verify(service).update(eq(7L), any(CourseDTO.class));
    }

    @Test
    void publish_returns200() throws Exception {
        when(service.publish(3L)).thenReturn(CourseDTO.builder().courseId(3L).isPublished(true).build());

        mockMvc.perform(patch("/api/v1/courses/3/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(3));

        verify(service).publish(3L);
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/courses/4"))
                .andExpect(status().isNoContent());

        verify(service).delete(4L);
    }
}

