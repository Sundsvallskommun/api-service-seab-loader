package se.sundsvall.seabloader.service.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.inexchange.generated.InExchangeInvoiceStatusTypeAttachment.Attachment;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

import javax.xml.bind.JAXBException;
import java.util.Locale;

import static generated.se.sundsvall.invoicecache.InvoicePdfRequest.InvoiceTypeEnum.INVOICE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.FAILED;

import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import generated.se.inexchange.InExchangeInvoiceStatusTypeAttachment;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith(ResourceLoaderExtension.class)
class InvoiceMapperTest {

	private static final String TEST_INVOICE_FILE = "files/invoice/invoice1.xml";
	private static final String TEST_FAULTY_INVOICE_FILE = "files/invoice/invoice2.xml";

	private static final String TEST_INVOICE_FILE_WITH_ATTACHEMENTS = "files/pdfutility/invoice1.xml";
	private static final String TEST_INVOICE_FILE_WITHOUT_ATTACHEMENTS = "files/pdfutility/invoice2.xml";


	@BeforeAll
	static void setup() {
		Locale.setDefault(ENGLISH);
	}

	@Test
	void toInvoiceEntity(@Load(TEST_INVOICE_FILE) final String xml) {

		// Call
		final var result = InvoiceMapper.toInvoiceEntity(xml.getBytes(UTF_8));

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEqualTo(xml);
		assertThat(result.getInvoiceId()).isEqualTo("683288");
	}

	@Test
	void toInvoiceEntityWhenXmlIsFaulty(@Load(TEST_FAULTY_INVOICE_FILE) final String xml) {

		// Call
		final var result = InvoiceMapper.toInvoiceEntity(xml.getBytes(UTF_8));

		// Verification
		assertThat(result).isNotNull();

		assertThat(result.getContent()).isEqualTo(xml);
		assertThat(result.getInvoiceId()).isNull();
		assertThat(result.getStatus()).isEqualTo(FAILED);
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
	void toInvoicePdfRequest(@Load(TEST_INVOICE_FILE_WITH_ATTACHEMENTS) final String xml) throws Exception {
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);

		// Call
		final var result = InvoiceMapper.toInvoicePdfRequest(inExchangeInvoice);

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getInvoiceNumber()).isEqualTo("791932494");
		assertThat(result.getInvoiceId()).isEqualTo("683288");
		assertThat(result.getInvoiceType()).isEqualTo(INVOICE);
		assertThat(result.getInvoiceName()).isEqualTo("InvoiceImage_791932494_0.pdf");
		assertThat(result.getIssuerLegalId()).isEqualTo("5555555555");
		assertThat(result.getDebtorLegalId()).isEqualTo("666666-6666");
		assertThat(result.getAttachment().getName()).isEqualTo("791932494_1.pdf");
		assertThat(result.getAttachment().getContent()).hasSize(190132);
	}

	@Test
	void toInvoicePdfRequestWithoutAttachments(@Load(TEST_INVOICE_FILE_WITHOUT_ATTACHEMENTS) final String xml) throws Exception {
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);

		// Call
		final var result = InvoiceMapper.toInvoicePdfRequest(inExchangeInvoice);

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getInvoiceNumber()).isEqualTo("791932494");
		assertThat(result.getInvoiceId()).isEqualTo("683288");
		assertThat(result.getInvoiceType()).isEqualTo(INVOICE);
		assertThat(result.getInvoiceName()).isEqualTo("InvoiceImage_791932494_0.pdf");
		assertThat(result.getIssuerLegalId()).isEqualTo("5555555555");
		assertThat(result.getDebtorLegalId()).isEqualTo("666666-6666");
		assertThat(result.getAttachment()).isNull();
	}

	@Test
	void toInExchangeInvoiceWhenExceptionOccurs(@Load(TEST_FAULTY_INVOICE_FILE) final String xml) {

		// Call
		final var exception = assertThrows(JAXBException.class, () -> InvoiceMapper.toInExchangeInvoice(xml));

		// Verification
		assertThat(getRootCauseMessage(exception)).isEqualTo("SAXParseException: Premature end of file.");
	}
}
