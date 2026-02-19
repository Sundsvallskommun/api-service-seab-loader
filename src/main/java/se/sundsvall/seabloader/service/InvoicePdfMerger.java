package se.sundsvall.seabloader.service;

import generated.se.inexchange.InExchangeInvoiceStatusType;
import generated.se.inexchange.InExchangeInvoiceStatusTypeAttachment;
import generated.se.inexchange.InExchangeInvoiceStatusTypeAttachment.Attachment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang3.function.Failable;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.openpdf.text.Document;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSmartCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.pdfbox.io.RandomAccessReadBuffer.createBufferFromStream;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

@Component
public class InvoicePdfMerger {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoicePdfMerger.class);

	private static final String MERGE_ERROR_MESSAGE = "A problem occured during merge of PDF:s. %s.";

	public OutputStream mergePdfs(InExchangeInvoiceStatusType inExchangeInvoice) {
		try {
			final var merger = initializePDFMergerUtility();
			final var originalInvoice = toInputStream(inExchangeInvoice.getOriginalInvoice().getValue());

			merger.addSource(createBufferFromStream(originalInvoice));

			Failable.stream(toInputStreams(inExchangeInvoice.getAttachments()))
				.forEach(inputStream -> merger.addSource(createBufferFromStream(inputStream)));

			merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
			return compress((ByteArrayOutputStream) merger.getDestinationStream());

		} catch (final Exception e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, MERGE_ERROR_MESSAGE.formatted(e.getMessage()));
		}
	}

	private PDFMergerUtility initializePDFMergerUtility() {
		final var merger = new PDFMergerUtility();
		merger.setDestinationStream(new ByteArrayOutputStream());
		return merger;
	}

	private List<ByteArrayInputStream> toInputStreams(InExchangeInvoiceStatusTypeAttachment attachments) {
		return ofNullable(attachments).orElse(new InExchangeInvoiceStatusTypeAttachment())
			.getAttachment().stream()
			.map(Attachment::getValue)
			.map(this::toInputStream)
			.toList();
	}

	private ByteArrayInputStream toInputStream(String pdf) {
		return new ByteArrayInputStream(Base64.getDecoder().decode(pdf.getBytes(UTF_8)));
	}

	private static ByteArrayOutputStream compress(ByteArrayOutputStream outputStream) {
		if (isNull(outputStream)) {
			return null;
		}

		try (final var pdfReader = new PdfReader(outputStream.toByteArray());
			final var document = new Document()) {

			final var result = new ByteArrayOutputStream();
			final var pdfSmartCopy = new PdfSmartCopy(document, result);
			pdfSmartCopy.setFullCompression();
			document.open();

			for (int pageNumber = 1; pageNumber <= pdfReader.getNumberOfPages(); pageNumber++) {
				final var page = pdfSmartCopy.getImportedPage(pdfReader, pageNumber);
				pdfSmartCopy.addPage(page);
			}
			pdfSmartCopy.close();
			return result;

		} catch (final IOException e) {
			LOGGER.warn("A problem occured during compression of PDF. {}", e.getMessage());
			return outputStream;
		}
	}
}
