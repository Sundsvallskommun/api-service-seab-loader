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
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
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
		final var entityIdsToRemove = createInvoiceIds();

		// Setup mocking
		when(invoiceRepositoryMock.countByStatusIn(Status.PROCESSED)).thenReturn(Integer.toUnsignedLong(entityIdsToRemove.size()));
		when(invoiceRepositoryMock.findIdsByStatusIn(Status.PROCESSED)).thenReturn(entityIdsToRemove);

		// Call.
		service.execute();

		// Verification.
		verify(invoiceRepositoryMock).countByStatusIn(Status.PROCESSED);
		verify(invoiceRepositoryMock).findIdsByStatusIn(Status.PROCESSED);
		verify(invoiceRepositoryMock).deleteAllByIdInBatch(List.of(5L, 6L));
	}

	@Test
	void executeWithNoEntitiesToRemove() {
		// Call.
		service.execute();

		// Verification.
		verify(invoiceRepositoryMock).countByStatusIn(Status.PROCESSED);
		verify(invoiceRepositoryMock, never()).findIdsByStatusIn(any());
		verify(invoiceRepositoryMock, never()).deleteAllByIdInBatch(any());
	}

	private List<InvoiceId> createInvoiceIds() {
		return List.of(createInvoiceIdInstance(5L), createInvoiceIdInstance(6L));
	}

	private InvoiceId createInvoiceIdInstance(final long id) {
		return () -> id;
	}
}
