package se.sundsvall.seabloader.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import se.sundsvall.seabloader.scheduler.dbcleaner.DatabaseCleanerScheduler;
import se.sundsvall.seabloader.scheduler.invoiceexporter.InvoiceExportScheduler;
import se.sundsvall.seabloader.scheduler.notifier.NotifierScheduler;

/**
 * Class responsible for async-execution of the *SchedulerServices.
 * <p>
 * The purpose with this is to detach the execution from the calling thread
 * when the call is initialized from the REST-API.
 */
@Service
public class AsyncExecutorService {

	private final InvoiceExportScheduler invoiceExportScheduler;
	private final NotifierScheduler notifierScheduler;
	private final DatabaseCleanerScheduler databaseCleanerScheduler;

	public AsyncExecutorService(
		InvoiceExportScheduler invoiceExportScheduler,
		NotifierScheduler notifierScheduler,
		DatabaseCleanerScheduler databaseCleanerScheduler) {

		this.invoiceExportScheduler = invoiceExportScheduler;
		this.notifierScheduler = notifierScheduler;
		this.databaseCleanerScheduler = databaseCleanerScheduler;
	}

	@Async
	public void invoiceExportExecute() {
		invoiceExportScheduler.execute();
	}

	@Async
	public void notifierExecute() {
		notifierScheduler.execute();
	}

	@Async
	public void databaseCleanerExecute() {
		databaseCleanerScheduler.execute();
	}
}
