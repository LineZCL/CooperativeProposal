package com.miyazaki.cooperativeproposals.controller.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void builder_ShouldCreatePagedResponse_WhenAllFieldsProvided() {
        // Arrange
        final List<String> content = Arrays.asList("item1", "item2");
        final int page = 1;
        final int size = 10;
        final long totalElements = 25;
        final int totalPages = 3;

        // Act
        final PagedResponse<String> response = PagedResponse.<String>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
    }

    @Test
    void builder_ShouldCreatePagedResponse_WhenEmptyContent() {
        // Arrange
        final List<String> emptyContent = Collections.emptyList();

        // Act
        final PagedResponse<String> response = PagedResponse.<String>builder()
                .content(emptyContent)
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .build();

        // Assert
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyPagedResponse() {
        // Act
        final PagedResponse<String> response = new PagedResponse<>();

        // Assert
        assertNotNull(response);
        assertNull(response.getContent());
        assertEquals(0, response.getPage());
        assertEquals(0, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }

    @Test
    void allArgsConstructor_ShouldCreatePagedResponse_WhenAllArgumentsProvided() {
        // Arrange
        final List<String> content = Arrays.asList("test1", "test2");
        final int page = 2;
        final int size = 5;
        final long totalElements = 50;
        final int totalPages = 10;

        // Act
        final PagedResponse<String> response = new PagedResponse<>(
                content, page, size, totalElements, totalPages
        );

        // Assert
        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(page, response.getPage());
        assertEquals(size, response.getSize());
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        final PagedResponse<String> response = new PagedResponse<>();
        final List<String> content = Arrays.asList("item");

        // Act
        response.setContent(content);
        response.setPage(1);
        response.setSize(20);
        response.setTotalElements(100);
        response.setTotalPages(5);

        // Assert
        assertEquals(content, response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(100, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
    }

    @Test
    void equals_ShouldReturnTrue_WhenObjectsAreEqual() {
        // Arrange
        final List<String> content = Arrays.asList("test");
        final PagedResponse<String> response1 = PagedResponse.<String>builder()
                .content(content)
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        final PagedResponse<String> response2 = PagedResponse.<String>builder()
                .content(content)
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        // Act & Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_WhenObjectsAreDifferent() {
        // Arrange
        final PagedResponse<String> response1 = PagedResponse.<String>builder()
                .content(Arrays.asList("test1"))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        final PagedResponse<String> response2 = PagedResponse.<String>builder()
                .content(Arrays.asList("test2"))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Arrange
        final PagedResponse<String> response = PagedResponse.<String>builder()
                .content(Arrays.asList("test"))
                .page(1)
                .size(5)
                .totalElements(10)
                .totalPages(2)
                .build();

        // Act
        final String toString = response.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("content"));
        assertTrue(toString.contains("page"));
        assertTrue(toString.contains("size"));
        assertTrue(toString.contains("totalElements"));
        assertTrue(toString.contains("totalPages"));
    }
}
