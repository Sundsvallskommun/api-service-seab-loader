package se.sundsvall.seabloader.service;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInExchangeInvoice;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoiceEntity;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoicePdfRequest;

@Service
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);
	private static final Status[] STATUSES_TO_SEND = {UNPROCESSED, FAILED};

	@Autowired
	private InvoiceRepository invoiceRepository;

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
		final var BATCH_SIZE = 10;
		LOGGER.info("Exporting invoices to Invoice-cache");

		final var invoiceIdsToSend = invoiceRepository.findInvoiceIdsByStatusIn(STATUSES_TO_SEND);

		if (invoiceIdsToSend.isEmpty()) {
			LOGGER.info("No invoices found with status {}", STATUSES_TO_SEND);
			return;
		}

		LOGGER.info("Found {} invoices with status {}", invoiceIdsToSend.size(), STATUSES_TO_SEND);

		new ArrayList<>(ListUtils
			.partition(new ArrayList<>(invoiceIdsToSend), BATCH_SIZE))
			.forEach(this::sendInvoiceToInvoiceCache);
	}

	private void sendInvoiceToInvoiceCache(final List<String> invoiceIds) {
		invoiceIds.forEach(invoiceId ->
			invoiceRepository.findByInvoiceId(invoiceId)
				.ifPresent(invoiceEntity -> {
					try {
						final var invoicePdfRequest = toInvoicePdfRequest(toInExchangeInvoice(invoiceEntity.getContent()));
						invoiceCacheClient.importInvoice(invoicePdfRequest);
						invoiceEntity.setStatus(PROCESSED);
						invoiceEntity.setContent(null);
						invoiceRepository.save(invoiceEntity);
						LOGGER.info("Invoice {} sent to Invoice-cache", invoicePdfRequest);
					} catch (Exception e) {
						LOGGER.error("Could not parse invoice with invoiceId: {} {}", invoiceId, e.getMessage());
						invoiceEntity.setStatus(FAILED);
						invoiceRepository.save(invoiceEntity);
					}
				}));
	}
}
