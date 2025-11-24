package com.foodfast.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testFailure() {
		assertEquals(2, 2, "This test intentionally fails to demonstrate failure in Grafana");
	}

}
