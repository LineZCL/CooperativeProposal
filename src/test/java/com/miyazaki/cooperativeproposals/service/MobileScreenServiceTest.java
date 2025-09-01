package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.ActionButton;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.FormField;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileFormScreen;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileSelectionScreen;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.SelectionOption;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MobileScreenServiceTest {

    @InjectMocks
    private MobileScreenService mobileScreenService;

    private UUID proposalId;
    private UUID sessionId;
    private Proposal mockProposal;
    private VotingSession mockSession;

    @BeforeEach
    void setUp() {
        proposalId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        
        ReflectionTestUtils.setField(mobileScreenService, "contextPath", "/api/v1");
        ReflectionTestUtils.setField(mobileScreenService, "baseUrl", "http://localhost:8080");
        
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
    void createVotingForm_ShouldReturnValidMobileFormScreen() {
        final MobileFormScreen result = mobileScreenService.createVotingForm(mockProposal, mockSession);

        assertNotNull(result);
        assertEquals("FORMULARIO", result.getTipo());
        assertEquals("Votação - Test Proposal", result.getTitulo());
        assertEquals("Test Description", result.getDescricao());
        
        final List<FormField> fields = result.getItens();
        assertNotNull(fields);
        assertEquals(3, fields.size());
        
        final FormField associateIdField = fields.get(0);
        assertEquals("associateId", associateIdField.getId());
        assertEquals("ID do Associado", associateIdField.getLabel());
        assertEquals("TEXT", associateIdField.getTipo());
        assertTrue(associateIdField.getObrigatorio());
        assertEquals("Digite seu ID de associado", associateIdField.getPlaceholder());
        
        final FormField cpfField = fields.get(1);
        assertEquals("associateCpf", cpfField.getId());
        assertEquals("CPF", cpfField.getLabel());
        assertEquals("TEXT", cpfField.getTipo());
        assertTrue(cpfField.getObrigatorio());
        assertEquals("000.000.000-00", cpfField.getPlaceholder());
        
        final FormField proposalIdField = fields.get(2);
        assertEquals("proposalId", proposalIdField.getId());
        assertEquals("HIDDEN", proposalIdField.getTipo());
        assertEquals(proposalId.toString(), proposalIdField.getValor());
        
        final List<ActionButton> buttons = result.getBotoes();
        assertNotNull(buttons);
        assertEquals(2, buttons.size());
        
        final ActionButton simButton = buttons.get(0);
        assertEquals("SIM", simButton.getTexto());
        assertEquals("PRIMARY", simButton.getEstilo());
        assertEquals("http://localhost:8080/api/v1/proposal/" + proposalId + "/vote", simButton.getUrl());
        assertEquals("POST", simButton.getMetodo());
        assertNotNull(simButton.getBody());
        assertEquals(true, simButton.getBody().get("vote"));
        
        final ActionButton naoButton = buttons.get(1);
        assertEquals("NÃO", naoButton.getTexto());
        assertEquals("SECONDARY", naoButton.getEstilo());
        assertEquals("http://localhost:8080/api/v1/proposal/" + proposalId + "/vote", naoButton.getUrl());
        assertEquals("POST", naoButton.getMetodo());
        assertNotNull(naoButton.getBody());
        assertEquals(false, naoButton.getBody().get("vote"));
    }

    @Test
    void createProposalList_ShouldReturnValidMobileSelectionScreen() {
        // Given
        final Proposal proposal1 = Proposal.builder()
                .id(UUID.randomUUID())
                .title("Proposal 1")
                .description("Description 1")
                .build();
                
        final Proposal proposal2 = Proposal.builder()
                .id(UUID.randomUUID())
                .title("Proposal 2")
                .description("Description 2")
                .build();
                
        final List<Proposal> proposals = Arrays.asList(proposal1, proposal2);

        final MobileSelectionScreen result = mobileScreenService.createProposalList(proposals);

        assertNotNull(result);
        assertEquals("SELECAO", result.getTipo());
        assertEquals("Selecione uma Pauta", result.getTitulo());
        assertEquals("Escolha uma pauta para visualizar ou votar", result.getDescricao());
        
        final List<SelectionOption> options = result.getOpcoes();
        assertNotNull(options);
        assertEquals(2, options.size());
        
        final SelectionOption option1 = options.get(0);
        assertEquals("Proposal 1", option1.getTexto());
        assertEquals(proposal1.getId().toString(), option1.getValor());
        assertEquals("Description 1", option1.getDescricao());
        assertEquals("http://localhost:8080/api/v1/mobile/proposal/" + proposal1.getId(), option1.getUrl());
        assertEquals("GET", option1.getMetodo());
        
        final SelectionOption option2 = options.get(1);
        assertEquals("Proposal 2", option2.getTexto());
        assertEquals(proposal2.getId().toString(), option2.getValor());
        assertEquals("Description 2", option2.getDescricao());
        assertEquals("http://localhost:8080/api/v1/mobile/proposal/" + proposal2.getId(), option2.getUrl());
        assertEquals("GET", option2.getMetodo());
    }

    @Test
    void createProposalList_ShouldHandleEmptyList() {
        final List<Proposal> emptyProposals = Arrays.asList();

        final MobileSelectionScreen result = mobileScreenService.createProposalList(emptyProposals);

        assertNotNull(result);
        assertEquals("SELECAO", result.getTipo());
        assertEquals("Selecione uma Pauta", result.getTitulo());
        assertEquals("Escolha uma pauta para visualizar ou votar", result.getDescricao());
        
        final List<SelectionOption> options = result.getOpcoes();
        assertNotNull(options);
        assertTrue(options.isEmpty());
    }

    @Test
    void createProposalForm_ShouldReturnValidMobileFormScreen() {
        final MobileFormScreen result = mobileScreenService.createProposalForm();

        assertNotNull(result);
        assertEquals("FORMULARIO", result.getTipo());
        assertEquals("Nova Pauta", result.getTitulo());
        assertEquals("Criar uma nova pauta para votação", result.getDescricao());
        
        final List<FormField> fields = result.getItens();
        assertNotNull(fields);
        assertEquals(2, fields.size());
        
        final FormField titleField = fields.get(0);
        assertEquals("title", titleField.getId());
        assertEquals("Título da Pauta", titleField.getLabel());
        assertEquals("TEXT", titleField.getTipo());
        assertTrue(titleField.getObrigatorio());
        assertEquals("Digite o título da pauta", titleField.getPlaceholder());
        
        final FormField descriptionField = fields.get(1);
        assertEquals("description", descriptionField.getId());
        assertEquals("Descrição", descriptionField.getLabel());
        assertEquals("TEXT", descriptionField.getTipo());
        assertFalse(descriptionField.getObrigatorio());
        assertEquals("Descreva a pauta (opcional)", descriptionField.getPlaceholder());
        
        final List<ActionButton> buttons = result.getBotoes();
        assertNotNull(buttons);
        assertEquals(2, buttons.size());
        
        final ActionButton createButton = buttons.get(0);
        assertEquals("Criar Pauta", createButton.getTexto());
        assertEquals("PRIMARY", createButton.getEstilo());
        assertEquals("http://localhost:8080/api/v1/proposal", createButton.getUrl());
        assertEquals("POST", createButton.getMetodo());
        
        final ActionButton cancelButton = buttons.get(1);
        assertEquals("Cancelar", cancelButton.getTexto());
        assertEquals("SECONDARY", cancelButton.getEstilo());
        assertEquals("http://localhost:8080/api/v1/mobile/proposals", cancelButton.getUrl());
        assertEquals("GET", cancelButton.getMetodo());
    }

    @Test
    void createVotingOptions_ShouldReturnValidMobileSelectionScreen() {
        final MobileSelectionScreen result = mobileScreenService.createVotingOptions(mockProposal);

        assertNotNull(result);
        assertEquals("SELECAO", result.getTipo());
        assertEquals("Votar em: Test Proposal", result.getTitulo());
        assertEquals("Test Description", result.getDescricao());
        
        final List<SelectionOption> options = result.getOpcoes();
        assertNotNull(options);
        assertEquals(2, options.size());
        
        final SelectionOption simOption = options.get(0);
        assertEquals("SIM - Aprovar a pauta", simOption.getTexto());
        assertEquals("true", simOption.getValor());
        assertEquals("http://localhost:8080/api/v1/mobile/vote-form/" + proposalId + "/true", simOption.getUrl());
        assertEquals("GET", simOption.getMetodo());
        
        final SelectionOption naoOption = options.get(1);
        assertEquals("NÃO - Rejeitar a pauta", naoOption.getTexto());
        assertEquals("false", naoOption.getValor());
        assertEquals("http://localhost:8080/api/v1/mobile/vote-form/" + proposalId + "/false", naoOption.getUrl());
        assertEquals("GET", naoOption.getMetodo());
    }

    @Test
    void createVotingForm_ShouldHandleNullProposal() {
        assertThrows(NullPointerException.class, () -> {
            mobileScreenService.createVotingForm(null, mockSession);
        });
    }


    @Test
    void createVotingForm_ShouldUseCorrectUrlsWithDifferentConfig() {
        ReflectionTestUtils.setField(mobileScreenService, "contextPath", "/custom");
        ReflectionTestUtils.setField(mobileScreenService, "baseUrl", "https://api.example.com");

        final MobileFormScreen result = mobileScreenService.createVotingForm(mockProposal, mockSession);

        final List<ActionButton> buttons = result.getBotoes();
        assertEquals("https://api.example.com/custom/proposal/" + proposalId + "/vote", buttons.get(0).getUrl());
        assertEquals("https://api.example.com/custom/proposal/" + proposalId + "/vote", buttons.get(1).getUrl());
    }

    @Test
    void createProposalList_ShouldUseCorrectUrlsWithDifferentConfig() {
        ReflectionTestUtils.setField(mobileScreenService, "contextPath", "/custom");
        ReflectionTestUtils.setField(mobileScreenService, "baseUrl", "https://api.example.com");
        
        final Proposal proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final List<Proposal> proposals = Arrays.asList(proposal);

        final MobileSelectionScreen result = mobileScreenService.createProposalList(proposals);

        final List<SelectionOption> options = result.getOpcoes();
        assertEquals("https://api.example.com/custom/mobile/proposal/" + proposalId, options.get(0).getUrl());
    }

    @Test
    void createProposalForm_ShouldUseCorrectUrlsWithDifferentConfig() {
        ReflectionTestUtils.setField(mobileScreenService, "contextPath", "/custom");
        ReflectionTestUtils.setField(mobileScreenService, "baseUrl", "https://api.example.com");

        final MobileFormScreen result = mobileScreenService.createProposalForm();

        final List<ActionButton> buttons = result.getBotoes();
        assertEquals("https://api.example.com/custom/proposal", buttons.get(0).getUrl());
        assertEquals("https://api.example.com/custom/mobile/proposals", buttons.get(1).getUrl());
    }

    @Test
    void createVotingOptions_ShouldUseCorrectUrlsWithDifferentConfig() {
        ReflectionTestUtils.setField(mobileScreenService, "contextPath", "/custom");
        ReflectionTestUtils.setField(mobileScreenService, "baseUrl", "https://api.example.com");

        final MobileSelectionScreen result = mobileScreenService.createVotingOptions(mockProposal);

        final List<SelectionOption> options = result.getOpcoes();
        assertEquals("https://api.example.com/custom/mobile/vote-form/" + proposalId + "/true", options.get(0).getUrl());
        assertEquals("https://api.example.com/custom/mobile/vote-form/" + proposalId + "/false", options.get(1).getUrl());
    }

    @Test
    void createVotingForm_ShouldAddItemsToMutableList() {
        final MobileFormScreen result = mobileScreenService.createVotingForm(mockProposal, mockSession);

        final List<FormField> items = result.getItens();
        assertNotNull(items);
        final int originalSize = items.size();
        
        final FormField newField = FormField.builder()
                .id("test")
                .tipo("TEXT")
                .build();
        
        assertDoesNotThrow(() -> items.add(newField));
        assertEquals(originalSize + 1, items.size());
    }
}
