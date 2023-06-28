package se.sundsvall.seabloader.batchimport.integration.datawarehousereader;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.seabloader.batchimport.integration.datawarehousereader.configuration.DataWarehouseReaderConfiguration.CLIENT_REGISTRATION_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import generated.se.sundsvall.datawarehousereader.CustomerEngagementResponse;
import se.sundsvall.seabloader.batchimport.integration.datawarehousereader.configuration.DataWarehouseReaderConfiguration;

@FeignClient(name = CLIENT_REGISTRATION_ID, url = "${integration.datawarehousereader.url}", configuration = DataWarehouseReaderConfiguration.class)
public interface DataWarehouseReaderClient { // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import

	@GetMapping(path = "customer/engagements", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	CustomerEngagementResponse getCustomerEngagement(@RequestParam(value = "customerNumber") String customerNumber);
}
