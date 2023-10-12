package se.sundsvall.seabloader.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.invoicecache.InvoicePdfRequest;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class InvoiceServiceTest {

	private static final String TEST_INVOICE_FILE = "files/invoice/invoice1.xml";
	private static final String TEST_FAULTY_INVOICE_FILE = "files/invoice/invoice2.xml";

	private static final String TEST_INVOICE_FILE_WITH_ATTACHMENTS = "files/pdfutility/invoice1.xml";
	private static final String TEST_INVOICE_FILE_WITHOUT_ATTACHMENTS = "files/pdfutility/invoice2.xml";

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@Mock
	private InvoiceCacheClient invoiceCacheClientMock;

	@Mock
	private InvoicePdfMerger invoicePdfMergerMock;

	@Captor
	private ArgumentCaptor<InvoicePdfRequest> invoicePdfRequestCaptor;

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
		assertThat(capturedInvoiceEntity.getStatus()).isEqualTo(IMPORT_FAILED);
		assertThat(capturedInvoiceEntity.getStatusMessage())
			.isEqualTo("Deserialization of received XML failed with message: SAXParseException: Premature end of file.");
	}

	@Test
	void exportInvoicesWhenInvoicesToSend(@Load(TEST_INVOICE_FILE_WITH_ATTACHMENTS) final String xml) {

		// Setup.
		final var pdfs = new ByteArrayOutputStream();
		pdfs.writeBytes("pdfs".getBytes(UTF_8));

		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(createInvoiceIds());
		when(invoiceRepositoryMock.findById(1L)).thenReturn(Optional.of(new InvoiceEntity().withInvoiceId("INVOICE_ID_1").withContent(xml)));
		when(invoiceRepositoryMock.findById(2L)).thenReturn(Optional.of(new InvoiceEntity().withInvoiceId("INVOICE_ID_2").withContent(xml)));
		when(invoicePdfMergerMock.mergePdfs(any())).thenReturn(pdfs);

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verify(invoiceRepositoryMock).findById(1L);
		verify(invoiceRepositoryMock).findById(2L);
		verify(invoiceRepositoryMock, times(2)).save(invoiceEntityCaptor.capture());
		verify(invoiceCacheClientMock, times(2)).sendInvoice(any());
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(2);
		assertThat(capturedInvoiceEntities.get(0).getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.get(0).getContent()).isEqualTo(xml);
		assertThat(capturedInvoiceEntities.get(0).getStatus()).isEqualTo(PROCESSED);
		assertThat(capturedInvoiceEntities.get(1).getInvoiceId()).isEqualTo("INVOICE_ID_2");
		assertThat(capturedInvoiceEntities.get(1).getStatus()).isEqualTo(PROCESSED);
		assertThat(capturedInvoiceEntities.get(1).getContent()).isEqualTo(xml);
	}

	@Test
	void exportInvoicesWhenNoInvoicesToSend() {

		//Setup.
		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(emptyList());

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verifyNoMoreInteractions(invoiceRepositoryMock);
		verifyNoInteractions(invoiceCacheClientMock);
	}

	@Test
	void exportInvoicesWhenNoInvoiceIdMatchInDb() {

		// Setup.
		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(List.of(createInvoiceIdInstance(1L)));
		when(invoiceRepositoryMock.findById(1L))
			.thenReturn(Optional.empty());

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verify(invoiceRepositoryMock).findById(1L);
		verifyNoMoreInteractions(invoiceRepositoryMock);
		verifyNoInteractions(invoiceCacheClientMock);
	}

	@Test
	void exportInvoicesWhenExceptionInSending(@Load(TEST_INVOICE_FILE_WITH_ATTACHMENTS) final String xml) {

		final var pdfs = new ByteArrayOutputStream();
		pdfs.writeBytes("pdfs".getBytes(UTF_8));

		final var invoiceIds = createInvoiceIds();
		final var invoiceEntity1 = new InvoiceEntity().withInvoiceId("INVOICE_ID_1").withContent(xml);
		final var invoiceEntity2 = new InvoiceEntity().withInvoiceId("INVOICE_ID_2").withContent(xml);
		// Setup.
		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(invoiceIds);
		when(invoiceRepositoryMock.findById(1L)).thenReturn(Optional.of(invoiceEntity1));
		when(invoiceRepositoryMock.findById(2L)).thenReturn(Optional.of(invoiceEntity2));
		when(invoicePdfMergerMock.mergePdfs(any())).thenReturn(pdfs);
		doThrow(new RuntimeException("Test exception")).when(invoiceCacheClientMock).sendInvoice(any());

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verify(invoiceRepositoryMock).findById(1L);
		verify(invoiceRepositoryMock).findById(2L);
		verify(invoiceRepositoryMock, times(2)).save(invoiceEntityCaptor.capture());
		verify(invoiceCacheClientMock, times(2)).sendInvoice(any());
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(2);
		assertThat(capturedInvoiceEntities.get(0).getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.get(0).getContent()).isNotNull();
		assertThat(capturedInvoiceEntities.get(0).getStatus()).isEqualTo(EXPORT_FAILED);
		assertThat(capturedInvoiceEntities.get(1).getInvoiceId()).isEqualTo("INVOICE_ID_2");
		assertThat(capturedInvoiceEntities.get(1).getStatus()).isEqualTo(EXPORT_FAILED);
		assertThat(capturedInvoiceEntities.get(1).getContent()).isNotNull();
	}

	@Test
	void exportInvoicesWhenNoAttachments(@Load(TEST_INVOICE_FILE_WITHOUT_ATTACHMENTS) final String xml) {

		final var invoiceEntity1 = new InvoiceEntity().withInvoiceId("INVOICE_ID_1").withContent(xml);
		// Setup.
		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(List.of(createInvoiceIdInstance(1L)));
		when(invoiceRepositoryMock.findById(1L)).thenReturn(Optional.of(invoiceEntity1));

		// Call.
		service.exportInvoices();

		// Verification.
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verify(invoiceRepositoryMock).findById(1L);
		verify(invoiceRepositoryMock, times(1)).save(invoiceEntityCaptor.capture());
		verifyNoInteractions(invoiceCacheClientMock);
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(1);
		assertThat(capturedInvoiceEntities.get(0).getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.get(0).getContent()).isNotNull();
		assertThat(capturedInvoiceEntities.get(0).getStatus()).isEqualTo(EXPORT_FAILED);
		assertThat(capturedInvoiceEntities.get(0).getStatusMessage()).isEqualTo("OriginalInvoice or attachments not found in invoice with invoiceId: 683288");
	}

	private List<InvoiceId> createInvoiceIds() {
		return List.of(createInvoiceIdInstance(1L), createInvoiceIdInstance(2L));
	}

	private InvoiceId createInvoiceIdInstance(final long id) {
		return () -> id;
	}
}
