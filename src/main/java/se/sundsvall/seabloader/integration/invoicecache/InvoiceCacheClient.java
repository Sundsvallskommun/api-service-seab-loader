package se.sundsvall.seabloader.integration.invoicecache;

import generated.se.sundsvall.invoicecache.InvoicePdfRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.seabloader.integration.invoicecache.configuration.InvoiceCacheConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.seabloader.integration.invoicecache.configuration.InvoiceCacheConfiguration.CLIENT_REGISTRATION_ID;

@FeignClient(name = CLIENT_REGISTRATION_ID, url = "${integration.invoicecache.url}", configuration = InvoiceCacheConfiguration.class)
public interface InvoiceCacheClient {

	/**
	 * Export invoice to invoice cache.
	 * 
	 * @param invoicePdfRequest with attributes for export an invoice.
	 */
	@PostMapping(path = "invoices", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> sendInvoice(@RequestBody InvoicePdfRequest invoicePdfRequest);
}
