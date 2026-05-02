package com.englishschool.courseservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityLifecycleTest {

    @Test
    void module_onCreate_setsDefaultOrderIndexWhenNull() {
        Module module = Module.builder().title("M").orderIndex(null).build();

        module.onCreate();

        assertThat(module.getOrderIndex()).isEqualTo(0);
    }

    @Test
    void lesson_onCreate_setsDefaultNumbersWhenNull() {
        Lesson lesson = Lesson.builder().title("L").durationMinutes(null).orderIndex(null).build();

        lesson.onCreate();

        assertThat(lesson.getDurationMinutes()).isEqualTo(0);
        assertThat(lesson.getOrderIndex()).isEqualTo(0);
    }
}

