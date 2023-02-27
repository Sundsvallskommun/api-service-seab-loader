package se.sundsvall.seabloader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class InvoicesResourceFailuresTest {

	private static final String PATH = "/invoices";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createInvoiceEmptyContent() throws IOException {

		// Call
		final var response = webTestClient.post().uri(PATH)
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
			+ "public org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.seabloader.api.InvoicesResource.createInvoice(byte[])");

		// TODO: verifyNoInteractions(serviceMock);
	}

	@Test
	void createInvoiceWrongContentType() throws IOException {

		// Call
		final var response = webTestClient.post().uri(PATH)
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
		assertThat(response.getDetail()).isEqualTo("Content type 'application/json' not supported");

		// TODO: verifyNoInteractions(serviceMock);
	}
}
