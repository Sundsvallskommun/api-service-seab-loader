package se.sundsvall.seabloader.integration.invoicecache;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.seabloader.integration.invoicecache.configuration.InvoiceCacheConfiguration.CLIENT_REGISTRATION_ID;

import generated.se.sundsvall.invoicecache.InvoicePdfRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.seabloader.integration.invoicecache.configuration.InvoiceCacheConfiguration;

@FeignClient(name = CLIENT_REGISTRATION_ID, url = "${integration.invoicecache.url}", configuration = InvoiceCacheConfiguration.class)
@CircuitBreaker(name = CLIENT_REGISTRATION_ID)
public interface InvoiceCacheClient {

	/**
	 * Export invoice to invoice cache.
	 *
	 * @param municipalityId the municipality ID.
	 * @param invoicePdfRequest with attributes for export an invoice.
	 */
	@PostMapping(path = "/{municipalityId}/invoices", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	void sendInvoice(@PathVariable(name = "municipalityId") String municipalityId, @RequestBody InvoicePdfRequest invoicePdfRequest);

}
