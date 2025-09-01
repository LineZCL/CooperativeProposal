package com.miyazaki.cooperativeproposals.controller;

import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.FormField;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileFormScreen;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileScreenResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileSelectionScreen;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.service.MobileScreenService;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import com.miyazaki.cooperativeproposals.service.VotingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileControllerTest {

    @Mock
    private MobileScreenService mobileScreenService;

    @Mock
    private ProposalService proposalService;

    @Mock
    private VotingSessionService votingSessionService;

    @InjectMocks
    private MobileController mobileController;

    private UUID proposalId;
    private UUID sessionId;
    private Proposal mockProposal;
    private VotingSession mockSession;

    @BeforeEach
    void setUp() {
        proposalId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        mockProposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();
        mockSession = VotingSession.builder()
                .id(sessionId)
                .proposal(mockProposal)
                .build();
    }

    @Test
    void getProposalsList_ShouldReturnOk_WhenDefaultParametersUsed() {
        final List<Proposal> proposals = Arrays.asList(mockProposal);
        final Page<Proposal> proposalPage = new PageImpl<>(proposals, PageRequest.of(0, 20), 1);
        final MobileSelectionScreen expectedScreen = MobileSelectionScreen.builder()
                .titulo("Selecione uma Pauta")
                .descricao("Escolha uma pauta para visualizar ou votar")
                .build();

        when(proposalService.getAllProposalsPage(any(PageRequest.class))).thenReturn(proposalPage);
        when(mobileScreenService.createProposalList(proposals)).thenReturn(expectedScreen);

        final ResponseEntity<MobileSelectionScreen> response = mobileController.getProposalsList(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedScreen, response.getBody());
        
        verify(proposalService, times(1)).getAllProposalsPage(PageRequest.of(0, 20));
        verify(mobileScreenService, times(1)).createProposalList(proposals);
    }

    @Test
    void getProposalsList_ShouldReturnOk_WhenCustomParametersProvided() {
        final List<Proposal> proposals = Arrays.asList(mockProposal);
        final Page<Proposal> proposalPage = new PageImpl<>(proposals, PageRequest.of(1, 10), 1);
        final MobileSelectionScreen expectedScreen = MobileSelectionScreen.builder()
                .titulo("Selecione uma Pauta")
                .descricao("Escolha uma pauta para visualizar ou votar")
                .build();

        when(proposalService.getAllProposalsPage(any(PageRequest.class))).thenReturn(proposalPage);
        when(mobileScreenService.createProposalList(proposals)).thenReturn(expectedScreen);

        final ResponseEntity<MobileSelectionScreen> response = mobileController.getProposalsList(1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedScreen, response.getBody());
        
        verify(proposalService, times(1)).getAllProposalsPage(PageRequest.of(1, 10));
        verify(mobileScreenService, times(1)).createProposalList(proposals);
    }

    @Test
    void getProposalDetails_ShouldReturnOk_WhenProposalExistsAndVotingSessionIsActive() {
        final MobileSelectionScreen expectedScreen = MobileSelectionScreen.builder()
                .titulo("Votar em: Test Proposal")
                .descricao("Test Description")
                .build();

        when(proposalService.getProposal(proposalId)).thenReturn(mockProposal);
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(true);
        when(votingSessionService.getSessionActiveByProposalId(proposalId)).thenReturn(mockSession);
        when(mobileScreenService.createVotingOptions(mockProposal, mockSession)).thenReturn(expectedScreen);

        final ResponseEntity<MobileScreenResponse> response = mobileController.getProposalDetails(proposalId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedScreen, response.getBody());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, times(1)).hasVotingSessionOpened(proposalId);
        verify(votingSessionService, times(1)).getSessionActiveByProposalId(proposalId);
        verify(mobileScreenService, times(1)).createVotingOptions(mockProposal, mockSession);
    }

    @Test
    void getProposalDetails_ShouldThrowNotFoundException_WhenNoActiveVotingSession() {
        when(proposalService.getProposal(proposalId)).thenReturn(mockProposal);
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(false);

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            mobileController.getProposalDetails(proposalId);
        });

        assertEquals("No active voting session for this proposal", exception.getMessage());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, times(1)).hasVotingSessionOpened(proposalId);
        verify(votingSessionService, never()).getSessionActiveByProposalId(any());
        verify(mobileScreenService, never()).createVotingOptions(any(), any());
    }

    @Test
    void getProposalDetails_ShouldPropagateNotFoundException_WhenProposalNotFound() {
        final NotFoundException expectedException = new NotFoundException("Proposal not found!");
        when(proposalService.getProposal(proposalId)).thenThrow(expectedException);

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            mobileController.getProposalDetails(proposalId);
        });

        assertEquals("Proposal not found!", exception.getMessage());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, never()).hasVotingSessionOpened(any());
    }

    @Test
    void getVoteForm_ShouldReturnOk_WithTrueVoteChoice() {
        final Boolean voteChoice = true;
        final MobileFormScreen expectedScreen = MobileFormScreen.builder()
                .titulo("Votação - Test Proposal")
                .descricao("Test Description")
                .itens(new ArrayList<>())
                .build();

        when(proposalService.getProposal(proposalId)).thenReturn(mockProposal);
        when(votingSessionService.getSessionActiveByProposalId(proposalId)).thenReturn(mockSession);
        when(mobileScreenService.createVotingForm(mockProposal, mockSession)).thenReturn(expectedScreen);

        final ResponseEntity<MobileFormScreen> response = mobileController.getVoteForm(proposalId, voteChoice);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedScreen, response.getBody());
        
        final List<FormField> formFields = response.getBody().getItens();
        final FormField voteField = formFields.stream()
                .filter(field -> "vote".equals(field.getId()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(voteField, "Vote field should be added");
        assertEquals("HIDDEN", voteField.getTipo());
        assertEquals(true, voteField.getValor());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, times(1)).getSessionActiveByProposalId(proposalId);
        verify(mobileScreenService, times(1)).createVotingForm(mockProposal, mockSession);
    }

    @Test
    void getVoteForm_ShouldReturnOk_WithFalseVoteChoice() {
        final Boolean voteChoice = false;
        final MobileFormScreen expectedScreen = MobileFormScreen.builder()
                .titulo("Votação - Test Proposal")
                .descricao("Test Description")
                .itens(new ArrayList<>())
                .build();

        when(proposalService.getProposal(proposalId)).thenReturn(mockProposal);
        when(votingSessionService.getSessionActiveByProposalId(proposalId)).thenReturn(mockSession);
        when(mobileScreenService.createVotingForm(mockProposal, mockSession)).thenReturn(expectedScreen);

        final ResponseEntity<MobileFormScreen> response = mobileController.getVoteForm(proposalId, voteChoice);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        final List<FormField> formFields = response.getBody().getItens();
        final FormField voteField = formFields.stream()
                .filter(field -> "vote".equals(field.getId()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(voteField, "Vote field should be added");
        assertEquals("HIDDEN", voteField.getTipo());
        assertEquals(false, voteField.getValor());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, times(1)).getSessionActiveByProposalId(proposalId);
        verify(mobileScreenService, times(1)).createVotingForm(mockProposal, mockSession);
    }

    @Test
    void getVoteForm_ShouldPropagateNotFoundException_WhenProposalNotFound() {
        final Boolean voteChoice = true;
        final NotFoundException expectedException = new NotFoundException("Proposal not found!");
        when(proposalService.getProposal(proposalId)).thenThrow(expectedException);

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            mobileController.getVoteForm(proposalId, voteChoice);
        });

        assertEquals("Proposal not found!", exception.getMessage());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, never()).getSessionActiveByProposalId(any());
        verify(mobileScreenService, never()).createVotingForm(any(), any());
    }

    @Test
    void getNewProposalForm_ShouldReturnOk() {
        final MobileFormScreen expectedScreen = MobileFormScreen.builder()
                .titulo("Nova Pauta")
                .descricao("Criar uma nova pauta para votação")
                .build();

        when(mobileScreenService.createProposalForm()).thenReturn(expectedScreen);

        final ResponseEntity<MobileFormScreen> response = mobileController.getNewProposalForm();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedScreen, response.getBody());
        
        verify(mobileScreenService, times(1)).createProposalForm();
    }

    @Test
    void getProposalsList_ShouldHandleEmptyProposalsList() {
        final List<Proposal> emptyProposals = Arrays.asList();
        final Page<Proposal> emptyPage = new PageImpl<>(emptyProposals, PageRequest.of(0, 20), 0);
        final MobileSelectionScreen expectedScreen = MobileSelectionScreen.builder()
                .titulo("Selecione uma Pauta")
                .descricao("Escolha uma pauta para visualizar ou votar")
                .build();

        when(proposalService.getAllProposalsPage(any(PageRequest.class))).thenReturn(emptyPage);
        when(mobileScreenService.createProposalList(emptyProposals)).thenReturn(expectedScreen);

        final ResponseEntity<MobileSelectionScreen> response = mobileController.getProposalsList(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedScreen, response.getBody());
        
        verify(proposalService, times(1)).getAllProposalsPage(PageRequest.of(0, 20));
        verify(mobileScreenService, times(1)).createProposalList(emptyProposals);
    }

    @Test
    void getVoteForm_ShouldPropagateException_WhenSessionServiceFails() {
        final Boolean voteChoice = true;
        final RuntimeException expectedException = new RuntimeException("Session service error");
        
        when(proposalService.getProposal(proposalId)).thenReturn(mockProposal);
        when(votingSessionService.getSessionActiveByProposalId(proposalId)).thenThrow(expectedException);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mobileController.getVoteForm(proposalId, voteChoice);
        });

        assertEquals("Session service error", exception.getMessage());
        
        verify(proposalService, times(1)).getProposal(proposalId);
        verify(votingSessionService, times(1)).getSessionActiveByProposalId(proposalId);
        verify(mobileScreenService, never()).createVotingForm(any(), any());
    }
}
