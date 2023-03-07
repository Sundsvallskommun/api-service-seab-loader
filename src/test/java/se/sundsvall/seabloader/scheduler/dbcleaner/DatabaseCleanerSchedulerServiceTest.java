package se.sundsvall.seabloader.scheduler.dbcleaner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerSchedulerServiceTest {

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@InjectMocks
	private DatabaseCleanerSchedulerService service;

	@Test
	void executeWithEntitiesToRemove() {
		// Setup
		final var entitiesToRemove = List.of(InvoiceEntity.create(), InvoiceEntity.create());

		// Setup mocking
		when(invoiceRepositoryMock.countByStatusIn(Status.PROCESSED)).thenReturn(Integer.toUnsignedLong(entitiesToRemove.size()));
		when(invoiceRepositoryMock.findByStatusIn(Status.PROCESSED)).thenReturn(entitiesToRemove);

		// Call.
		service.execute();

		// Verification.
		verify(invoiceRepositoryMock).countByStatusIn(Status.PROCESSED);
		verify(invoiceRepositoryMock).findByStatusIn(Status.PROCESSED);
		verify(invoiceRepositoryMock).deleteAll(entitiesToRemove);
	}

	@Test
	void executeWithNoEntitiesToRemove() {
		// Call.
		service.execute();

		// Verification.
		verify(invoiceRepositoryMock).countByStatusIn(Status.PROCESSED);
		verify(invoiceRepositoryMock, never()).findByStatusIn(any());
		verify(invoiceRepositoryMock, never()).deleteAll(any());
	}
}
