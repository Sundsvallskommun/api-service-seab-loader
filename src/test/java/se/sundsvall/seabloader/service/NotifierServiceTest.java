package se.sundsvall.seabloader.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.integration.messaging.MessagingClient;

import generated.se.sundsvall.messaging.EmailRequest;

@ExtendWith(MockitoExtension.class)
class NotifierServiceTest {

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@Mock
	private MessagingClient messagingClientMock;

	@Captor
	private ArgumentCaptor<EmailRequest> emailRequestCaptor;

	@InjectMocks
	private NotifierService service;

	@BeforeEach
	void setup() {
		setField(service, "applicationName", "MyApplication");
		setField(service, "applicationEnvironment", "test");
		setField(service, "mailNotificationEnabled", true);
		setField(service, "mailRecipientAddress", "recipient@host.com");
		setField(service, "mailSenderAddress", "sender@host.com");
	}

	@Test
	void executeWithNoFailedRecords() {

		// Setup
		when(invoiceRepositoryMock.findByStatusIn(Status.values())).thenReturn(emptyList());

		// Call.
		service.sendFailureNotification();

		// Verification.
		verify(invoiceRepositoryMock).findByStatusIn(Status.values());
		verifyNoInteractions(messagingClientMock);
	}

	@Test
	void executeWithExportFailedRecords() {

		// Setup
		when(invoiceRepositoryMock.findByStatusIn(Status.values())).thenReturn(List.of(InvoiceEntity.create().withStatus(EXPORT_FAILED).withMunicipalityId("2281")));

		// Call.
		service.sendFailureNotification();

		// Verification.
		verify(invoiceRepositoryMock).findByStatusIn(Status.values());
		verify(messagingClientMock).sendEmail(eq("2281"), emailRequestCaptor.capture());

		final var capturedEmailRequest = emailRequestCaptor.getValue();
		assertThat(capturedEmailRequest).isNotNull();
		assertThat(capturedEmailRequest.getEmailAddress()).isEqualTo("recipient@host.com");
		assertThat(capturedEmailRequest.getSender().getName()).isEqualTo("MyApplication");
		assertThat(capturedEmailRequest.getSender().getAddress()).isEqualTo("sender@host.com");
		assertThat(capturedEmailRequest.getSubject()).isEqualTo("Failed records discovered in MyApplication (test)");
		assertThat(capturedEmailRequest.getMessage()).isEqualToIgnoringWhitespace("""
				Failed record(s) exist in test-database!
			
				UNPROCESSED         	0 records
				PROCESSED           	0 records
				EXPORT_FAILED       	1 records
				IMPORT_FAILED       	0 records
			""");
	}

	@Test
	void executeWithImportFailedRecords() {

		// Setup
		when(invoiceRepositoryMock.findByStatusIn(Status.values())).thenReturn(List.of(InvoiceEntity.create().withStatus(IMPORT_FAILED).withMunicipalityId("2281")));

		// Call.
		service.sendFailureNotification();

		// Verification.
		verify(invoiceRepositoryMock).findByStatusIn(Status.values());
		verify(messagingClientMock).sendEmail(eq("2281"), emailRequestCaptor.capture());

		final var capturedEmailRequest = emailRequestCaptor.getValue();
		assertThat(capturedEmailRequest).isNotNull();
		assertThat(capturedEmailRequest.getEmailAddress()).isEqualTo("recipient@host.com");
		assertThat(capturedEmailRequest.getSender().getName()).isEqualTo("MyApplication");
		assertThat(capturedEmailRequest.getSender().getAddress()).isEqualTo("sender@host.com");
		assertThat(capturedEmailRequest.getSubject()).isEqualTo("Failed records discovered in MyApplication (test)");
		assertThat(capturedEmailRequest.getMessage()).isEqualToIgnoringWhitespace("""
				Failed record(s) exist in test-database!
			
				UNPROCESSED         	0 records
				PROCESSED           	0 records
				EXPORT_FAILED       	0 records
				IMPORT_FAILED       	1 records
			""");
	}

	@Test
	void executeWithFailedRecordsAndMailNotificationDisabled() {

		// Setup
		setField(service, "mailNotificationEnabled", false);

		// Call.
		service.sendFailureNotification();

		// Verification.
		verifyNoInteractions(invoiceRepositoryMock);
		verifyNoInteractions(messagingClientMock);
	}

}
