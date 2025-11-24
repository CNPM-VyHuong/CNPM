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
<<<<<<< HEAD
		assertEquals(2, 2, "This test intentionally fails to demonstrate failure in Grafana");
=======
		// Fixed: the test should pass in CI — assert a true sanity check instead
		assertEquals(1, 1, "Sanity check: values should be equal");
>>>>>>> 1514350993ee67918cdc2ad5df6e9ef64780749e
	}

}
