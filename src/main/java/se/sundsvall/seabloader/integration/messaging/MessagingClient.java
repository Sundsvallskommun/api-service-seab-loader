package se.sundsvall.seabloader.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.seabloader.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.MessageResult;
import generated.se.sundsvall.messaging.SmsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.seabloader.integration.messaging.configuration.MessagingConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.url}", configuration = MessagingConfiguration.class)
public interface MessagingClient {

	/**
	 * Send a single e-mail
	 *
	 * @param  municipalityId the municipality ID.
	 * @param  emailRequest   containing email information
	 * @return                response containing id for sent message
	 */
	@PostMapping(path = "/{municipalityId}/email", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendEmail(@PathVariable(name = "municipalityId") final String municipalityId, @RequestBody EmailRequest emailRequest);

	/**
	 * Send a single sms
	 *
	 * @param  municipalityId the municipality ID.
	 * @param  smsRequest     containing sms information
	 * @return                response containing id for sent message
	 */
	@PostMapping(path = "/{municipalityId}/sms", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendSms(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@RequestBody SmsRequest smsRequest);
}
