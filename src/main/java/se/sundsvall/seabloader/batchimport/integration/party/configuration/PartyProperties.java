package se.sundsvall.seabloader.batchimport.integration.party.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.party") // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import
public record PartyProperties(int connectTimeout, int readTimeout) {
}
