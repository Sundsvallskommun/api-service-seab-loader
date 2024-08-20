package se.sundsvall.seabloader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.seabloader.service.InvoiceService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class InvoicesResourceFailuresTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/{municipalityId}/invoices";

	@MockBean
	private InvoiceService invoiceService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createInvoiceEmptyContent() {

		// Call
		final var response = webTestClient.post().uri(uriBuilder -> uriBuilder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_XML)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Verifications
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("Required request body is missing: "
			+ "public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.seabloader.api.InvoicesResource.createInvoice(java.lang.String,byte[])");

		verifyNoInteractions(invoiceService);
	}

	@Test
	void createInvoiceWrongContentType() {

		// Call
		final var response = webTestClient.post().uri(uriBuilder -> uriBuilder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNSUPPORTED_MEDIA_TYPE)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Verifications
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Unsupported Media Type");
		assertThat(response.getStatus()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
		assertThat(response.getDetail()).isEqualTo("Content-Type 'application/json' is not supported");

		verifyNoInteractions(invoiceService);
	}
}
