package se.sundsvall.seabloader.api;

import static java.nio.file.Files.readAllBytes;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.util.ResourceUtils.getFile;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.seabloader.service.InvoiceService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class InvoicesResourceTest {

	private static final String PATH = "/invoices";
	private static final String FILE_PATH = "classpath:files/invoice/invoice1.xml";

	@MockBean
	private InvoiceService invoiceService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createInvoice() throws IOException {

		// Setup
		final var fileContent = readAllBytes(getFile(FILE_PATH).toPath());

		// Call
		webTestClient.post().uri(PATH)
			.contentType(APPLICATION_XML)
			.bodyValue(fileContent)
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(invoiceService).create(fileContent);
	}
}
