package se.sundsvall.seabloader.scheduler.invoiceexporter;

import static org.springframework.util.CollectionUtils.isEmpty;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.InvoiceService;

@Service
@ConfigurationProperties("scheduler.invoiceexporter.cron")
public class InvoiceExportScheduler extends AbstractScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceExportScheduler.class);

	private static final Status[] STATUSES_OF_INVOICES_TO_SEND = { UNPROCESSED, EXPORT_FAILED };

	private static final String LOG_SENDING_STARTED = "Beginning of sending invoices to Invoice-cache";
	private static final String LOG_SENDING_ENDED = "Sending of invoices to Invoice-cache has ended";

	private final InvoiceService invoiceService;
	private final InvoiceRepository invoiceRepository;

	public InvoiceExportScheduler(final InvoiceService invoiceService, final InvoiceRepository invoiceRepository) {
		this.invoiceService = invoiceService;
		this.invoiceRepository = invoiceRepository;
	}

	@Override
	@Scheduled(cron = "${scheduler.invoiceexporter.cron.expression}")
	@SchedulerLock(name = "exportinvoices", lockAtMostFor = "${scheduler.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_SENDING_STARTED);
		LOGGER.info("Exporting invoices to InvoiceCache");

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

		LOGGER.info(LOG_SENDING_ENDED);
	}
}
