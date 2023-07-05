package se.sundsvall.seabloader.batchimport.mapper;

import static java.lang.String.format;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.STRALFORS;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import generated.se.sundsvall.invoicecache.InvoicePdf;
import generated.se.sundsvall.invoicecache.InvoicePdfRequest;
import se.sundsvall.seabloader.batchimport.model.StralforsFile;
import se.sundsvall.seabloader.batchimport.model.XmlRoot;
import se.sundsvall.seabloader.batchimport.util.ImportUtility;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.service.InvoicePdfMerger;
import se.sundsvall.seabloader.service.mapper.model.InvoiceType;

public class StralforsMapper { // TODO: Remove after completion of Stralfors invoices import

	private static final Logger LOGGER = LoggerFactory.getLogger(StralforsMapper.class);
	private static final ObjectMapper OBJECT_MAPPER;

	static {
		OBJECT_MAPPER = new XmlMapper();
		OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private StralforsMapper() {}

	public static XmlRoot toXmlRoot(String input) throws JsonProcessingException {
		return OBJECT_MAPPER.readValue(input, XmlRoot.class);
	}

	public static StralforsFile toStralforsFile(String input) throws JsonProcessingException {
		return OBJECT_MAPPER.readValue(input, StralforsFile.class);
	}

	public static InvoicePdfRequest toInvoicePdfRequest(final StralforsFile file, String issuerLegalId) {
		return new InvoicePdfRequest()
			.invoiceNumber(ImportUtility.extractLowestInvoiceNumber(file))
			.invoiceId(FilenameUtils.removeExtension(file.getName()))
			.invoiceType(InvoicePdfRequest.InvoiceTypeEnum.valueOf(InvoiceType.fromValue(ImportUtility.getEntryValue(file, ImportUtility.ENTRY_KEY_INVOICE_TYPE)).toString()))
			.issuerLegalId(issuerLegalId)
			.debtorLegalId("5564786647") // Debtor is always Sundsvall Energi AB (5564786647)
			.attachment(toInvoicePdf(file));
	}

	private static InvoicePdf toInvoicePdf(final StralforsFile file) {

		if (Objects.isNull(file.getPdf())) {
			final var invoiceId = FilenameUtils.removeExtension(file.getName());
			LOGGER.error("Pdf data not found for Stralfors invoice with invoiceId: {}", invoiceId);
			throw new IllegalArgumentException(format("Pdf data not found for invoice with invoiceId: %s", invoiceId));
		}

		return new InvoicePdf()
			.content(InvoicePdfMerger.compress(file.getPdf()))
			.name(file.getName());
	}

	public static InvoiceEntity toInvoiceEntity(StralforsFile file) {
		try {
			return InvoiceEntity.create()
				.withSource(STRALFORS)
				.withContent(OBJECT_MAPPER.writeValueAsString(file))
				.withInvoiceId(FilenameUtils.removeExtension(file.getName()));

		} catch (final Exception e) {
			LOGGER.error("Error during deserialization of XML content", e);
			return InvoiceEntity.create()
				.withContent("See descriptor file for content of stralfors file record " + file.getName())
				.withStatus(IMPORT_FAILED)
				.withStatusMessage(format("Deserialization of received XML failed with message: %s", getRootCauseMessage(e)));
		}
	}

}
