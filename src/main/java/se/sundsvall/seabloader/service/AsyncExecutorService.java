package se.sundsvall.seabloader.service;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private InvoiceExportSchedulerService invoiceExportSchedulerService;

	@Autowired
	private NotifierSchedulerService notifierSchedulerService;

	@Autowired
	private DatabaseCleanerSchedulerService databaseCleanerSchedulerService;

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
