package com.englishschool.courseservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

class CourseServiceApplicationTests {

    @Test
    void loadSampleData_runsWithoutStartingSpring() throws Exception {
        var repository = org.mockito.Mockito.mock(com.englishschool.courseservice.repository.CourseRepository.class);
        CourseServiceApplication app = new CourseServiceApplication();

        CommandLineRunner runner = app.loadSampleData(repository);
        runner.run();

        org.mockito.Mockito.verify(repository).deleteAll();
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.times(3))
                .save(org.mockito.ArgumentMatchers.any(com.englishschool.courseservice.entity.Course.class));
    }

}
