package se.sundsvall.seabloader.integration.invoicecache.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.invoicecache")
public record InvoiceCacheProperties(int connectTimeout, int readTimeout) {
}
