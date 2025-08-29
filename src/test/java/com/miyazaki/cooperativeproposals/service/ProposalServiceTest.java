package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.entity.Proposal;
import com.miyazaki.cooperativeproposals.entity.VotingSession;
import com.miyazaki.cooperativeproposals.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.exception.SessionOpenedException;
import com.miyazaki.cooperativeproposals.mapper.ProposalMapper;
import com.miyazaki.cooperativeproposals.mapper.VotingSessionMapper;
import com.miyazaki.cooperativeproposals.repository.ProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private VotingSessionService votingSessionService;

    @Mock
    private VotingSessionMapper votingSessionMapper;

    @Mock
    private ProposalMapper proposalMapper;

    @InjectMocks
    private ProposalService proposalService;

    private static final Long DEFAULT_DURATION = 60L;

    @Test
    void create_ShouldMapAndSaveProposal_WhenValidRequestProvided() {
        CreateProposalRequest request = new CreateProposalRequest("Test Title", "Test Description");
        Proposal mappedProposal = Proposal.builder()
                .title("Test Title")
                .description("Test Description")
                .build();

        when(proposalMapper.toEntity(request)).thenReturn(mappedProposal);
        when(proposalRepository.save(any(Proposal.class))).thenReturn(mappedProposal);

        proposalService.create(request);

        verify(proposalMapper, times(1)).toEntity(request);
        verify(proposalRepository, times(1)).save(mappedProposal);
    }

    @Test
    void create_ShouldCallRepositorySave_WhenMapperReturnsEntity() {
        CreateProposalRequest request = new CreateProposalRequest("Another Title", "Another Description");
        Proposal proposal = Proposal.builder()
                .title("Another Title")
                .description("Another Description")
                .build();

        when(proposalMapper.toEntity(request)).thenReturn(proposal);

        proposalService.create(request);

        verify(proposalRepository).save(proposal);
    }

    @Test
    void openVotingSession_ShouldReturnSessionResponse_WhenValidProposalAndRequestProvided() {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(120);
        final Proposal proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final VotingSession session = VotingSession.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(120))
                .status(SessionStatus.OPENED)
                .build();
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(session.getId())
                .openedAt(session.getOpenedAt())
                .closesAt(session.getClosesAt())
                .status(SessionStatus.OPENED)
                .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(false);
        when(votingSessionService.create(proposal, 120)).thenReturn(session);
        when(votingSessionMapper.toSessionResponse(session)).thenReturn(expectedResponse);

        final SessionResponse result = proposalService.openVotingSession(proposalId, request);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getStatus(), result.getStatus());

        verify(proposalRepository, times(1)).findById(proposalId);
        verify(votingSessionService, times(1)).hasVotingSessionOpened(proposalId);
        verify(votingSessionService, times(1)).create(proposal, 120);
        verify(votingSessionService, times(1)).schedulerSessionClosure(session.getId(), 120L);
        verify(votingSessionMapper, times(1)).toSessionResponse(session);
    }

    @Test
    void openVotingSession_ShouldUseDefaultDuration_WhenRequestIsNull() {
        final UUID proposalId = UUID.randomUUID();
        final Proposal proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final VotingSession session = VotingSession.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(60))
                .status(SessionStatus.OPENED)
                .build();
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(session.getId())
                .openedAt(session.getOpenedAt())
                .closesAt(session.getClosesAt())
                .status(SessionStatus.OPENED)
                .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(false);
        when(votingSessionService.create(proposal, 60)).thenReturn(session);
        when(votingSessionMapper.toSessionResponse(session)).thenReturn(expectedResponse);

        final SessionResponse result = proposalService.openVotingSession(proposalId, null);

        assertNotNull(result);
        verify(votingSessionService, times(1)).create(proposal, DEFAULT_DURATION.intValue()); 
        verify(votingSessionService, times(1)).schedulerSessionClosure(session.getId(), DEFAULT_DURATION);
    }

    @Test
    void openVotingSession_ShouldUseDefaultDuration_WhenRequestDurationIsNull() {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(null);
        final Proposal proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final VotingSession session = VotingSession.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .status(SessionStatus.OPENED)
                .build();
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(session.getId())
                .status(SessionStatus.OPENED)
                .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(false);
        when(votingSessionService.create(proposal, 60)).thenReturn(session);
        when(votingSessionMapper.toSessionResponse(session)).thenReturn(expectedResponse);

        final SessionResponse result = proposalService.openVotingSession(proposalId, request);

        assertNotNull(result);
        verify(votingSessionService, times(1)).create(proposal, DEFAULT_DURATION.intValue()); 
        verify(votingSessionService, times(1)).schedulerSessionClosure(session.getId(), DEFAULT_DURATION);
    }

    @Test
    void openVotingSession_ShouldThrowNotFoundException_WhenProposalNotFound() {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(DEFAULT_DURATION.intValue());

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            proposalService.openVotingSession(proposalId, request);
        });

        assertEquals("Proposal not found!", exception.getMessage());
        verify(proposalRepository, times(1)).findById(proposalId);
    }

    @Test
    void openVotingSession_ShouldThrowSessionOpenedException_WhenSessionAlreadyOpened() {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(DEFAULT_DURATION.intValue());
        final Proposal proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(true);

        final SessionOpenedException exception = assertThrows(SessionOpenedException.class, () -> {
            proposalService.openVotingSession(proposalId, request);
        });

        assertEquals("Session voting to proposal already opened", exception.getMessage());
        verify(proposalRepository, times(1)).findById(proposalId);
        verify(votingSessionService, times(1)).hasVotingSessionOpened(proposalId);
        verify(votingSessionService, never()).create(any(), any());
        verify(votingSessionService, never()).schedulerSessionClosure(any(), any());
        verify(votingSessionMapper, never()).toSessionResponse(any());
    }

    @Test
    void getProposal_ShouldReturnProposal_WhenProposalExists() {
        final UUID proposalId = UUID.randomUUID();
        final Proposal expectedProposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(expectedProposal));

        final Proposal result = proposalService.getProposal(proposalId);

        assertNotNull(result);
        assertEquals(expectedProposal.getId(), result.getId());
        assertEquals(expectedProposal.getTitle(), result.getTitle());
        assertEquals(expectedProposal.getDescription(), result.getDescription());
        verify(proposalRepository, times(1)).findById(proposalId);
    }

    @Test
    void getProposal_ShouldThrowNotFoundException_WhenProposalDoesNotExist() {
        final UUID proposalId = UUID.randomUUID();

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            proposalService.getProposal(proposalId);
        });

        assertEquals("Proposal not found!", exception.getMessage());
        verify(proposalRepository, times(1)).findById(proposalId);
    }

    @Test
    void getProposal_ShouldHandleNullId_WhenNullIdProvided() {
        when(proposalRepository.findById(null)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            proposalService.getProposal(null);
        });

        assertEquals("Proposal not found!", exception.getMessage());
        verify(proposalRepository, times(1)).findById(null);
    }

}
