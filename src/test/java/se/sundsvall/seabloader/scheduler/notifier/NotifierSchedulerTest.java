package se.sundsvall.seabloader.scheduler.notifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.seabloader.service.NotifierService;

@ExtendWith(MockitoExtension.class)
class NotifierSchedulerTest {

	@Mock
	private NotifierService notifierService;

	@InjectMocks
	private NotifierScheduler service;

	@Test
	void exportInvoices() {

		// Act
		service.execute();

		// Assert
		verify(notifierService).sendFailureNotification();
		verifyNoMoreInteractions(notifierService);
	}
}
