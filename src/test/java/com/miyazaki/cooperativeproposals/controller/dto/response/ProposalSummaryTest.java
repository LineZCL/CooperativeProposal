package com.miyazaki.cooperativeproposals.controller.dto.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProposalSummaryTest {

    @Test
    void builder_ShouldCreateProposalSummary_WhenAllFieldsProvided() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final String title = "Test Proposal";
        final String description = "Test Description";
        final ProposalStatusEnum status = ProposalStatusEnum.WAITING;

        // Act
        final ProposalSummary summary = ProposalSummary.builder()
                .id(id)
                .title(title)
                .description(description)
                .status(status)
                .build();

        // Assert
        assertNotNull(summary);
        assertEquals(id, summary.getId());
        assertEquals(title, summary.getTitle());
        assertEquals(description, summary.getDescription());
        assertEquals(status, summary.getStatus());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyProposalSummary() {
        // Act
        final ProposalSummary summary = new ProposalSummary();

        // Assert
        assertNotNull(summary);
        assertNull(summary.getId());
        assertNull(summary.getTitle());
        assertNull(summary.getDescription());
        assertNull(summary.getStatus());
    }

    @Test
    void allArgsConstructor_ShouldCreateProposalSummary_WhenAllArgumentsProvided() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final String title = "Constructor Test";
        final String description = "Constructor Description";
        final ProposalStatusEnum status = ProposalStatusEnum.OPENED;

        // Act
        final ProposalSummary summary = new ProposalSummary(id, title, description, status);

        // Assert
        assertNotNull(summary);
        assertEquals(id, summary.getId());
        assertEquals(title, summary.getTitle());
        assertEquals(description, summary.getDescription());
        assertEquals(status, summary.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        final ProposalSummary summary = new ProposalSummary();
        final UUID id = UUID.randomUUID();
        final String title = "Setter Test";
        final String description = "Setter Description";
        final ProposalStatusEnum status = ProposalStatusEnum.CLOSED;

        // Act
        summary.setId(id);
        summary.setTitle(title);
        summary.setDescription(description);
        summary.setStatus(status);

        // Assert
        assertEquals(id, summary.getId());
        assertEquals(title, summary.getTitle());
        assertEquals(description, summary.getDescription());
        assertEquals(status, summary.getStatus());
    }

    @Test
    void builder_ShouldCreateProposalSummary_WithWaitingStatus() {
        // Arrange & Act
        final ProposalSummary summary = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Waiting Proposal")
                .description("Waiting Description")
                .status(ProposalStatusEnum.WAITING)
                .build();

        // Assert
        assertEquals(ProposalStatusEnum.WAITING, summary.getStatus());
    }

    @Test
    void builder_ShouldCreateProposalSummary_WithOpenedStatus() {
        // Arrange & Act
        final ProposalSummary summary = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Opened Proposal")
                .description("Opened Description")
                .status(ProposalStatusEnum.OPENED)
                .build();

        // Assert
        assertEquals(ProposalStatusEnum.OPENED, summary.getStatus());
    }

    @Test
    void builder_ShouldCreateProposalSummary_WithClosedStatus() {
        // Arrange & Act
        final ProposalSummary summary = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Closed Proposal")
                .description("Closed Description")
                .status(ProposalStatusEnum.CLOSED)
                .build();

        // Assert
        assertEquals(ProposalStatusEnum.CLOSED, summary.getStatus());
    }

    @Test
    void equals_ShouldReturnTrue_WhenObjectsAreEqual() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final ProposalSummary summary1 = ProposalSummary.builder()
                .id(id)
                .title("Test")
                .description("Description")
                .status(ProposalStatusEnum.WAITING)
                .build();

        final ProposalSummary summary2 = ProposalSummary.builder()
                .id(id)
                .title("Test")
                .description("Description")
                .status(ProposalStatusEnum.WAITING)
                .build();

        // Act & Assert
        assertEquals(summary1, summary2);
        assertEquals(summary1.hashCode(), summary2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_WhenObjectsAreDifferent() {
        // Arrange
        final ProposalSummary summary1 = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Test1")
                .description("Description1")
                .status(ProposalStatusEnum.WAITING)
                .build();

        final ProposalSummary summary2 = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Test2")
                .description("Description2")
                .status(ProposalStatusEnum.OPENED)
                .build();

        // Act & Assert
        assertNotEquals(summary1, summary2);
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final ProposalSummary summary = ProposalSummary.builder()
                .id(id)
                .title("ToString Test")
                .description("ToString Description")
                .status(ProposalStatusEnum.WAITING)
                .build();

        // Act
        final String toString = summary.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("id"));
        assertTrue(toString.contains("title"));
        assertTrue(toString.contains("description"));
        assertTrue(toString.contains("status"));
    }

    @Test
    void builder_ShouldHandleNullValues() {
        // Act
        final ProposalSummary summary = ProposalSummary.builder()
                .id(null)
                .title(null)
                .description(null)
                .status(null)
                .build();

        // Assert
        assertNotNull(summary);
        assertNull(summary.getId());
        assertNull(summary.getTitle());
        assertNull(summary.getDescription());
        assertNull(summary.getStatus());
    }

    @Test
    void builder_ShouldHandleEmptyStrings() {
        // Arrange
        final UUID id = UUID.randomUUID();

        // Act
        final ProposalSummary summary = ProposalSummary.builder()
                .id(id)
                .title("")
                .description("")
                .status(ProposalStatusEnum.WAITING)
                .build();

        // Assert
        assertEquals(id, summary.getId());
        assertEquals("", summary.getTitle());
        assertEquals("", summary.getDescription());
        assertEquals(ProposalStatusEnum.WAITING, summary.getStatus());
    }
}
