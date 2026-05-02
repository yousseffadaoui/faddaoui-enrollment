package com.englishschool.courseservice.repository;

import com.englishschool.courseservice.dto.CourseSearchRequest;
import com.englishschool.courseservice.entity.Course;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CourseSpecificationTest {

    @Test
    void search_buildsPredicates_forMostBranches() {
        CourseSearchRequest req = CourseSearchRequest.builder()
                .search("English")
                .level("A1")
                .categoryId(1L)
                .instructorId(2L)
                .isPublished(true)
                .minRating(4.0)
                .freeOnly(true)
                .build();

        @SuppressWarnings({"rawtypes", "unchecked"})
        Root root = mock(Root.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings({"rawtypes", "unchecked"})
        Path namePath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path descPath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path levelPath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path categoryPath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path instructorPath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path isPublishedPath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path ratingAvgPath = mock(Path.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path pricePath = mock(Path.class);

        when(root.get("name")).thenReturn(namePath);
        when(root.get("description")).thenReturn(descPath);
        when(root.get("level")).thenReturn(levelPath);
        when(root.get("category")).thenReturn(categoryPath);
        when(root.get("instructor")).thenReturn(instructorPath);
        when(root.get("isPublished")).thenReturn(isPublishedPath);
        when(root.get("ratingAvg")).thenReturn(ratingAvgPath);
        when(root.get("price")).thenReturn(pricePath);

        @SuppressWarnings({"rawtypes", "unchecked"})
        Path categoryIdPath = mock(Path.class);
        when(categoryPath.get("id")).thenReturn(categoryIdPath);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Path instructorIdPath = mock(Path.class);
        when(instructorPath.get("id")).thenReturn(instructorIdPath);

        Predicate pLike1 = mock(Predicate.class);
        Predicate pLike2 = mock(Predicate.class);
        Predicate pOrSearch = mock(Predicate.class);
        Predicate pLevel = mock(Predicate.class);
        Predicate pCat = mock(Predicate.class);
        Predicate pInst = mock(Predicate.class);
        Predicate pPub = mock(Predicate.class);
        Predicate pMinRating = mock(Predicate.class);
        Predicate pPriceNull = mock(Predicate.class);
        Predicate pPriceZero = mock(Predicate.class);
        Predicate pFreeOr = mock(Predicate.class);
        Predicate pAnd = mock(Predicate.class);

        @SuppressWarnings({"rawtypes", "unchecked"})
        Expression lowerName = mock(Expression.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Expression lowerDesc = mock(Expression.class);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.lower(descPath)).thenReturn(lowerDesc);
        when(cb.like(eq(lowerName), anyString())).thenReturn(pLike1);
        when(cb.like(eq(lowerDesc), anyString())).thenReturn(pLike2);
        when(cb.or(pLike1, pLike2)).thenReturn(pOrSearch);

        when(cb.equal(levelPath, "A1")).thenReturn(pLevel);
        when(cb.equal(categoryIdPath, 1L)).thenReturn(pCat);
        when(cb.equal(instructorIdPath, 2L)).thenReturn(pInst);
        when(cb.equal(isPublishedPath, true)).thenReturn(pPub);
        when(cb.greaterThanOrEqualTo(ratingAvgPath, 4.0)).thenReturn(pMinRating);

        when(cb.isNull(pricePath)).thenReturn(pPriceNull);
        when(cb.equal(pricePath, 0)).thenReturn(pPriceZero);
        when(cb.or(pPriceNull, pPriceZero)).thenReturn(pFreeOr);

        when(cb.and(any(Predicate[].class))).thenReturn(pAnd);

        @SuppressWarnings("unchecked")
        Predicate result = CourseSpecification.search(req).toPredicate(root, query, cb);

        assertThat(result).isSameAs(pAnd);
        verify(cb).and(any(Predicate[].class));
        verify(cb).or(pLike1, pLike2);
        verify(cb).or(pPriceNull, pPriceZero);
    }

    @Test
    void search_whenFreeOnlyFalse_usesGreaterThanPrice() {
        CourseSearchRequest req = CourseSearchRequest.builder()
                .freeOnly(false)
                .build();

        @SuppressWarnings({"rawtypes", "unchecked"})
        Root root = mock(Root.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings({"rawtypes", "unchecked"})
        Path pricePath = mock(Path.class);
        when(root.get("price")).thenReturn(pricePath);

        Predicate gt = mock(Predicate.class);
        when(cb.greaterThan(pricePath, 0)).thenReturn(gt);
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        CourseSpecification.search(req).toPredicate(root, query, cb);

        verify(cb).greaterThan(pricePath, 0);
    }
}

