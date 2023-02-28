package se.sundsvall.seabloader.service.mapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.FAILED;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inexchange.generated.InExchangeInvoiceStatusType;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

public class InvoiceMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceMapper.class);

	private InvoiceMapper() {}

	public static InvoiceEntity toInvoiceEntity(final byte[] fileContent) {
		final var xmlContent = new String(fileContent, UTF_8);

		try {
			final var inExchangeInvoice = toInExchangeInvoice(xmlContent);
			return InvoiceEntity.create()
				.withContent(xmlContent)
				.withInvoiceId(String.valueOf(inExchangeInvoice.getInvoice().getInvoiceId()));

		} catch (final Exception e) {
			LOGGER.error("Error during deserialization of XML content", e);
			return InvoiceEntity.create()
				.withContent(xmlContent)
				.withStatus(FAILED)
				.withStatusMessage(String.format("Deserialization of received XML failed with message: %s", getRootCauseMessage(e)));
		}
	}

	public static InExchangeInvoiceStatusType toInExchangeInvoice(final String xml) throws JAXBException {
		final var unmarshaller = JAXBContext.newInstance(InExchangeInvoiceStatusType.class).createUnmarshaller();
		return (InExchangeInvoiceStatusType) JAXBIntrospector.getValue(unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes(UTF_8))));
	}
}
