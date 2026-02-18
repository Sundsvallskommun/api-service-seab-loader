package se.sundsvall.seabloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.seabloader.scheduler.dbcleaner.DatabaseCleanerScheduler;
import se.sundsvall.seabloader.scheduler.invoiceexporter.InvoiceExportScheduler;
import se.sundsvall.seabloader.scheduler.notifier.NotifierScheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AsyncExecutorServiceTest {

	@Mock
	private InvoiceExportScheduler invoiceExportSchedulerMock;

	@Mock
	private NotifierScheduler notifierSchedulerMock;

	@Mock
	private DatabaseCleanerScheduler databaseCleanerSchedulerMock;

	@InjectMocks
	private AsyncExecutorService asyncExecutorService;

	@Test
	void databaseCleanerExecute() {

		// Act
		asyncExecutorService.databaseCleanerExecute();

		// Verification
		verify(databaseCleanerSchedulerMock).execute();
		verifyNoInteractions(notifierSchedulerMock);
		verifyNoInteractions(invoiceExportSchedulerMock);
	}

	@Test
	void invoiceExportExecute() {

		// Act
		asyncExecutorService.invoiceExportExecute();

		verify(invoiceExportSchedulerMock).execute();
		verifyNoInteractions(notifierSchedulerMock);
		verifyNoInteractions(databaseCleanerSchedulerMock);
	}

	@Test
	void notifierExecute() {

		// Act
		asyncExecutorService.notifierExecute();

		verify(notifierSchedulerMock).execute();
		verifyNoInteractions(invoiceExportSchedulerMock);
		verifyNoInteractions(databaseCleanerSchedulerMock);
	}
}
