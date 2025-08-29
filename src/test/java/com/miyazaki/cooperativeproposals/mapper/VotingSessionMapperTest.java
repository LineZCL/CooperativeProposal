package com.miyazaki.cooperativeproposals.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.domain.mapper.VotingSessionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VotingSessionMapperTest {

    @Autowired
    private VotingSessionMapper votingSessionMapper;

    @Test
    void toSessionResponse_ShouldMapAllFields_WhenValidVotingSessionProvided() {
        UUID sessionId = UUID.randomUUID();
        LocalDateTime openedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime closesAt = LocalDateTime.of(2024, 1, 15, 11, 30, 0);
        
        VotingSession votingSession = VotingSession.builder()
                .id(sessionId)
                .openedAt(openedAt)
                .closesAt(closesAt)
                .status(SessionStatus.OPENED)
                .build();

        SessionResponse result = votingSessionMapper.toSessionResponse(votingSession);

        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        assertEquals(openedAt, result.getOpenedAt());
        assertEquals(closesAt, result.getClosesAt());
        assertEquals(SessionStatus.OPENED, result.getStatus());
    }

    @Test
    void toSessionResponse_ShouldReturnNull_WhenVotingSessionIsNull() {
        SessionResponse result = votingSessionMapper.toSessionResponse(null);

        assertNull(result);
    }

    @Test
    void toSessionResponse_ShouldHandleNullDates_WhenDatesAreNull() {
        // Given
        UUID sessionId = UUID.randomUUID();
        

        VotingSession votingSession = VotingSession.builder()
                .id(sessionId)
                .openedAt(null)
                .closesAt(null)
                .status(SessionStatus.OPENED)
                .build();

        SessionResponse result = votingSessionMapper.toSessionResponse(votingSession);

        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        assertNull(result.getOpenedAt());
        assertNull(result.getClosesAt());
        assertEquals(SessionStatus.OPENED, result.getStatus());
    }
}
