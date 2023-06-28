package se.sundsvall.seabloader.batchimport.integration.datawarehousereader.configuration;

import static java.util.List.of;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class DataWarehouseReaderConfiguration { // TODO: Remove all logic regarding Stralfors invoices after completion of Stralfors invoices import

	public static final String CLIENT_REGISTRATION_ID = "datawarehousereader";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(ClientRegistrationRepository clientRepository, DataWarehouseReaderProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_REGISTRATION_ID, of(NOT_FOUND.value())))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRepository.findByRegistrationId(CLIENT_REGISTRATION_ID))
			.composeCustomizersToOne();
	}
}
