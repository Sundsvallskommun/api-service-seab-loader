package se.sundsvall.seabloader.scheduler.invoiceexporter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.service.InvoiceService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvoiceExportSchedulerServiceTest {

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@Mock
	private InvoiceService invoiceServiceMock;
	@InjectMocks
	private InvoiceExportSchedulerService service;

	@Test
	void exportInvoices() {
		// Call.
		service.execute();

		// Verification.
		verify(invoiceServiceMock).exportInvoices();
	}
}
