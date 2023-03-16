package se.sundsvall.seabloader.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.seabloader.service.InvoiceService;

import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class JobsResourceTest {
	private static final String PATH = "/jobs";

	@MockBean
	private InvoiceService invoiceService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void export() {

		// Call
		webTestClient.post().uri(PATH + "/export")
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(invoiceService).exportInvoices();
	}
}

