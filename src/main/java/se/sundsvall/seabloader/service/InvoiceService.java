package se.sundsvall.seabloader.service;

import static java.util.Objects.nonNull;
import static se.sundsvall.seabloader.service.mapper.InvoiceMapper.toInvoiceEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;

@Service
public class InvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);

	@Autowired
	private InvoiceRepository invoiceRepository;

	public void create(final byte[] content) {
		final var invoiceEntity = toInvoiceEntity(content);

		final var invoiceId = invoiceEntity.getInvoiceId();
		if (nonNull(invoiceId) && invoiceRepository.existsByInvoiceId(invoiceId)) {
			LOGGER.info("Invoice with invoiceId: '{}' already exists in database. Nothing will be persisted.", invoiceId);
			return;
		}

		invoiceRepository.save(invoiceEntity);
	}
}
