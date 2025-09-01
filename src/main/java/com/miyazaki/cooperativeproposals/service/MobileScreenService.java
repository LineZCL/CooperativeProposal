package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.*;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileScreenService {
    
    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public MobileFormScreen createVotingForm(Proposal proposal, VotingSession session) {
        log.info("Creating voting form for proposal: {}", proposal.getId());
        
        return MobileFormScreen.builder()
                .tipo("FORMULARIO")
                .titulo("Votação - " + proposal.getTitle())
                .descricao(proposal.getDescription())
                .itens(new ArrayList<>(List.of(
                        FormField.builder()
                                .id("associateId")
                                .label("ID do Associado")
                                .tipo("TEXT")
                                .obrigatorio(true)
                                .placeholder("Digite seu ID de associado")
                                .build(),
                        FormField.builder()
                                .id("associateCpf")
                                .label("CPF")
                                .tipo("TEXT")
                                .obrigatorio(true)
                                .placeholder("000.000.000-00")
                                .build(),
                        FormField.builder()
                                .id("proposalId")
                                .tipo("HIDDEN")
                                .valor(proposal.getId().toString())
                                .build()
                )))
                .botoes(List.of(
                        ActionButton.builder()
                                .texto("SIM")
                                .estilo("PRIMARY")
                                .url(baseUrl + contextPath + "/proposal/" + proposal.getId() + "/vote")
                                .metodo("POST")
                                .body(Map.of("vote", true, "associateId", "", "associateCpf", ""))
                                .build(),
                        ActionButton.builder()
                                .texto("NÃO")
                                .estilo("SECONDARY")
                                .url(baseUrl + contextPath + "/proposal/" + proposal.getId() + "/vote")
                                .metodo("POST")
                                .body(Map.of("vote", false, "associateId", "", "associateCpf", ""))
                                .build()
                ))
                .build();
    }
    
    public MobileSelectionScreen createProposalList(List<Proposal> proposals) {
        log.info("Creating proposal selection screen with {} proposals", proposals.size());
        
        final List<SelectionOption> options = proposals.stream()
                .map(proposal -> SelectionOption.builder()
                        .texto(proposal.getTitle())
                        .valor(proposal.getId().toString())
                        .descricao(proposal.getDescription())
                        .url(baseUrl + contextPath + "/mobile/proposal/" + proposal.getId())
                        .metodo("GET")
                        .build())
                .toList();
        
        return MobileSelectionScreen.builder()
                .tipo("SELECAO")
                .titulo("Selecione uma Pauta")
                .descricao("Escolha uma pauta para visualizar ou votar")
                .opcoes(options)
                .build();
    }
    
    public MobileFormScreen createProposalForm() {
        log.info("Creating new proposal form");
        
        return MobileFormScreen.builder()
                .tipo("FORMULARIO")
                .titulo("Nova Pauta")
                .descricao("Criar uma nova pauta para votação")
                .itens(List.of(
                        FormField.builder()
                                .id("title")
                                .label("Título da Pauta")
                                .tipo("TEXT")
                                .obrigatorio(true)
                                .placeholder("Digite o título da pauta")
                                .build(),
                        FormField.builder()
                                .id("description")
                                .label("Descrição")
                                .tipo("TEXT")
                                .obrigatorio(false)
                                .placeholder("Descreva a pauta (opcional)")
                                .build()
                ))
                .botoes(List.of(
                        ActionButton.builder()
                                .texto("Criar Pauta")
                                .estilo("PRIMARY")
                                .url(baseUrl + contextPath + "/proposal")
                                .metodo("POST")
                                .build(),
                        ActionButton.builder()
                                .texto("Cancelar")
                                .estilo("SECONDARY")
                                .url(baseUrl + contextPath + "/mobile/proposals")
                                .metodo("GET")
                                .build()
                ))
                .build();
    }
    
    public MobileSelectionScreen createVotingOptions(Proposal proposal, VotingSession session) {
        log.info("Creating voting options for proposal: {}", proposal.getId());
        
        return MobileSelectionScreen.builder()
                .tipo("SELECAO")
                .titulo("Votar em: " + proposal.getTitle())
                .descricao(proposal.getDescription())
                .opcoes(List.of(
                        SelectionOption.builder()
                                .texto("SIM - Aprovar a pauta")
                                .valor("true")
                                .url(baseUrl + contextPath + "/mobile/vote-form/" + proposal.getId() + "/true")
                                .metodo("GET")
                                .build(),
                        SelectionOption.builder()
                                .texto("NÃO - Rejeitar a pauta")
                                .valor("false")
                                .url(baseUrl + contextPath + "/mobile/vote-form/" + proposal.getId() + "/false")
                                .metodo("GET")
                                .build()
                ))
                .build();
    }
}