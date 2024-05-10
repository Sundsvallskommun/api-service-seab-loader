package se.sundsvall.seabloader.scheduler.invoiceexporter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.seabloader.service.InvoiceService;

@ExtendWith(MockitoExtension.class)
class InvoiceExportSchedulerTest {

	@Mock
	private InvoiceService invoiceServiceMock;

	@InjectMocks
	private InvoiceExportScheduler service;

	@Test
	void exportInvoices() {
		service.execute();
		verify(invoiceServiceMock).exportInvoices();
		verifyNoMoreInteractions(invoiceServiceMock);
	}
}
