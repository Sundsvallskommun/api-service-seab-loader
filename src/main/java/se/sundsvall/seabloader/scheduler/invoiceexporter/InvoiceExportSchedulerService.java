package se.sundsvall.seabloader.scheduler.invoiceexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.InvoiceService;

@Service
@ConfigurationProperties("scheduler.invoice.exporter.cron")
public class InvoiceExportSchedulerService extends AbstractScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceExportSchedulerService.class);
	private static final String LOG_SENDING_STARTED = "Beginning of sending invoices to Invoice-cache";
	private static final String LOG_SENDING_ENDED = "Sending of invoices to Invoice-cache has ended";

	@Autowired
	private InvoiceService invoiceService;

	@Override
	@Scheduled(cron = "${scheduler.invoice.exporter.cron.expression:-}")
	public void execute() {
		LOGGER.info(LOG_SENDING_STARTED);
		invoiceService.exportInvoices();
		LOGGER.info(LOG_SENDING_ENDED);
	}
}
