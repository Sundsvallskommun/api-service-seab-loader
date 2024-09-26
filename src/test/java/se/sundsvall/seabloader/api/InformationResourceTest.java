package se.sundsvall.seabloader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.seabloader.Application;
import se.sundsvall.seabloader.api.model.SchedulerInformation;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class InformationResourceTest {

	private static final String MUNICIPALITY_ID = "2260";
	private static final String PATH = "/{municipalityId}/information/schedulers";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getSchedulerInformation() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(SchedulerInformation.class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(response)
			.hasSize(3)
			.extracting(
				SchedulerInformation::getDescription,
				SchedulerInformation::getExpression,
				SchedulerInformation::getName)
			.containsExactly(
				tuple("At 07:00, every day, only on Sunday", "0 0 7 * * 7", "DatabaseCleanerScheduler"),
				tuple("Every hour, every day", "0 0 */1 * * *", "InvoiceExportScheduler"),
				tuple("At 08:00, every day, Monday through Friday", "0 0 8 * * MON-FRI", "NotifierScheduler"));
	}
}
