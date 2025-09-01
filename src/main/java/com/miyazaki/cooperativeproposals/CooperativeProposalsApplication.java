package com.miyazaki.cooperativeproposals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public final class CooperativeProposalsApplication {

    private CooperativeProposalsApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(CooperativeProposalsApplication.class, args);
    }
}
