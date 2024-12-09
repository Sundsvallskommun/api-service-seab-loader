package se.sundsvall.seabloader.integration.invoicecache.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.seabloader.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class InvoiceCachePropertiesTest {

	@Autowired
	private InvoiceCacheProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(10);
		assertThat(properties.readTimeout()).isEqualTo(20);
	}
}
