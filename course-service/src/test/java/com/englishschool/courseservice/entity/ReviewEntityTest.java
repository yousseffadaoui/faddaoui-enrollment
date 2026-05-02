package com.englishschool.courseservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewEntityTest {

    @Test
    void onUpdate_whenRatingOutOfRange_throwsIllegalArgument() {
        Review r = Review.builder().rating(6).build();

        assertThatThrownBy(r::onUpdate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 5");
    }

    @Test
    void onUpdate_whenRatingValid_doesNotThrow() {
        Review r = Review.builder().rating(5).build();

        assertThatCode(r::onUpdate).doesNotThrowAnyException();
    }
}

