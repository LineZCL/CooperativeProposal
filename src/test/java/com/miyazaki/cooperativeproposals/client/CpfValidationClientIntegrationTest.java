package com.miyazaki.cooperativeproposals.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.miyazaki.cooperativeproposals.client.dto.CpfValidationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "feign.cpf-validation.url=http://localhost:${wiremock.server.port}"
})
class CpfValidationClientIntegrationTest {

    @Autowired
    private CpfValidationClient cpfValidationClient;

    @Autowired
    private WireMockServer wireMockServer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }

    @Test
    void validateCpf_ShouldReturnValidResponse_WhenCpfIsValid() throws Exception {
        final String cpf = "12345678901";
        final String statusApproved = "ABLE_TO_VOTE";
        final CpfValidationResponse expectedResponse = CpfValidationResponse.builder()
                .status(statusApproved)
                .build();

        stubFor(get(urlEqualTo("/users/" + cpf))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(expectedResponse))));

        CpfValidationResponse actualResponse = cpfValidationClient.validateCpf(cpf);

        assertNotNull(actualResponse);
        assertEquals(statusApproved, actualResponse.getStatus());
    }

    @Test
    void validateCpf_ShouldReturnInvalidResponse_WhenCpfIsInvalid() throws Exception {
        String cpf = "12345678900";
        final String statusUnable= "UNABLE_TO_VOTE";
        CpfValidationResponse expectedResponse = CpfValidationResponse.builder()
                .status(statusUnable)
                .build();

        stubFor(get(urlEqualTo("/users/" + cpf))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(expectedResponse))));

        CpfValidationResponse actualResponse = cpfValidationClient.validateCpf(cpf);

        assertNotNull(actualResponse);
        assertEquals(statusUnable, actualResponse.getStatus());
    }
}

