package se.sundsvall.seabloader.service.mapper;

import generated.se.inexchange.InExchangeInvoiceStatusType;
import generated.se.sundsvall.invoicecache.InvoicePdf;
import generated.se.sundsvall.invoicecache.InvoicePdfRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.service.mapper.model.InvoiceType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Base64;
import java.util.Objects;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.IN_EXCHANGE;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

public class InvoiceMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceMapper.class);

	private InvoiceMapper() {}

	public static InvoiceEntity toInvoiceEntity(final byte[] fileContent) {
		final var xmlContent = new String(fileContent, UTF_8);

		try {
			final var inExchangeInvoice = toInExchangeInvoice(xmlContent);
			return InvoiceEntity.create()
				.withSource(IN_EXCHANGE)
				.withContent(xmlContent)
				.withInvoiceId(String.valueOf(inExchangeInvoice.getInvoice().getInvoiceId()));

		} catch (final Exception e) {
			LOGGER.error("Error during deserialization of XML content", e);
			return InvoiceEntity.create()
				.withSource(IN_EXCHANGE)
				.withContent(xmlContent)
				.withStatus(IMPORT_FAILED)
				.withStatusMessage(format("Deserialization of received XML failed with message: %s", getRootCauseMessage(e)));
		}
	}

	public static InvoicePdfRequest toInvoicePdfRequest(final InExchangeInvoiceStatusType inExchangeInvoiceStatusType, final OutputStream outputStream) {
		return new InvoicePdfRequest()
			.invoiceNumber(inExchangeInvoiceStatusType.getInvoice().getInvoiceNo())
			.invoiceId(String.valueOf(inExchangeInvoiceStatusType.getInvoice().getInvoiceId()))
			.invoiceType(InvoicePdfRequest.InvoiceTypeEnum.valueOf(InvoiceType.fromValue(inExchangeInvoiceStatusType.getInvoice().getInternalTag().getValue()).toString()))
			.issuerLegalId(inExchangeInvoiceStatusType.getInvoice().getSellerParty().getOrgNo())
			.debtorLegalId(inExchangeInvoiceStatusType.getInvoice().getBuyerParty().getOrgNo())
			.attachment(toInvoicePdf(inExchangeInvoiceStatusType, outputStream));
	}

	public static InExchangeInvoiceStatusType toInExchangeInvoice(final String xml) throws SAXException, JAXBException, ParserConfigurationException {
		// Disable XXE.
		final var saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		// Do unmarshall operation.
		final var xmlSource = new SAXSource(saxParserFactory.newSAXParser().getXMLReader(), new InputSource(new StringReader(xml)));
		final var unmarshaller = JAXBContext.newInstance(InExchangeInvoiceStatusType.class).createUnmarshaller();
		return (InExchangeInvoiceStatusType) JAXBIntrospector.getValue(unmarshaller.unmarshal(xmlSource));
	}

	private static InvoicePdf toInvoicePdf(final InExchangeInvoiceStatusType inExchangeInvoiceStatusType, final OutputStream outputStream) {

		if (Objects.isNull(inExchangeInvoiceStatusType.getOriginalInvoice()) || Objects.isNull(outputStream)) {
			LOGGER.error("OriginalInvoice or attachments not found in invoice with invoiceId: {}", inExchangeInvoiceStatusType.getInvoice().getInvoiceId());
			throw new IllegalArgumentException(format("OriginalInvoice or attachments not found in invoice with invoiceId: %s", inExchangeInvoiceStatusType.getInvoice().getInvoiceId()));
		}

		return new InvoicePdf()
			.content(Base64.getEncoder().encodeToString(((ByteArrayOutputStream) outputStream).toByteArray()))
			.name(toInvoiceFileName(inExchangeInvoiceStatusType));
	}

	private static String toInvoiceFileName(final InExchangeInvoiceStatusType inExchangeInvoiceStatusType) {
		return inExchangeInvoiceStatusType.getInvoice().getInvoiceId() + ".pdf";
	}
}
