package se.sundsvall.seabloader.service;

import static java.util.stream.LongStream.rangeClosed;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerServiceTest {

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@InjectMocks
	private DatabaseCleanerService service;

	@Test
	void executeWithEntitiesToRemove() {

		// Arrange
		final var entityIdsToRemove = createInvoiceIds(22); // Will be divided into three "delete chunks" (since the chunk size = 10).

		when(invoiceRepositoryMock.countByStatusIn(PROCESSED)).thenReturn(Integer.toUnsignedLong(entityIdsToRemove.size()));
		when(invoiceRepositoryMock.findIdsByStatusIn(PROCESSED)).thenReturn(entityIdsToRemove);

		// Act
		service.cleanDatabase();

		// Assert
		verify(invoiceRepositoryMock).countByStatusIn(PROCESSED);
		verify(invoiceRepositoryMock).findIdsByStatusIn(PROCESSED);
		verify(invoiceRepositoryMock).optimizeTable();
		// Verify chunk deletion (3 chunks)
		verify(invoiceRepositoryMock, times(3)).flush();
		verify(invoiceRepositoryMock).deleteAllByIdInBatch(rangeClosed(0, 9).boxed().toList());
		verify(invoiceRepositoryMock).deleteAllByIdInBatch(rangeClosed(10, 19).boxed().toList());
		verify(invoiceRepositoryMock).deleteAllByIdInBatch(rangeClosed(20, 21).boxed().toList());
	}

	@Test
	void executeWithNoEntitiesToRemove() {

		// Act
		service.cleanDatabase();

		// Assert
		verify(invoiceRepositoryMock).countByStatusIn(PROCESSED);
		verify(invoiceRepositoryMock, never()).flush();
		verify(invoiceRepositoryMock, never()).findIdsByStatusIn(any());
		verify(invoiceRepositoryMock, never()).deleteAllByIdInBatch(any());
		verify(invoiceRepositoryMock, never()).optimizeTable();
	}

	private List<InvoiceId> createInvoiceIds(final int numberOfInvoiceIdsToCreate) {
		return IntStream.range(0, numberOfInvoiceIdsToCreate)
			.mapToObj(this::createInvoiceIdInstance)
			.toList();
	}

	private InvoiceId createInvoiceIdInstance(final long id) {
		return () -> id;
	}
}
