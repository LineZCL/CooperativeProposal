package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.PagedResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalStatusEnum;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalSummary;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.exception.SessionOpenedException;
import com.miyazaki.cooperativeproposals.domain.mapper.ProposalMapper;
import com.miyazaki.cooperativeproposals.domain.mapper.VotingSessionMapper;
import com.miyazaki.cooperativeproposals.domain.repository.ProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void getAllProposals_ShouldReturnPagedResponse_WhenProposalsExist() {
        final Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        final List<Proposal> proposals = Arrays.asList(
                createProposalWithoutSession(UUID.randomUUID(), "Proposal 1", "Description 1"),
                createProposalWithOpenSession(UUID.randomUUID(), "Proposal 2", "Description 2"),
                createProposalWithClosedSession(UUID.randomUUID(), "Proposal 3", "Description 3")
        );
        final Page<Proposal> proposalPage = new PageImpl<>(proposals, pageable, 3);

        when(proposalRepository.findAll(pageable)).thenReturn(proposalPage);

        final PagedResponse<ProposalSummary> result = proposalService.getAllProposals(pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        final ProposalSummary summary1 = result.getContent().get(0);
        assertEquals("Proposal 1", summary1.getTitle());
        assertEquals(ProposalStatusEnum.WAITING, summary1.getStatus());

        final ProposalSummary summary2 = result.getContent().get(1);
        assertEquals("Proposal 2", summary2.getTitle());
        assertEquals(ProposalStatusEnum.OPENED, summary2.getStatus());

        final ProposalSummary summary3 = result.getContent().get(2);
        assertEquals("Proposal 3", summary3.getTitle());
        assertEquals(ProposalStatusEnum.CLOSED, summary3.getStatus());

        verify(proposalRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllProposals_ShouldReturnEmptyPagedResponse_WhenNoProposalsExist() {
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Proposal> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(proposalRepository.findAll(pageable)).thenReturn(emptyPage);

        final PagedResponse<ProposalSummary> result = proposalService.getAllProposals(pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(proposalRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllProposals_ShouldReturnCorrectPageInfo_WhenRequestingSecondPage() {
        final Pageable pageable = PageRequest.of(1, 2);
        final List<Proposal> proposals = Arrays.asList(
                createProposalWithoutSession(UUID.randomUUID(), "Proposal 3", "Description 3"),
                createProposalWithoutSession(UUID.randomUUID(), "Proposal 4", "Description 4")
        );
        final Page<Proposal> proposalPage = new PageImpl<>(proposals, pageable, 5);

        when(proposalRepository.findAll(pageable)).thenReturn(proposalPage);

        final PagedResponse<ProposalSummary> result = proposalService.getAllProposals(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());

        verify(proposalRepository, times(1)).findAll(pageable);
    }

    private Proposal createProposalWithoutSession(UUID id, String title, String description) {
        return Proposal.builder()
                .id(id)
                .title(title)
                .description(description)
                .votingSession(null)
                .build();
    }

    private Proposal createProposalWithOpenSession(UUID id, String title, String description) {
        final VotingSession session = VotingSession.builder()
                .id(UUID.randomUUID())
                .status(SessionStatus.OPENED)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusMinutes(60))
                .build();

        return Proposal.builder()
                .id(id)
                .title(title)
                .description(description)
                .votingSession(session)
                .build();
    }

    private Proposal createProposalWithClosedSession(UUID id, String title, String description) {
        final VotingSession session = VotingSession.builder()
                .id(UUID.randomUUID())
                .status(SessionStatus.CLOSED)
                .openedAt(LocalDateTime.now().minusHours(1))
                .closesAt(LocalDateTime.now().minusMinutes(30))
                .build();

        return Proposal.builder()
                .id(id)
                .title(title)
                .description(description)
                .votingSession(session)
                .build();
    }

}
