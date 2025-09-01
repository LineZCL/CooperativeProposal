package com.miyazaki.cooperativeproposals.client;

import com.miyazaki.cooperativeproposals.client.dto.CpfValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "cpf-validation-client",
    url = "${feign.cpf-validation.url}"
)
public interface CpfValidationClient {

    @GetMapping("/users/{cpf}")
    CpfValidationResponse validateCpf(@PathVariable("cpf") String cpf);
}
