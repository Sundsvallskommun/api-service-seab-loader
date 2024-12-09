package se.sundsvall.seabloader.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;

import generated.se.sundsvall.invoicecache.InvoicePdfRequest;
import java.io.ByteArrayOutputStream;
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
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class InvoiceServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
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

		// Act
		service.create(MUNICIPALITY_ID, xml.getBytes(UTF_8));

		// Assert
		verify(invoiceRepositoryMock).existsByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "683288");
		verify(invoiceRepositoryMock).save(invoiceEntityCaptor.capture());
		final var capturedInvoiceEntity = invoiceEntityCaptor.getValue();
		assertThat(capturedInvoiceEntity).isNotNull();
		assertThat(capturedInvoiceEntity.getContent()).isEqualTo(xml);
		assertThat(capturedInvoiceEntity.getInvoiceId()).isEqualTo("683288");
	}

	@Test
	void createWhenInvoiceIdExists(@Load(TEST_INVOICE_FILE) final String xml) {

		// Arrange
		when(invoiceRepositoryMock.existsByMunicipalityIdAndInvoiceId(eq(MUNICIPALITY_ID), any())).thenReturn(true);

		// Act
		service.create(MUNICIPALITY_ID, xml.getBytes(UTF_8));

		// Assert
		verify(invoiceRepositoryMock).existsByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "683288");
		verify(invoiceRepositoryMock, never()).save(any());
	}

	@Test
	void createWhenXmlIsFaulty(@Load(TEST_FAULTY_INVOICE_FILE) final String xml) {

		// Act
		service.create(MUNICIPALITY_ID, xml.getBytes(UTF_8));

		// Assert
		verify(invoiceRepositoryMock, never()).existsByMunicipalityIdAndInvoiceId(eq(MUNICIPALITY_ID), any());
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
	void sendInvoiceToInvoiceCache(@Load(TEST_INVOICE_FILE_WITH_ATTACHMENTS) final String xml) {

		// Arrange
		final var id = 1L;
		final var municipalityId = "2281";
		final var pdfs = new ByteArrayOutputStream();
		pdfs.writeBytes("pdfs".getBytes(UTF_8));

		when(invoiceRepositoryMock.findById(id)).thenReturn(Optional.of(new InvoiceEntity().withId(id).withMunicipalityId(municipalityId).withInvoiceId("INVOICE_ID_1").withContent(xml)));
		when(invoicePdfMergerMock.mergePdfs(any())).thenReturn(pdfs);

		// Act
		service.sendInvoiceToInvoiceCache(id);

		// Assert
		verify(invoiceRepositoryMock).findById(id);
		verify(invoiceRepositoryMock).save(invoiceEntityCaptor.capture());
		verify(invoiceCacheClientMock).sendInvoice(eq(municipalityId), any());
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(1);
		assertThat(capturedInvoiceEntities.getFirst().getId()).isEqualTo(id);
		assertThat(capturedInvoiceEntities.getFirst().getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.getFirst().getContent()).isEqualTo(xml);
		assertThat(capturedInvoiceEntities.getFirst().getStatus()).isEqualTo(PROCESSED);
		assertThat(capturedInvoiceEntities.getFirst().getStatusMessage()).isNull();
	}

	@Test
	void sendInvoiceToInvoiceCacheWhenNoInvoiceEntityFound() {

		// Arrange
		final var id = 1L;
		when(invoiceRepositoryMock.findById(id)).thenReturn(empty());

		// Act
		service.sendInvoiceToInvoiceCache(id);

		// Assert
		verify(invoiceRepositoryMock).findById(id);
		verifyNoMoreInteractions(invoiceRepositoryMock);
		verifyNoInteractions(invoiceCacheClientMock);
	}

	@Test
	void exportInvoicesWhenExceptionInSending(@Load(TEST_INVOICE_FILE_WITH_ATTACHMENTS) final String xml) {

		// Arrange
		final var municipalityId = "2281";
		final var id = 1L;
		final var invoiceEntity1 = new InvoiceEntity().withId(id).withMunicipalityId(municipalityId).withInvoiceId("INVOICE_ID_1").withContent(xml);
		final var pdfs = new ByteArrayOutputStream();
		pdfs.writeBytes("pdfs".getBytes(UTF_8));

		when(invoiceRepositoryMock.findById(id)).thenReturn(Optional.of(invoiceEntity1));
		when(invoicePdfMergerMock.mergePdfs(any())).thenReturn(pdfs);
		doThrow(new RuntimeException("Error occured")).when(invoiceCacheClientMock).sendInvoice(eq(municipalityId), any());

		// Act
		service.sendInvoiceToInvoiceCache(id);

		// Assert
		verify(invoiceRepositoryMock).findById(1L);
		verify(invoiceRepositoryMock).save(invoiceEntityCaptor.capture());
		verify(invoiceCacheClientMock).sendInvoice(eq(municipalityId), any());
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(1);
		assertThat(capturedInvoiceEntities.getFirst().getId()).isEqualTo(id);
		assertThat(capturedInvoiceEntities.getFirst().getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.getFirst().getContent()).isNotNull();
		assertThat(capturedInvoiceEntities.getFirst().getStatus()).isEqualTo(EXPORT_FAILED);
		assertThat(capturedInvoiceEntities.getFirst().getStatusMessage()).isEqualTo("Error occured");
	}

	@Test
	void sendInvoiceToInvoiceCacheWhenNoAttachments(@Load(TEST_INVOICE_FILE_WITHOUT_ATTACHMENTS) final String xml) {

		// Arrange
		final var id = 1L;
		final var invoiceEntity1 = new InvoiceEntity().withId(id).withInvoiceId("INVOICE_ID_1").withContent(xml);
		when(invoiceRepositoryMock.findById(id)).thenReturn(Optional.of(invoiceEntity1));

		// Act
		service.sendInvoiceToInvoiceCache(id);

		// Assert
		verify(invoiceRepositoryMock).findById(id);
		verify(invoiceRepositoryMock).save(invoiceEntityCaptor.capture());
		verifyNoInteractions(invoiceCacheClientMock);
		final var capturedInvoiceEntities = invoiceEntityCaptor.getAllValues();
		assertThat(capturedInvoiceEntities).hasSize(1);
		assertThat(capturedInvoiceEntities.getFirst().getId()).isEqualTo(id);
		assertThat(capturedInvoiceEntities.getFirst().getInvoiceId()).isEqualTo("INVOICE_ID_1");
		assertThat(capturedInvoiceEntities.getFirst().getContent()).isNotNull();
		assertThat(capturedInvoiceEntities.getFirst().getStatus()).isEqualTo(EXPORT_FAILED);
		assertThat(capturedInvoiceEntities.getFirst().getStatusMessage()).isEqualTo("OriginalInvoice or attachments not found in invoice with invoiceId: 683288");
	}
}
