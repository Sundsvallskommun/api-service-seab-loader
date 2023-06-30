package se.sundsvall.seabloader.batchimport.integration.party;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.seabloader.batchimport.integration.party.configuration.PartyConfiguration.CLIENT;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.party.PartyType;
import se.sundsvall.seabloader.batchimport.integration.party.configuration.PartyConfiguration;

@FeignClient(name = CLIENT, url = "${integration.party.url}", configuration = PartyConfiguration.class, dismiss404 = true)
public interface PartyClient { // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import

	/**
	 * Get legal-ID by partyId (personId or organizationId).
	 * 
	 * @param partyType the type of party.
	 * @param partyId   the ID of the party. I.e. the personId or organizationId.
	 * @return an optional string containing the legalId that corresponds to the provided partyType and partyId if found.
	 */
	@GetMapping(path = "/{type}/{partyId}/legalId", produces = { TEXT_PLAIN_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	Optional<String> getLegalId(@PathVariable("type") PartyType partyType, @PathVariable("partyId") String partyId);
}
