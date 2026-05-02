package com.englishschool.enrollmentservice.controller;

import com.englishschool.enrollmentservice.service.CertificateService;
import com.englishschool.enrollmentservice.service.EnrollmentService;
import com.englishschool.enrollmentservice.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CertificateController.class)
@AutoConfigureMockMvc(addFilters = false)
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificateService certificateService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private ProgressService progressService;

    @Test
    void downloadCertificate_returns200_pdf_andContentDisposition() throws Exception {
        byte[] pdf = new byte[]{1, 2, 3, 4};
        when(certificateService.generateCertificatePdf(7L)).thenReturn(pdf);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/certificates/enrollment/7/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate-7.pdf\""))
                .andExpect(content().bytes(pdf));
    }
}

