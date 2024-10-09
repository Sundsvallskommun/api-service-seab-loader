package se.sundsvall.seabloader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.seabloader.service.mapper.InvoiceMapper;

@SpringBootTest(classes = InvoicePdfMerger.class)
@ActiveProfiles(value = "junit")
@ExtendWith(ResourceLoaderExtension.class)
class InvoicePdfMergerTest {

	private static final String TEST_INVOICE_FILE_WITH_ATTACHEMENTS = "files/pdfutility/invoice1.xml";
	private static final String TEST_INVOICE_FILE_WITHOUT_ATTACHEMENTS = "files/pdfutility/invoice2.xml";
	private static final String TEST_INVOICE_FILE_WITH_FAULTY_ORIGINAL_PDF = "files/pdfutility/invoice3.xml";

	@Autowired
	private InvoicePdfMerger pdfUtil;

	@Test
	void mergeWithAttachements(@Load(TEST_INVOICE_FILE_WITH_ATTACHEMENTS) final String xml) throws Exception {

		// Act
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);
		final var byteArrayOutput = pdfUtil.mergePdfs(inExchangeInvoice);
		final var pdfDocument = Loader.loadPDF(((ByteArrayOutputStream) byteArrayOutput).toByteArray());

		// Assert
		assertThat(byteArrayOutput).isNotNull();
		assertThat(pdfDocument.getNumberOfPages()).isEqualTo(3);
	}

	@Test
	void mergeWithoutAttachements(@Load(TEST_INVOICE_FILE_WITHOUT_ATTACHEMENTS) final String xml) throws Exception {

		// Arrange
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);

		// Act
		final var byteArrayOutput = pdfUtil.mergePdfs(inExchangeInvoice);
		final var pdfDocument = Loader.loadPDF(((ByteArrayOutputStream) byteArrayOutput).toByteArray());

		// Assert
		assertThat(byteArrayOutput).isNotNull();
		assertThat(pdfDocument.getNumberOfPages()).isEqualTo(1);
	}

	@Test
	void mergeFaultyInvoicePdf(@Load(TEST_INVOICE_FILE_WITH_FAULTY_ORIGINAL_PDF) final String xml) throws Exception {

		// Act
		final var inExchangeInvoice = InvoiceMapper.toInExchangeInvoice(xml);
		final var exception = assertThrows(ThrowableProblem.class, () -> pdfUtil.mergePdfs(inExchangeInvoice));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: A problem occured during merge of PDF:s. Input byte array has wrong 4-byte ending unit.");
	}
}
