package se.sundsvall.seabloader.scheduler.invoiceexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.InvoiceService;

@Service
@ConfigurationProperties("scheduler.invoiceexporter.cron")
public class InvoiceExportSchedulerService extends AbstractScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceExportSchedulerService.class);
	private static final String LOG_SENDING_STARTED = "Beginning of sending invoices to Invoice-cache";
	private static final String LOG_SENDING_ENDED = "Sending of invoices to Invoice-cache has ended";

	private final InvoiceService invoiceService;

	public InvoiceExportSchedulerService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Override
	@Scheduled(cron = "${scheduler.invoiceexporter.cron.expression:-}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_SENDING_STARTED);
		invoiceService.exportInvoices();
		LOGGER.info(LOG_SENDING_ENDED);
	}
}
