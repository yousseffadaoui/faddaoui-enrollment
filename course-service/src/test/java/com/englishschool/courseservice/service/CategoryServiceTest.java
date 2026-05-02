package com.englishschool.courseservice.service;

import com.englishschool.courseservice.dto.CategoryDTO;
import com.englishschool.courseservice.entity.Category;
import com.englishschool.courseservice.exception.ResourceNotFoundException;
import com.englishschool.courseservice.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    @Test
    void create_whenSlugNull_generatesSlugFromName() {
        CategoryDTO in = CategoryDTO.builder()
                .name("Business English")
                .description("Professional English")
                .slug(null)
                .build();

        when(repository.save(categoryCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        CategoryDTO out = service.create(in);

        Category saved = categoryCaptor.getValue();
        assertThat(saved.getSlug()).isEqualTo("business-english");
        assertThat(out.getSlug()).isEqualTo("business-english");
        verify(repository).save(any(Category.class));
    }

    @Test
    void create_whenSlugProvided_usesIt() {
        CategoryDTO in = CategoryDTO.builder()
                .name("Grammar")
                .slug("grammar-custom")
                .build();

        when(repository.save(categoryCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        CategoryDTO out = service.create(in);

        assertThat(categoryCaptor.getValue().getSlug()).isEqualTo("grammar-custom");
        assertThat(out.getSlug()).isEqualTo("grammar-custom");
    }

    @Test
    void getAll_mapsEntities() {
        when(repository.findAll()).thenReturn(List.of(
                Category.builder().id(1L).name("A").slug("a").build(),
                Category.builder().id(2L).name("B").slug("b").build()
        ));

        List<CategoryDTO> res = service.getAll();

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getId()).isEqualTo(1L);
        assertThat(res.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getById_notFound_throws() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void update_whenSlugNull_keepsExistingSlug() {
        Category existing = Category.builder().id(5L).name("Old").slug("keep-me").build();
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        CategoryDTO in = CategoryDTO.builder()
                .name("New")
                .description("Desc")
                .slug(null)
                .build();

        CategoryDTO out = service.update(5L, in);

        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getSlug()).isEqualTo("keep-me");
        assertThat(out.getSlug()).isEqualTo("keep-me");
    }

    @Test
    void delete_whenMissing_throws() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(99L)).thenReturn(true);

        service.delete(99L);

        verify(repository).deleteById(99L);
    }
}

