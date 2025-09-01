package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.client.CpfValidationClient;
import com.miyazaki.cooperativeproposals.client.dto.CpfValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssociateValidationServiceTest {

    @Mock
    private CpfValidationClient cpfValidationClient;

    @InjectMocks
    private AssociateValidationService associateValidationService;

    private static final String VALID_CPF_STATUS = "ABLE_TO_VOTE";
    private static final String INVALID_CPF_STATUS = "UNABLE_TO_VOTE";
    private static final String VALID_CPF = "12345678901";
    private static final String FORMATTED_CPF = "123.456.789-01";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(associateValidationService, "isCpfValidationEnabled", true);
    }

    @Test
    void isValidCpf_ShouldReturnTrue_WhenCpfIsValidAndStatusIsAbleToVote() {
        final CpfValidationResponse response = CpfValidationResponse.builder()
                .status(VALID_CPF_STATUS)
                .build();
        
        when(cpfValidationClient.validateCpf(VALID_CPF)).thenReturn(response);

        final boolean result = associateValidationService.isValidCpf(VALID_CPF);

        assertTrue(result);
        verify(cpfValidationClient, times(1)).validateCpf(VALID_CPF);
    }

    @Test
    void isValidCpf_ShouldReturnTrue_WhenFormattedCpfIsValidAndStatusIsAbleToVote() {
        final CpfValidationResponse response = CpfValidationResponse.builder()
                .status(VALID_CPF_STATUS)
                .build();
        
        when(cpfValidationClient.validateCpf(VALID_CPF)).thenReturn(response);

        final boolean result = associateValidationService.isValidCpf(FORMATTED_CPF);

        assertTrue(result);
        verify(cpfValidationClient, times(1)).validateCpf(VALID_CPF);
    }

    @Test
    void isValidCpf_ShouldReturnFalse_WhenCpfIsValidButStatusIsUnableToVote() {
        final CpfValidationResponse response = CpfValidationResponse.builder()
                .status(INVALID_CPF_STATUS)
                .build();
        
        when(cpfValidationClient.validateCpf(VALID_CPF)).thenReturn(response);

        final boolean result = associateValidationService.isValidCpf(VALID_CPF);

        assertFalse(result);
        verify(cpfValidationClient, times(1)).validateCpf(VALID_CPF);
    }

    @Test
    void isValidCpf_ShouldReturnFalse_WhenStatusIsNull() {
        final CpfValidationResponse response = CpfValidationResponse.builder()
                .status(null)
                .build();
        
        when(cpfValidationClient.validateCpf(VALID_CPF)).thenReturn(response);
        final boolean result = associateValidationService.isValidCpf(VALID_CPF);

        assertFalse(result);
        verify(cpfValidationClient, times(1)).validateCpf(VALID_CPF);
    }

    @Test
    void isValidCpf_ShouldReturnFalse_WhenStatusIsEmpty() {
        final CpfValidationResponse response = CpfValidationResponse.builder()
                .status("")
                .build();
        
        when(cpfValidationClient.validateCpf(VALID_CPF)).thenReturn(response);

        final boolean result = associateValidationService.isValidCpf(VALID_CPF);

        assertFalse(result);
        verify(cpfValidationClient, times(1)).validateCpf(VALID_CPF);
    }

    @Test
    void isValidCpf_ShouldThrowRuntimeException_WhenClientThrowsException() {
        when(cpfValidationClient.validateCpf(anyString()))
                .thenThrow(new RuntimeException("Client connection error"));

        final RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> associateValidationService.isValidCpf(VALID_CPF));

        assertEquals("Erro ao validar cpf", exception.getMessage());
        verify(cpfValidationClient, times(1)).validateCpf(VALID_CPF);
    }

    @Test
    void isValidCpf_ShouldReturnTrue_WhenValidationIsDisabled() {
        ReflectionTestUtils.setField(associateValidationService, "isCpfValidationEnabled", false);

        final boolean result = associateValidationService.isValidCpf(VALID_CPF);

        assertTrue(result);
        verify(cpfValidationClient, never()).validateCpf(anyString());
    }

}
