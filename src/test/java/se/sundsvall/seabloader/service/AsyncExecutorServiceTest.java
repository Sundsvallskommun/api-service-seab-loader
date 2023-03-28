package se.sundsvall.seabloader.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.seabloader.scheduler.dbcleaner.DatabaseCleanerSchedulerService;
import se.sundsvall.seabloader.scheduler.invoiceexporter.InvoiceExportSchedulerService;
import se.sundsvall.seabloader.scheduler.notifier.NotifierSchedulerService;

@ExtendWith(MockitoExtension.class)
class AsyncExecutorServiceTest {

	@Mock
	private InvoiceExportSchedulerService invoiceExportSchedulerServiceMock;

	@Mock
	private NotifierSchedulerService notifierSchedulerServiceMock;

	@Mock
	private DatabaseCleanerSchedulerService databaseCleanerSchedulerServiceMock;

	@InjectMocks
	private AsyncExecutorService asyncExecutorService;

	@Test
	void databaseCleanerExecute() {

		// Call
		asyncExecutorService.databaseCleanerExecute();

		// Verification
		verify(databaseCleanerSchedulerServiceMock).execute();
		verifyNoInteractions(notifierSchedulerServiceMock);
		verifyNoInteractions(invoiceExportSchedulerServiceMock);
	}

	@Test
	void invoiceExportExecute() {

		// Call
		asyncExecutorService.invoiceExportExecute();

		verify(invoiceExportSchedulerServiceMock).execute();
		verifyNoInteractions(notifierSchedulerServiceMock);
		verifyNoInteractions(databaseCleanerSchedulerServiceMock);
	}

	@Test
	void notifierExecute() {

		// Call
		asyncExecutorService.notifierExecute();

		verify(notifierSchedulerServiceMock).execute();
		verifyNoInteractions(invoiceExportSchedulerServiceMock);
		verifyNoInteractions(databaseCleanerSchedulerServiceMock);
	}
}
