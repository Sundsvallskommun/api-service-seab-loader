package se.sundsvall.seabloader.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.IN_EXCHANGE;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.STRALFORS;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInExchangeInvoice;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoiceEntity;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoicePdfRequest;

import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import generated.se.sundsvall.datawarehousereader.CustomerEngagement;
import generated.se.sundsvall.datawarehousereader.CustomerType;
import generated.se.sundsvall.party.PartyType;
import se.sundsvall.seabloader.batchimport.integration.datawarehousereader.DataWarehouseReaderClient;
import se.sundsvall.seabloader.batchimport.integration.party.PartyClient;
import se.sundsvall.seabloader.batchimport.mapper.StralforsMapper;
import se.sundsvall.seabloader.batchimport.util.ImportUtility;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

@Service
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);
	private static final Status[] STATUSES_OF_INVOICES_TO_SEND = { UNPROCESSED, EXPORT_FAILED };

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private InvoicePdfMerger invoicePdfMerger;

	@Autowired
	private InvoiceCacheClient invoiceCacheClient;

	@Autowired
	private PartyClient partyClient; // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import

	@Autowired
	private DataWarehouseReaderClient dwrClient; // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import

	public void create(final byte[] content) {
		final var invoiceEntity = toInvoiceEntity(content);

		final var invoiceId = invoiceEntity.getInvoiceId();
		if (nonNull(invoiceId) && invoiceRepository.existsByInvoiceId(invoiceId)) {
			LOGGER.info("Invoice with invoiceId: '{}' already exists in database. Nothing will be persisted.", invoiceId);
			return;
		}

		invoiceRepository.save(invoiceEntity);
	}

	public void exportInvoices() {
		LOGGER.info("Exporting invoices to InvoiceCache");

		final var invoiceIdsToSend = invoiceRepository.findIdsByStatusIn(STATUSES_OF_INVOICES_TO_SEND);

		if (invoiceIdsToSend.isEmpty()) {
			LOGGER.info("No invoices found with status {}", (Object[]) STATUSES_OF_INVOICES_TO_SEND);
			return;
		}

		LOGGER.info("Found {} invoices with status {}", invoiceIdsToSend.size(), STATUSES_OF_INVOICES_TO_SEND);

		// Send invoices.
		invoiceIdsToSend.stream()
			.map(InvoiceId::getId)
			.forEach(this::sendInvoiceToInvoiceCache);
	}

	private void sendInvoiceToInvoiceCache(final long id) {

		final var invoiceEntity = invoiceRepository.findById(id).orElse(null);

		if (isNull(invoiceEntity)) {
			LOGGER.error("No invoice with invoice with id:{} found. Do nothing.", id);
			return;
		}

		try {
			if (IN_EXCHANGE == invoiceEntity.getSource()) {
				final var inExchangeInvoice = toInExchangeInvoice(invoiceEntity.getContent());
				invoiceCacheClient.sendInvoice(toInvoicePdfRequest(inExchangeInvoice, invoicePdfMerger.mergePdfs(inExchangeInvoice)));
				invoiceRepository.save(invoiceEntity.withStatus(PROCESSED));
			}

			 // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import
			else if (STRALFORS == invoiceEntity.getSource()) {
				final var stralforsFile = StralforsMapper.toStralforsFile(invoiceEntity.getContent());
				invoiceCacheClient.sendInvoice(
					StralforsMapper.toInvoicePdfRequest(stralforsFile,
						translateToLegalId(
							ImportUtility.getEntryValue(stralforsFile, ImportUtility.ENTRY_KEY_CUSTOMER_NBR))));

				invoiceRepository.save(invoiceEntity.withStatus(PROCESSED));

				// Put to sleep 1ms
				hibernate();
			}

		} catch (final Exception e) {
			LOGGER.error("Error when sending invoice with id: {}. Message: {}", id, e.getMessage());
			invoiceRepository.save(invoiceEntity
				.withStatus(EXPORT_FAILED)
				.withStatusMessage(e.getMessage()));
		}
	}

	private void hibernate() {
		try {
			Thread.sleep(1);
		} catch (final InterruptedException e) { // NOSONAR
			LOGGER.info("Hibernation interrupted");
		}
	}

	private String translateToLegalId(String customerNbr) {
		final var engagements = dwrClient.getCustomerEngagement(customerNbr);

		return engagements.getCustomerEngagements().stream()
			.findAny()
			.map(this::fetchLegalId)
			.map(Optional::get)
			.orElse(null);
	}

	private Optional<String> fetchLegalId(CustomerEngagement customerEngagement) {
		return Stream.ofNullable(customerEngagement.getCustomerType())
			.map(this::toPartyType)
			.map(partyType -> partyClient.getLegalId(partyType, customerEngagement.getPartyId()))
			.findAny()
			.orElse(null);
	}

	private PartyType toPartyType(CustomerType customerType) {
		return switch (customerType) {
			case ENTERPRISE -> PartyType.ENTERPRISE;
			case PRIVATE -> PartyType.PRIVATE;
		};
	}
}
