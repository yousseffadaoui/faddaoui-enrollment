package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.dto.CategoryDTO;
import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.service.CategoryService;
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

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService service;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAll()).thenReturn(List.of(
                CategoryDTO.builder().id(1L).name("Grammar").slug("grammar").build()
        ));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].slug").value("grammar"));

        verify(service).getAll();
    }

    @Test
    void create_valid_returns201() throws Exception {
        CategoryDTO in = CategoryDTO.builder().name("Vocabulary").build();
        when(service.create(any(CategoryDTO.class))).thenReturn(CategoryDTO.builder().id(10L).name("Vocabulary").slug("vocabulary").build());

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.slug").value("vocabulary"));

        verify(service).create(any(CategoryDTO.class));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        CategoryDTO invalid = CategoryDTO.builder().name(" ").build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name[0]").exists());
    }

    @Test
    void update_valid_returns200() throws Exception {
        CategoryDTO in = CategoryDTO.builder().name("Updated").build();
        when(service.update(eq(5L), any(CategoryDTO.class))).thenReturn(CategoryDTO.builder().id(5L).name("Updated").slug("updated").build());

        mockMvc.perform(put("/api/v1/categories/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(service).update(eq(5L), any(CategoryDTO.class));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/3"))
                .andExpect(status().isNoContent());

        verify(service).delete(3L);
    }
}

