package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.dto.PageResponse;
import com.englishschool.courseservice.dto.ReviewDTO;
import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.repository.CourseRepository;
import com.englishschool.courseservice.service.ReviewService;
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

@WebMvcTest(ReviewController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService service;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void getByCourseId_returnsPageResponse() throws Exception {
        PageResponse<ReviewDTO> response = PageResponse.<ReviewDTO>builder()
                .content(List.of(ReviewDTO.builder().id(1L).courseId(5L).userId(9L).rating(5).build()))
                .page(0).size(20).totalElements(1).totalPages(1).first(true).last(true)
                .build();
        when(service.getByCourseId(5L, 0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reviews").param("courseId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void create_valid_returns201() throws Exception {
        ReviewDTO in = ReviewDTO.builder().courseId(1L).userId(2L).rating(5).comment("Great").build();
        when(service.create(any(ReviewDTO.class))).thenReturn(ReviewDTO.builder().id(10L).courseId(1L).userId(2L).rating(5).build());

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rating").value(5));

        verify(service).create(any(ReviewDTO.class));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        ReviewDTO invalid = ReviewDTO.builder().courseId(null).userId(null).rating(10).build();

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.courseId[0]").exists())
                .andExpect(jsonPath("$.validationErrors.userId[0]").exists())
                .andExpect(jsonPath("$.validationErrors.rating[0]").exists());
    }

    @Test
    void create_whenServiceThrowsIllegalArgument_returns400() throws Exception {
        ReviewDTO in = ReviewDTO.builder().courseId(1L).userId(2L).rating(5).build();
        when(service.create(any(ReviewDTO.class))).thenThrow(new IllegalArgumentException("User has already reviewed this course"));

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User has already reviewed this course"));
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Review", 99L));

        mockMvc.perform(get("/api/v1/reviews/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Review not found with id: 99"));
    }

    @Test
    void update_valid_returns200() throws Exception {
        ReviewDTO in = ReviewDTO.builder().courseId(1L).userId(2L).rating(4).build();
        when(service.update(eq(5L), any(ReviewDTO.class))).thenReturn(ReviewDTO.builder().id(5L).courseId(1L).userId(2L).rating(4).build());

        mockMvc.perform(put("/api/v1/reviews/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.rating").value(4));

        verify(service).update(eq(5L), any(ReviewDTO.class));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/4"))
                .andExpect(status().isNoContent());

        verify(service).delete(4L);
    }
}

