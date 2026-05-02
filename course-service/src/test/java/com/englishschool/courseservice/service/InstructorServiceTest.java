package com.englishschool.courseservice.service;

import com.englishschool.courseservice.dto.InstructorDTO;
import com.englishschool.courseservice.entity.Instructor;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.mapper.InstructorMapper;
import com.englishschool.courseservice.repository.InstructorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository repository;

    @Mock
    private InstructorMapper mapper;

    @InjectMocks
    private InstructorService service;

    @Captor
    private ArgumentCaptor<Instructor> instructorCaptor;

    @Test
    void create_mapsAndSaves() {
        InstructorDTO in = InstructorDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@school.com")
                .build();
        Instructor entity = new Instructor();
        Instructor saved = Instructor.builder().id(11L).firstName("Jane").lastName("Smith").build();
        InstructorDTO out = InstructorDTO.builder().id(11L).firstName("Jane").lastName("Smith").build();

        when(mapper.toEntity(in)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(out);

        InstructorDTO res = service.create(in);

        assertThat(res.getId()).isEqualTo(11L);
        verify(repository).save(entity);
    }

    @Test
    void getAll_filtersNullDtos() {
        Instructor a = Instructor.builder().id(1L).build();
        Instructor b = Instructor.builder().id(2L).build();
        when(repository.findAll()).thenReturn(List.of(a, b));
        when(mapper.toDTO(a)).thenReturn(InstructorDTO.builder().id(1L).firstName("A").lastName("B").build());
        when(mapper.toDTO(b)).thenReturn(null);

        List<InstructorDTO> res = service.getAll();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getById_notFound_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor not found");
    }

    @Test
    void update_setsFieldsAndSaves() {
        Instructor existing = Instructor.builder().id(5L).firstName("Old").lastName("Name").build();
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(InstructorDTO.builder().id(5L).firstName("New").lastName("Last").build());

        InstructorDTO in = InstructorDTO.builder()
                .firstName("New")
                .lastName("Last")
                .email("new@school.com")
                .bio("Bio")
                .avatarUrl("http://avatar")
                .build();

        InstructorDTO res = service.update(5L, in);

        assertThat(existing.getFirstName()).isEqualTo("New");
        assertThat(existing.getEmail()).isEqualTo("new@school.com");
        assertThat(res.getId()).isEqualTo(5L);
    }

    @Test
    void delete_whenMissing_throws() {
        when(repository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor not found");
    }

    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(repository).deleteById(10L);
    }
}

