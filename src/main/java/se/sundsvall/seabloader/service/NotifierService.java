package se.sundsvall.seabloader.service;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.integration.messaging.MessagingClient;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;

@Service
public class NotifierService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotifierService.class);
	private static final String LOG_MAIL_NOTIFICATION_DISABLED = "Mail notification disabled, returning. Enable this feature with: 'notification.mail.enabled=true'";
	private static final String NOTIFICATION_SUBJECT = "Failed records discovered in %s (%s)";
	private static final String NOTIFICATION_BODY_INTRODUCTION = "Failed record(s) exist in %s-database! \n";
	private static final String NOTIFICATION_BODY_ROW = "\n%-20s\t%s records";

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

	private final InvoiceRepository invoiceRepository;
	private final MessagingClient messagingClient;

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

		// Collect data
		final var failedStatusMap = new EnumMap<Status, Long>(Status.class);
		Stream.of(Status.values()).forEach(status -> failedStatusMap.put(status, invoiceRepository.countByStatusIn(status)));

		// Send notification (if send conditions are met)
		if (matchesSendFailureNotificationCondition(failedStatusMap)) {

			// Format message
			final var subject = format(NOTIFICATION_SUBJECT, applicationName, applicationEnvironment);
			final var message = new StringBuilder().append(format(NOTIFICATION_BODY_INTRODUCTION, applicationEnvironment));
			failedStatusMap.entrySet()
				.forEach(statusEntry -> message.append(format(NOTIFICATION_BODY_ROW, statusEntry.getKey(), statusEntry.getValue())));

			// Send mail.
			sendMail(subject, message.toString());
		}
	}

	private void sendMail(final String subject, final String message) {
		messagingClient.sendEmail(createEmailMessage(subject, message));
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
	 * @param statusMap the statusMap to check
	 * @return true if condition is met, false otherwise.
	 */
	private boolean matchesSendFailureNotificationCondition(final EnumMap<Status, Long> statusMap) {
		if (isEmpty(statusMap)) {
			return false;
		}

		return statusMap.entrySet().stream()
			.filter(entry -> Arrays.asList(IMPORT_FAILED, EXPORT_FAILED).contains(entry.getKey()))
			.anyMatch(entry -> entry.getValue() > 0);
	}
}
