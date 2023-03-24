package se.sundsvall.seabloader.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInExchangeInvoice;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoiceEntity;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoicePdfRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

@Service
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);
	private static final Status[] STATUSES_OF_INVOICES_TO_SEND = { UNPROCESSED, EXPORT_FAILED };
	private static final int SLEEP_TIME_AFTER_SEND_ERROR_IN_MS = 500;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private InvoicePdfMerger invoicePdfMerger;

	@Autowired
	private InvoiceCacheClient invoiceCacheClient;

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
			final var inExchangeInvoice = toInExchangeInvoice(invoiceEntity.getContent());
			invoiceCacheClient.sendInvoice(toInvoicePdfRequest(inExchangeInvoice, invoicePdfMerger.mergePdfs(inExchangeInvoice)));
			invoiceRepository.save(invoiceEntity.withStatus(PROCESSED));
		} catch (final Exception e) {
			LOGGER.error("Error when sending invoice with id: {}. Message: {}", id, e.getMessage());
			invoiceRepository.save(invoiceEntity
				.withStatus(EXPORT_FAILED)
				.withStatusMessage(e.getMessage()));

			try {
				Thread.sleep(SLEEP_TIME_AFTER_SEND_ERROR_IN_MS);
			} catch (final InterruptedException e1) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
