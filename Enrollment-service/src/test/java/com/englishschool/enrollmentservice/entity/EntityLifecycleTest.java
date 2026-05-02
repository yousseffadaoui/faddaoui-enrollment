package com.englishschool.enrollmentservice.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EntityLifecycleTest {

    @Test
    void enrollment_prePersist_setsEnrolledAt_whenNull_andDefaultStatus() throws Exception {
        Enrollment e = Enrollment.builder()
                .courseId(10L)
                .build();

        assertThat(e.getStatus()).isEqualTo("active");
        assertThat(e.getEnrolledAt()).isNull();

        invokeLifecycle(e, "onCreate");

        assertThat(e.getEnrolledAt()).isNotNull();
    }

    @Test
    void progress_prePersist_setsCompletedAt() throws Exception {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).build();
        Progress p = Progress.builder()
                .enrollment(enrollment)
                .lessonId(2L)
                .build();

        assertThat(p.getCompletedAt()).isNull();
        invokeLifecycle(p, "onCreate");
        assertThat(p.getCompletedAt()).isNotNull();
    }

    @Test
    void certificate_prePersist_setsIssuedAt() throws Exception {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).userId(7L).build();
        Certificate c = Certificate.builder()
                .enrollment(enrollment)
                .userId(7L)
                .courseId(10L)
                .build();

        assertThat(c.getIssuedAt()).isNull();
        invokeLifecycle(c, "onCreate");
        assertThat(c.getIssuedAt()).isNotNull();
    }

    @Test
    void idempotencyKey_prePersist_setsCreatedAt_whenNull() throws Exception {
        IdempotencyKey key = IdempotencyKey.builder()
                .keyHash("hash")
                .responseBody("{}")
                .build();

        assertThat(key.getCreatedAt()).isNull();
        invokeLifecycle(key, "onCreate");
        assertThat(key.getCreatedAt()).isNotNull();
    }

    @Test
    void studentProgress_prePersist_setsUpdatedAt_andDefaults() throws Exception {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).build();
        StudentProgress sp = StudentProgress.builder()
                .enrollment(enrollment)
                .build();

        assertThat(sp.getTotalLessons()).isEqualTo(0);
        assertThat(sp.getCompletedLessons()).isEqualTo(0);
        assertThat(sp.getProgressPercent()).isEqualTo(0);

        assertThat(sp.getUpdatedAt()).isNull();
        invokeLifecycle(sp, "onUpdate");
        assertThat(sp.getUpdatedAt()).isNotNull();
    }

    @Test
    void studentProgress_preUpdate_refreshesUpdatedAt() throws Exception {
        Enrollment enrollment = Enrollment.builder().id(1L).courseId(10L).build();
        StudentProgress sp = StudentProgress.builder()
                .enrollment(enrollment)
                .updatedAt(Instant.EPOCH)
                .build();

        invokeLifecycle(sp, "onUpdate");
        assertThat(sp.getUpdatedAt()).isAfter(Instant.EPOCH);
    }

    private static void invokeLifecycle(Object target, String method) throws Exception {
        Method m = target.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(target);
    }
}

