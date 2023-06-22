package se.sundsvall.seabloader.batchimport.mapper;

import static java.lang.String.format;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.STRALFORS;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import se.sundsvall.seabloader.batchimport.model.StralforsFile;
import se.sundsvall.seabloader.batchimport.util.ImportUtility;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

public class StralforsMapper { // TODO: Remove after completion of Stralfors invoices import

	private static final Logger LOGGER = LoggerFactory.getLogger(StralforsMapper.class);
	private static final ObjectMapper OBJECT_MAPPER;

	static {
		OBJECT_MAPPER = new XmlMapper();
		OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	private StralforsMapper() {}

	public static InvoiceEntity toInvoiceEntity(StralforsFile file) throws JsonProcessingException {
		final var xmlContent = OBJECT_MAPPER.writeValueAsString(file);

		try {
			return InvoiceEntity.create()
				.withSource(STRALFORS)
				.withContent(xmlContent)
				.withInvoiceId(ImportUtility.extractLowestInvoiceNumber(file));

		} catch (final Exception e) {
			LOGGER.error("Error during deserialization of XML content", e);
			return InvoiceEntity.create()
				.withContent(xmlContent)
				.withStatus(IMPORT_FAILED)
				.withStatusMessage(format("Deserialization of received XML failed with message: %s", getRootCauseMessage(e)));
		}
	}

}
