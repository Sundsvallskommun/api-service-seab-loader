package se.sundsvall.seabloader.scheduler.invoiceexporter;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.service.InvoiceService;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

@ExtendWith(MockitoExtension.class)
class InvoiceExportSchedulerTest {

	@Mock
	private InvoiceService invoiceServiceMock;

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@InjectMocks
	private InvoiceExportScheduler service;

	@Test
	void exportInvoices() {

		// Arrange
		final var invoiceId1 = mock(InvoiceId.class);
		final var invoiceId2 = mock(InvoiceId.class);
		final var invoiceId3 = mock(InvoiceId.class);
		final var invoiceIdList = List.of(invoiceId1, invoiceId2, invoiceId3);

		when(invoiceId1.getId()).thenReturn(1L);
		when(invoiceId2.getId()).thenReturn(2L);
		when(invoiceId3.getId()).thenReturn(3L);
		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(invoiceIdList);

		// Act
		service.execute();

		// Assert
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verify(invoiceServiceMock).sendInvoiceToInvoiceCache(1L);
		verify(invoiceServiceMock).sendInvoiceToInvoiceCache(2L);
		verify(invoiceServiceMock).sendInvoiceToInvoiceCache(3L);
		verifyNoMoreInteractions(invoiceServiceMock);
	}

	@Test
	void exportInvoicesWhnNothingToExport() {

		// Arrange
		when(invoiceRepositoryMock.findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED)).thenReturn(emptyList());

		// Act
		service.execute();

		// Assert
		verify(invoiceRepositoryMock).findIdsByStatusIn(UNPROCESSED, EXPORT_FAILED);
		verifyNoMoreInteractions(invoiceRepositoryMock);
		verifyNoInteractions(invoiceServiceMock);
	}
}
