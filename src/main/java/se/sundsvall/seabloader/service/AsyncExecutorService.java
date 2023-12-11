package se.sundsvall.seabloader.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import se.sundsvall.seabloader.scheduler.dbcleaner.DatabaseCleanerSchedulerService;
import se.sundsvall.seabloader.scheduler.invoiceexporter.InvoiceExportSchedulerService;
import se.sundsvall.seabloader.scheduler.notifier.NotifierSchedulerService;

/**
 * Class responsible for async-execution of the *SchedulerServices.
 *
 * The purpose with this is to detach the execution from the calling thread
 * when the call is initialized from the REST-API.
 */
@Service
public class AsyncExecutorService {

	private final InvoiceExportSchedulerService invoiceExportSchedulerService;
	private final NotifierSchedulerService notifierSchedulerService;
	private final DatabaseCleanerSchedulerService databaseCleanerSchedulerService;

	public AsyncExecutorService(
		InvoiceExportSchedulerService invoiceExportSchedulerService,
		NotifierSchedulerService notifierSchedulerService,
		DatabaseCleanerSchedulerService databaseCleanerSchedulerService) {

		this.invoiceExportSchedulerService = invoiceExportSchedulerService;
		this.notifierSchedulerService = notifierSchedulerService;
		this.databaseCleanerSchedulerService = databaseCleanerSchedulerService;
	}

	@Async
	public void invoiceExportExecute() {
		invoiceExportSchedulerService.execute();
	}

	@Async
	public void notifierExecute() {
		notifierSchedulerService.execute();
	}

	@Async
	public void databaseCleanerExecute() {
		databaseCleanerSchedulerService.execute();
	}
}
