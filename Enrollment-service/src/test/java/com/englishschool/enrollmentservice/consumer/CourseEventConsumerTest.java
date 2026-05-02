package com.englishschool.enrollmentservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class CourseEventConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void consume_courseDeleted_validMessage_doesNotThrow() {
        CourseEventConsumer consumer = new CourseEventConsumer(objectMapper);

        assertThatCode(() -> consumer.consume("""
                {"eventType":"COURSE_DELETED","courseId":123}
                """)).doesNotThrowAnyException();
    }

    @Test
    void consume_courseUnpublished_validMessage_doesNotThrow() {
        CourseEventConsumer consumer = new CourseEventConsumer(objectMapper);

        assertThatCode(() -> consumer.consume("""
                {"eventType":"COURSE_UNPUBLISHED","courseId":123}
                """)).doesNotThrowAnyException();
    }

    @Test
    void consume_unknownEvent_validMessage_doesNotThrow() {
        CourseEventConsumer consumer = new CourseEventConsumer(objectMapper);

        assertThatCode(() -> consumer.consume("""
                {"eventType":"SOMETHING_ELSE","courseId":123}
                """)).doesNotThrowAnyException();
    }

    @Test
    void consume_missingCourseId_doesNotThrow() {
        CourseEventConsumer consumer = new CourseEventConsumer(objectMapper);

        assertThatCode(() -> consumer.consume("""
                {"eventType":"COURSE_DELETED"}
                """)).doesNotThrowAnyException();
    }

    @Test
    void consume_invalidJson_doesNotThrow() {
        CourseEventConsumer consumer = new CourseEventConsumer(objectMapper);

        assertThatCode(() -> consumer.consume("{not-json")).doesNotThrowAnyException();
    }
}

