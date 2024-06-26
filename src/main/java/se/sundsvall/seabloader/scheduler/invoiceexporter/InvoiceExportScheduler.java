package se.sundsvall.seabloader.scheduler.invoiceexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.InvoiceService;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service
@ConfigurationProperties("scheduler.invoiceexporter.cron")
public class InvoiceExportScheduler extends AbstractScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceExportScheduler.class);
	private static final String LOG_SENDING_STARTED = "Beginning of sending invoices to Invoice-cache";
	private static final String LOG_SENDING_ENDED = "Sending of invoices to Invoice-cache has ended";

	private final InvoiceService invoiceService;

	public InvoiceExportScheduler(final InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Override
	@Scheduled(cron = "${scheduler.invoiceexporter.cron.expression}")
	@SchedulerLock(name = "exportinvoices", lockAtMostFor = "${scheduler.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_SENDING_STARTED);
		invoiceService.exportInvoices();
		LOGGER.info(LOG_SENDING_ENDED);
	}
}
