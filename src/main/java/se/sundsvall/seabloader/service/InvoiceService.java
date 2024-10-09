package se.sundsvall.seabloader.service;

import static java.util.Objects.nonNull;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInExchangeInvoice;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoiceEntity;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoicePdfRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.invoicecache.InvoiceCacheClient;

@Service
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);

	private final InvoiceRepository invoiceRepository;

	private final InvoicePdfMerger invoicePdfMerger;

	private final InvoiceCacheClient invoiceCacheClient;

	public InvoiceService(final InvoiceRepository invoiceRepository, final InvoicePdfMerger invoicePdfMerger, final InvoiceCacheClient invoiceCacheClient) {
		this.invoiceRepository = invoiceRepository;
		this.invoicePdfMerger = invoicePdfMerger;
		this.invoiceCacheClient = invoiceCacheClient;
	}

	public void create(final String municipalityId, final byte[] content) {
		final var invoiceEntity = toInvoiceEntity(municipalityId, content);

		final var invoiceId = invoiceEntity.getInvoiceId();
		if (nonNull(invoiceId) && invoiceRepository.existsByMunicipalityIdAndInvoiceId(municipalityId, invoiceId)) {
			LOGGER.info("Invoice with invoiceId: '{}' already exists in database. Nothing will be persisted.", invoiceId);
			return;
		}

		invoiceRepository.save(invoiceEntity);
	}

	public void sendInvoiceToInvoiceCache(final long id) {
		invoiceRepository.findById(id).ifPresent(invoiceEntity -> {
			try {
				final var inExchangeInvoice = toInExchangeInvoice(invoiceEntity.getContent());
				invoiceCacheClient.sendInvoice(invoiceEntity.getMunicipalityId(), toInvoicePdfRequest(inExchangeInvoice, invoicePdfMerger.mergePdfs(inExchangeInvoice)));
				invoiceRepository.save(invoiceEntity.withStatus(PROCESSED));
			} catch (final Exception e) {
				LOGGER.error("Error when sending invoice with id: {}. Message: {}", id, e.getMessage());
				invoiceRepository.save(invoiceEntity
					.withStatus(EXPORT_FAILED)
					.withStatusMessage(e.getMessage()));
			}
		});
	}
}
