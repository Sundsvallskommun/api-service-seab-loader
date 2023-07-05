package se.sundsvall.seabloader.batchimport.integration.datawarehousereader.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.datawarehousereader")
public record DataWarehouseReaderProperties(int connectTimeout, int readTimeout) { // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import
}
