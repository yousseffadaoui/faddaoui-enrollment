package com.englishschool.courseservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    @Test
    void corsFilter_hasExpectedDefaults() {
        WebConfig config = new WebConfig();
        CorsFilter filter = config.corsFilter();

        assertThat(filter).isNotNull();
    }

    @Test
    void addResourceHandlers_registersUploadsPath() {
        WebConfig config = new WebConfig();
        var appContext = new org.springframework.context.support.StaticApplicationContext();
        var servletContext = new org.springframework.mock.web.MockServletContext();
        var registry = new org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry(appContext, servletContext);

        config.addResourceHandlers(registry);
        // If no exception thrown, registration worked.
        assertThat(registry.hasMappingForPattern("/uploads/**")).isTrue();
    }
}

