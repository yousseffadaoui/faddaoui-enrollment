package com.englishschool.courseservice.controller;

import com.englishschool.courseservice.exception.GlobalExceptionHandler;
import com.englishschool.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UploadController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Needed because CourseServiceApplication defines loadSampleData(CourseRepository)
    @MockBean
    private CourseRepository courseRepository;

    @Test
    void uploadImage_whenEmptyFile_returns400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/v1/upload/image").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No file selected"));
    }

    @Test
    void uploadImage_whenInvalidContentType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());

        mockMvc.perform(multipart("/api/v1/upload/image").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid file type. Use JPEG, PNG, GIF or WebP"));
    }

    @Test
    void uploadImage_whenValidPng_returns201_andBodyContainsUrlAndFilename() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "png".getBytes());

        mockMvc.perform(multipart("/api/v1/upload/image").file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.startsWith("/uploads/")))
                .andExpect(jsonPath("$.filename").value(org.hamcrest.Matchers.endsWith(".png")));
    }
}

