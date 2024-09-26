package se.sundsvall.seabloader.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.springframework.util.CollectionUtils.isEmpty;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.integration.messaging.MessagingClient;

@Service
public class NotifierService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotifierService.class);

	private static final String LOG_MAIL_NOTIFICATION_DISABLED = "Mail notification disabled, returning. Enable this feature with: 'notification.mail.enabled=true'";

	private static final String NOTIFICATION_SUBJECT = "Failed records discovered in %s (%s)";

	private static final String NOTIFICATION_BODY_INTRODUCTION = "Failed record(s) exist in %s-database! \n";

	private static final String NOTIFICATION_BODY_ROW = "\n%-20s\t%s records";

	private final InvoiceRepository invoiceRepository;

	private final MessagingClient messagingClient;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.profiles.active:}")
	private String applicationEnvironment;

	@Value("${notification.mail.enabled:true}")
	private boolean mailNotificationEnabled;

	@Value("${notification.mail.recipient.address}")
	private String mailRecipientAddress;

	@Value("${notification.mail.sender.address}")
	private String mailSenderAddress;

	public NotifierService(final InvoiceRepository invoiceRepository, final MessagingClient messagingClient) {
		this.invoiceRepository = invoiceRepository;
		this.messagingClient = messagingClient;
	}

	/**
	 * Sends failure notifications to configured recipient.
	 */
	public void sendFailureNotification() {

		if (!mailNotificationEnabled) {
			LOGGER.info(LOG_MAIL_NOTIFICATION_DISABLED);
			return;
		}

		final var failedStatusMap = invoiceRepository.findByStatusIn(Status.values())
			.stream()
			.collect(groupingBy(
				InvoiceEntity::getMunicipalityId,
				groupingBy(
					InvoiceEntity::getStatus,
					() -> new EnumMap<>(Status.class),
					counting())));

		// Initialize missing statuses to 0
		failedStatusMap.values().forEach(statusMap -> Arrays.stream(Status.values()).forEach(s -> statusMap.putIfAbsent(s, 0L)));

		// Send notification (if send conditions are met)
		if (matchesSendFailureNotificationCondition(failedStatusMap)) {
			failedStatusMap.forEach((municipalityId, statusMap) -> {
				// Format message
				final var subject = format(NOTIFICATION_SUBJECT, applicationName, applicationEnvironment);
				final var message = new StringBuilder().append(NOTIFICATION_BODY_INTRODUCTION.formatted(applicationEnvironment));
				statusMap.forEach((key, value) -> message.append(NOTIFICATION_BODY_ROW.formatted(key, value)));

				// Send mail.
				sendMail(subject, message.toString(), municipalityId);
			});
		}
	}

	private void sendMail(final String subject, final String message, final String municipalityId) {
		messagingClient.sendEmail(municipalityId, createEmailMessage(subject, message));
	}

	private EmailRequest createEmailMessage(final String subject, final String message) {
		return new EmailRequest()
			.sender(new EmailSender()
				.name(applicationName)
				.address(mailSenderAddress))
			.emailAddress(mailRecipientAddress)
			.subject(subject)
			.message(message);
	}

	/**
	 * The following criteria must be met for the sendFailureNotificationCondition:
	 * <p>
	 * - The status map is not empty
	 * - The status map contains one of the keys EXPORT_FAILED or IMPORT_FAILED with a positive entry-value.
	 *
	 * @param  statusMap the statusMap to check
	 * @return           true if condition is met, false otherwise.
	 */
	private boolean matchesSendFailureNotificationCondition(final Map<String, EnumMap<Status, Long>> statusMap) {
		if (isEmpty(statusMap)) {
			return false;
		}

		return statusMap.values().stream()
			.flatMap(failedStatusMap -> failedStatusMap.entrySet().stream())
			.filter(entry -> Arrays.asList(IMPORT_FAILED, EXPORT_FAILED).contains(entry.getKey()))
			.anyMatch(entry -> entry.getValue() > 0);
	}
}
