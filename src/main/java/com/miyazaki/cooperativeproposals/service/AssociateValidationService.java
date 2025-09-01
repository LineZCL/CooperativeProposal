package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.client.CpfValidationClient;
import com.miyazaki.cooperativeproposals.client.dto.CpfValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssociateValidationService {

    private final CpfValidationClient cpfValidationClient;
    private static final String VALID_CPF_STATUS = "ABLE_TO_VOTE";

    @Value("${app.cpf-validation-enabled}")
    private boolean isCpfValidationEnabled;

    public boolean isValidCpf(String cpf) {
        if (!isCpfValidationEnabled) {
            log.info("CPF validation is disabled. Considering CPF {} as valid", cpf);
            return true;
        }
        
        try {
            String cleanCpf = cpf.replaceAll("[^0-9]", "");
            
            log.info("Validating CPF: {}", cpf);
            
            CpfValidationResponse response = cpfValidationClient.validateCpf(cleanCpf);
            
            log.info("Validate result for CPF {}: status={}",
                    cpf, response.getStatus());

            return Objects.nonNull(response.getStatus()) && response.getStatus().equals(VALID_CPF_STATUS);
            
        } catch (Exception e) {
            log.error("Error to validate CPF {}: {}", cpf, e.getMessage());
            throw new RuntimeException("Erro ao validar cpf");
        }
    }

}
