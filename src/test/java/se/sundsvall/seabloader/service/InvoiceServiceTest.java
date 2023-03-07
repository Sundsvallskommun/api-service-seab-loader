package se.sundsvall.seabloader.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.FAILED;

import java.util.Locale;

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

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class InvoiceServiceTest {

	private static final String TEST_INVOICE_FILE = "files/invoice/invoice1.xml";
	private static final String TEST_FAULTY_INVOICE_FILE = "files/invoice/invoice2.xml";

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

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
}
