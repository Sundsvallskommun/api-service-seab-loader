package se.sundsvall.seabloader.scheduler.invoiceexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.InvoiceService;

import static org.springframework.util.CollectionUtils.isEmpty;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

@Service
@ConfigurationProperties("scheduler.invoiceexporter.cron")
public class InvoiceExportScheduler extends AbstractScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceExportScheduler.class);

	private static final Status[] STATUSES_OF_INVOICES_TO_SEND = {
		UNPROCESSED, EXPORT_FAILED
	};

	private final InvoiceService invoiceService;
	private final InvoiceRepository invoiceRepository;

	public InvoiceExportScheduler(final InvoiceService invoiceService, final InvoiceRepository invoiceRepository) {
		this.invoiceService = invoiceService;
		this.invoiceRepository = invoiceRepository;
	}

	@Override
	@Dept44Scheduled(
		cron = "${scheduler.invoiceexporter.cron.expression}",
		name = "${scheduler.invoiceexporter.name}",
		lockAtMostFor = "${scheduler.invoiceexporter.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.invoiceexporter.maximum-execution-time}")
	public void execute() {

		final var invoiceIdsToSend = invoiceRepository.findIdsByStatusIn(STATUSES_OF_INVOICES_TO_SEND);

		if (isEmpty(invoiceIdsToSend)) {
			LOGGER.info("No invoices found with status {}", (Object[]) STATUSES_OF_INVOICES_TO_SEND);
			return;
		}

		LOGGER.info("Found {} invoices with status {}", invoiceIdsToSend.size(), STATUSES_OF_INVOICES_TO_SEND);

		// Send invoices.
		invoiceIdsToSend.stream()
			.map(InvoiceId::getId)
			.forEach(invoiceService::sendInvoiceToInvoiceCache);

	}
}
