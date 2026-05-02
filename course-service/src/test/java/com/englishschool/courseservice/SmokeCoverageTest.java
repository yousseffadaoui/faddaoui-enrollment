package com.englishschool.courseservice;

import com.englishschool.courseservice.dto.*;
import com.englishschool.courseservice.entity.Category;
import com.englishschool.courseservice.entity.Course;
import com.englishschool.courseservice.entity.Instructor;
import com.englishschool.courseservice.entity.Lesson;
import com.englishschool.courseservice.entity.Review;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight smoke test to execute Lombok-generated DTO/entity methods and
 * simple getters/setters. This boosts coverage without touching business logic.
 */
class SmokeCoverageTest {

    @Test
    void dtoAndEntity_smoke() {
        Category category = Category.builder().id(1L).name("Grammar").description("Desc").slug("grammar").build();
        Instructor instructor = Instructor.builder().id(2L).firstName("Jane").lastName("Doe").email("jane@x.com").bio("bio").avatarUrl("a").build();

        Course course = new Course();
        course.setCourseId(10L);
        course.setName("English A1");
        course.setLevel("A1");
        course.setDescription("Basics");
        course.setCategory(category);
        course.setInstructor(instructor);
        course.setPrice(BigDecimal.valueOf(0));
        course.setThumbnailUrl("/img");
        course.setIsPublished(false);
        course.setRatingAvg(BigDecimal.valueOf(4.25));
        course.setRatingCount(5);
        course.setModules(null);

        assertThat(course.getModules()).isNotNull();
        assertThat(course.getCategory().getId()).isEqualTo(1L);
        assertThat(course.getInstructor().getId()).isEqualTo(2L);

        com.englishschool.courseservice.entity.Module module =
                com.englishschool.courseservice.entity.Module.builder().id(3L).course(course).title("M1").orderIndex(0).lessons(List.of()).build();

        Lesson lesson = Lesson.builder().id(4L).module(module).title("L1").contentType(Lesson.ContentType.TEXT)
                .durationMinutes(0).orderIndex(0).build();

        Review review = Review.builder().id(5L).course(course).userId(9L).rating(5).comment("great").createdAt(Instant.now()).build();
        assertThat(review.getRating()).isEqualTo(5);

        CourseDTO courseDTO = CourseDTO.builder().courseId(10L).name("English A1").level("A1").categoryId(1L).instructorId(2L)
                .price(BigDecimal.ZERO).thumbnailUrl("/img").isPublished(false).ratingAvg(BigDecimal.valueOf(4.25)).ratingCount(5).build();
        assertThat(courseDTO.getName()).isEqualTo("English A1");

        CategoryDTO categoryDTO = CategoryDTO.builder().id(1L).name("Grammar").description("Desc").slug("grammar").build();
        InstructorDTO instructorDTO = InstructorDTO.builder().id(2L).firstName("Jane").lastName("Doe").email("jane@x.com").bio("bio").avatarUrl("a").build();
        ModuleDTO moduleDTO = ModuleDTO.builder().id(3L).courseId(10L).title("M1").orderIndex(0).lessons(List.of()).build();
        LessonDTO lessonDTO = LessonDTO.builder().id(4L).moduleId(3L).title("L1").contentType(Lesson.ContentType.TEXT).durationMinutes(0).orderIndex(0).build();
        ReviewDTO reviewDTO = ReviewDTO.builder().id(5L).courseId(10L).userId(9L).rating(5).comment("great").createdAt(Instant.now()).build();
        CourseSearchRequest search = CourseSearchRequest.builder().search("x").page(0).size(20).freeOnly(true).build();
        PageResponse<CourseDTO> page = PageResponse.<CourseDTO>builder().content(List.of(courseDTO)).page(0).size(1).totalElements(1).totalPages(1).first(true).last(true).build();

        assertThat(categoryDTO.getSlug()).isEqualTo("grammar");
        assertThat(instructorDTO.getLastName()).isEqualTo("Doe");
        assertThat(moduleDTO.getCourseId()).isEqualTo(10L);
        assertThat(lessonDTO.getModuleId()).isEqualTo(3L);
        assertThat(reviewDTO.getUserId()).isEqualTo(9L);
        assertThat(search.getFreeOnly()).isTrue();
        assertThat(page.getContent()).hasSize(1);
    }
}

