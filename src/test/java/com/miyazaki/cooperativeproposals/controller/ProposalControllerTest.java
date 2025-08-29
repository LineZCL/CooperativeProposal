package com.miyazaki.cooperativeproposals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProposalController.class)
class ProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProposalService proposalService;

    @Test
    void create_ShouldReturnNoContent_WhenValidProposalProvided() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest("Valid Title", "Valid Description");
        doNothing().when(proposalService).create(any(CreateProposalRequest.class));

        mockMvc.perform(post("/proposal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(proposalService, times(1)).create(any(CreateProposalRequest.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest("", "Valid Description");

        mockMvc.perform(post("/proposal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.details").value("title: must not be blank"));

        verify(proposalService, never()).create(any());
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTitleIsNull() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest(null, "Valid Description");

        mockMvc.perform(post("/proposal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.details").value("title: must not be blank"));

        verify(proposalService, never()).create(any());
    }
}
