package com.miyazaki.cooperativeproposals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.cpf-validation-enabled=true")

class CooperativeProposalsApplicationTests {

	@Test
	void contextLoads() {
	}

}
