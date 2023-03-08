package se.sundsvall.seabloader.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class InvoiceServiceTest {

	private static final String TEST_INVOICE_FILE = "files/invoice/invoice1.xml";
	private static final String TEST_FAULTY_INVOICE_FILE = "files/invoice/invoice2.xml";

	private static final String TEST_INVOICE_FILE_WITH_ATTACHEMENTS = "files/pdfutility/invoice1.xml";

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@Mock
	private InvoiceCacheClient invoiceCacheClientMock;

	@Captor
	private ArgumentCaptor<InvoiceEntity> invoiceEntityCaptor;

	@InjectMocks
	private InvoiceService service;

	@BeforeAll
	static void setup() {
		Locale.setDefault(ENGLISH);
	}

	@Test
	void create(@Load(TEST_INVOICE_FILE) final String xml) {

		// Call
		service.create(xml.getBytes(UTF_8));

		// Verification
		verify(invoiceRepositoryMock).existsByInvoiceId("683288");
		verify(invoiceRepositoryMock).save(invoiceEntityCaptor.capture());

		final var capturedInvoiceEntity = invoiceEntityCaptor.getValue();
		assertThat(capturedInvoiceEntity).isNotNull();
		assertThat(capturedInvoiceEntity.getContent()).isEqualTo(xml);
		assertThat(capturedInvoiceEntity.getInvoiceId()).isEqualTo("683288");
	}

	@Test
	void createWhenInvoiceIdExists(@Load(TEST_INVOICE_FILE) final String xml) {

		// Setup
		when(invoiceRepositoryMock.existsByInvoiceId(any())).thenReturn(true);

		// Call
		service.create(xml.getBytes(UTF_8));

		// Verification
		verify(invoiceRepositoryMock).existsByInvoiceId("683288");
		verify(invoiceRepositoryMock, never()).save(any());
	}

	@Test
	void createWhenXmlIsFaulty(@Load(TEST_FAULTY_INVOICE_FILE) final String xml) {

		// Call
		service.create(xml.getBytes(UTF_8));

		// Verification
		verify(invoiceRepositoryMock, never()).existsByInvoiceId(any());
		verify(invoiceRepositoryMock).save(invoiceEntityCaptor.capture());

		final var capturedInvoiceEntity = invoiceEntityCaptor.getValue();
		assertThat(capturedInvoiceEntity).isNotNull();
		assertThat(capturedInvoiceEntity.getContent()).isEqualTo(xml);
		assertThat(capturedInvoiceEntity.getInvoiceId()).isNull();
		assertThat(capturedInvoiceEntity.getStatus()).isEqualTo(FAILED);
		assertThat(capturedInvoiceEntity.getStatusMessage())
			.isEqualTo("Deserialization of received XML failed with message: SAXParseException: Premature end of file.");
	}

	@Test
	void exportInvoicesWhenInvoicesToSend(@Load(TEST_INVOICE_FILE_WITH_ATTACHEMENTS) final String xml) {

		//Setup.
		when(invoiceRepositoryMock.findInvoiceIdsByStatusIn(UNPROCESSED, FAILED)).thenReturn(List.of("INVOICE_ID_1", "INVOICE_ID_2"));
		when(invoiceRepositoryMock.findByInvoiceId("INVOICE_ID_1"))
			.thenReturn(Optional.of(new InvoiceEntity().withInvoiceId("INVOICE_ID_1").withContent(xml)));
		when(invoiceRepositoryMock.findByInvoiceId("INVOICE_ID_2"))
			.thenReturn(Optional.of(new InvoiceEntity().withInvoiceId("INVOICE_ID_2").withContent(xml)));

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findInvoiceIdsByStatusIn(UNPROCESSED, FAILED);
		verify(invoiceRepositoryMock).findByInvoiceId("INVOICE_ID_1");
		verify(invoiceRepositoryMock).findByInvoiceId("INVOICE_ID_2");
		verify(invoiceRepositoryMock, times(2)).save(invoiceEntityCaptor.capture());
		verify(invoiceCacheClientMock, times(2)).importInvoice(any());
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(2);
		assertThat(capturedInvoiceEntities.get(0).getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.get(0).getContent()).isNull();
		assertThat(capturedInvoiceEntities.get(0).getStatus()).isEqualTo(PROCESSED);
		assertThat(capturedInvoiceEntities.get(1).getInvoiceId()).isEqualTo("INVOICE_ID_2");
		assertThat(capturedInvoiceEntities.get(1).getStatus()).isEqualTo(PROCESSED);
		assertThat(capturedInvoiceEntities.get(1).getContent()).isNull();
	}

	@Test
	void exportInvoicesWhenNoInvoicesToSend() {

		//Setup.
		when(invoiceRepositoryMock.findInvoiceIdsByStatusIn(UNPROCESSED, FAILED)).thenReturn(emptyList());

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findInvoiceIdsByStatusIn(UNPROCESSED, FAILED);
		verifyNoMoreInteractions(invoiceRepositoryMock);
		verifyNoInteractions(invoiceCacheClientMock);
	}

	@Test
	void exportInvoicesWhenExceptionInSending(@Load(TEST_INVOICE_FILE_WITH_ATTACHEMENTS) final String xml) {

		//Setup.
		when(invoiceRepositoryMock.findInvoiceIdsByStatusIn(UNPROCESSED, FAILED)).thenReturn(List.of("INVOICE_ID_1", "INVOICE_ID_2"));
		when(invoiceRepositoryMock.findByInvoiceId("INVOICE_ID_1"))
			.thenReturn(Optional.of(new InvoiceEntity().withInvoiceId("INVOICE_ID_1").withContent(xml)));
		when(invoiceRepositoryMock.findByInvoiceId("INVOICE_ID_2"))
			.thenReturn(Optional.of(new InvoiceEntity().withInvoiceId("INVOICE_ID_2").withContent(xml)));
		when(invoiceCacheClientMock.importInvoice(any())).thenThrow(new RuntimeException("Test exception"));

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findInvoiceIdsByStatusIn(UNPROCESSED, FAILED);
		verify(invoiceRepositoryMock).findByInvoiceId("INVOICE_ID_1");
		verify(invoiceRepositoryMock).findByInvoiceId("INVOICE_ID_2");
		verify(invoiceRepositoryMock, times(2)).save(invoiceEntityCaptor.capture());
		verify(invoiceCacheClientMock, times(2)).importInvoice(any());
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(2);
		assertThat(capturedInvoiceEntities.get(0).getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.get(0).getContent()).isNotNull();
		assertThat(capturedInvoiceEntities.get(0).getStatus()).isEqualTo(FAILED);
		assertThat(capturedInvoiceEntities.get(1).getInvoiceId()).isEqualTo("INVOICE_ID_2");
		assertThat(capturedInvoiceEntities.get(1).getStatus()).isEqualTo(FAILED);
		assertThat(capturedInvoiceEntities.get(1).getContent()).isNotNull();
	}
}
