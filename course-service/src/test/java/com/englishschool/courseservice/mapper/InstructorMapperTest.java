package com.englishschool.courseservice.mapper;

import com.englishschool.courseservice.dto.InstructorDTO;
import com.englishschool.courseservice.entity.Instructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstructorMapperTest {

    private final InstructorMapper mapper = new InstructorMapper();

    @Test
    void toDTO_null_returnsNull() {
        assertThat(mapper.toDTO(null)).isNull();
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDTO_mapsFields() {
        Instructor e = Instructor.builder()
                .id(5L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@school.com")
                .bio("Bio")
                .avatarUrl("/a.png")
                .build();

        InstructorDTO dto = mapper.toDTO(e);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getFirstName()).isEqualTo("Jane");
        assertThat(dto.getEmail()).isEqualTo("jane@school.com");
    }

    @Test
    void toEntity_mapsFields() {
        InstructorDTO dto = InstructorDTO.builder()
                .id(5L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@school.com")
                .bio("Bio")
                .avatarUrl("/a.png")
                .build();

        Instructor e = mapper.toEntity(dto);

        assertThat(e.getId()).isEqualTo(5L);
        assertThat(e.getLastName()).isEqualTo("Doe");
        assertThat(e.getAvatarUrl()).isEqualTo("/a.png");
    }
}

