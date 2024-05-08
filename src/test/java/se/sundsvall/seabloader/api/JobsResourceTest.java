package se.sundsvall.seabloader.api;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.seabloader.scheduler.dbcleaner.DatabaseCleanerScheduler;
import se.sundsvall.seabloader.scheduler.notifier.NotifierScheduler;
import se.sundsvall.seabloader.service.AsyncExecutorService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class JobsResourceTest {
	private static final String PATH = "/jobs";

	@MockBean
	private AsyncExecutorService asyncExecutorService;

	@MockBean
	private NotifierScheduler notifierScheduler;

	@MockBean
	private DatabaseCleanerScheduler databaseCleanerScheduler;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void invoiceexporter() {

		// Call
		webTestClient.post().uri(PATH + "/invoiceexporter")
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService).invoiceExportExecute();
		verify(asyncExecutorService, never()).databaseCleanerExecute();
		verify(asyncExecutorService, never()).notifierExecute();
	}

	@Test
	void dbcleaner() {

		// Call
		webTestClient.post().uri(PATH + "/dbcleaner")
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService, never()).invoiceExportExecute();
		verify(asyncExecutorService).databaseCleanerExecute();
		verify(asyncExecutorService, never()).notifierExecute();
	}

	@Test
	void notifier() {

		// Call
		webTestClient.post().uri(PATH + "/notifier")
			.exchange()
			.expectStatus().isNoContent();

		// Verifications
		verify(asyncExecutorService, never()).invoiceExportExecute();
		verify(asyncExecutorService, never()).databaseCleanerExecute();
		verify(asyncExecutorService).notifierExecute();
	}
}
