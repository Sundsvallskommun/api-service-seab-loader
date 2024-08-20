package se.sundsvall.seabloader.service.mapper;

import static generated.se.sundsvall.invoicecache.InvoicePdfRequest.InvoiceTypeEnum.INVOICE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Locale;

import jakarta.xml.bind.UnmarshalException;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

import generated.se.inexchange.InExchangeInvoiceStatusTypeAttachment.Attachment;

@ExtendWith(ResourceLoaderExtension.class)
class InvoiceMapperTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String TEST_INVOICE_FILE = "files/invoice/invoice1.xml";
	private static final String TEST_FAULTY_INVOICE_FILE = "files/invoice/invoice2.xml";

	private static final String TEST_INVOICE_FILE_WITH_ATTACHMENTS = "files/pdfutility/invoice1.xml";
	private static final String TEST_INVOICE_FILE_WITHOUT_ATTACHMENTS = "files/pdfutility/invoice2.xml";

	@BeforeAll
	static void setup() {
		Locale.setDefault(ENGLISH);
	}

	@Test
	void toInvoiceEntity(@Load(TEST_INVOICE_FILE) final String xml) {

		// Call
		final var result = InvoiceMapper.toInvoiceEntity(MUNICIPALITY_ID, xml.getBytes(UTF_8));

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getContent()).isEqualTo(xml);
		assertThat(result.getInvoiceId()).isEqualTo("683288");
	}

	@Test
	void toInvoiceEntityWhenXmlIsFaulty(@Load(TEST_FAULTY_INVOICE_FILE) final String xml) {

		// Call
		final var result = InvoiceMapper.toInvoiceEntity(MUNICIPALITY_ID, xml.getBytes(UTF_8));

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getContent()).isEqualTo(xml);
		assertThat(result.getInvoiceId()).isNull();
		assertThat(result.getStatus()).isEqualTo(IMPORT_FAILED);
		assertThat(result.getStatusMessage())
			.isEqualTo("Deserialization of received XML failed with message: SAXParseException: Premature end of file.");
	}

	@Test
	void toInExchangeInvoice(@Load(TEST_INVOICE_FILE) final String xml) throws Exception {

		// Call
		final var result = InvoiceMapper.toInExchangeInvoice(xml);

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getInvoice()).isNotNull();
		assertThat(result.getInvoice().getInvoiceId()).isEqualTo(683288);
		assertThat(result.getInvoice().getInvoiceNo()).isEqualTo("791932494");
		assertThat(result.getInvoice().getInternalTag().getValue()).isEqualTo("Faktura");
		assertThat(result.getInvoice().getCustomerNo()).isEqualTo("66666666");
		assertThat(result.getInvoice().getBuyerParty().getOrgNo()).isEqualTo("666666-6666");
		assertThat(result.getAttachments()).isNotNull();
		assertThat(result.getAttachments().getAttachment())
			.extracting(Attachment::getValue, Attachment::getName)
			.contains(tuple("JVBERxxxxxx", "791932494_1.pdf"));
	}

	@Test
	void toInvoicePdfRequest(@Load(TEST_INVOICE_FILE_WITH_ATTACHMENTS) final String xml) throws Exception {
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);

		final var outputStream = new ByteArrayOutputStream();

		outputStream.writeBytes("JVBERxxxxxx".getBytes());
		// Call
		final var result = InvoiceMapper.toInvoicePdfRequest(inExchangeInvoice, outputStream);

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getInvoiceNumber()).isEqualTo("791932494");
		assertThat(result.getInvoiceId()).isEqualTo("683288");
		assertThat(result.getInvoiceType()).isEqualTo(INVOICE);
		assertThat(result.getIssuerLegalId()).isEqualTo("5555555555");
		assertThat(result.getDebtorLegalId()).isEqualTo("666666-6666");
		assertThat(result.getAttachment().getName()).isEqualTo("683288.pdf");
		assertThat(Base64.getDecoder().decode(result.getAttachment().getContent())).isEqualTo("JVBERxxxxxx".getBytes(UTF_8));
	}

	@Test
	void toInvoicePdfRequestWithoutAttachments(@Load(TEST_INVOICE_FILE_WITHOUT_ATTACHMENTS) final String xml) throws Exception {
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);

		// Call
		final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> InvoiceMapper.toInvoicePdfRequest(inExchangeInvoice, null));

		AssertionsForClassTypes.assertThat(e.getMessage()).isEqualTo("OriginalInvoice or attachments not found in invoice with invoiceId: 683288");// Verification
	}

	@Test
	void toInExchangeInvoiceWhenExceptionOccurs(@Load(TEST_FAULTY_INVOICE_FILE) final String xml) {

		// Call
		final var exception = assertThrows(UnmarshalException.class, () -> InvoiceMapper.toInExchangeInvoice(xml));

		// Verification
		assertThat(getRootCauseMessage(exception)).isEqualTo("SAXParseException: Premature end of file.");
	}
}
