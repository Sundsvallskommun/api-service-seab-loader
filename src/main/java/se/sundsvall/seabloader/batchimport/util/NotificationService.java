package se.sundsvall.seabloader.batchimport.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import se.sundsvall.seabloader.integration.messaging.MessagingClient;
import se.sundsvall.seabloader.scheduler.notifier.NotifierSchedulerService;

@Service
public class NotificationService { // TODO: Remove after completion of Stralfors invoices import

	private static final Logger LOGGER = LoggerFactory.getLogger(NotifierSchedulerService.class);
	private static final String LOG_MAIL_NOTIFICATION_DISABLED = "Mail notification disabled, returning. Enable this feature with: 'notification.mail.enabled=true'";

	@Value("${spring.application.name:}")
	private String applicationName;

	@Value("${spring.profiles.active:}")
	private String applicationEnvironment;

	@Value("${notification.mail.enabled:true}")
	private boolean mailNotificationEnabled;

	@Value("${notification.mail.recipient.address:}")
	private String mailRecipientAddress;

	@Value("${notification.mail.sender.address:}")
	private String mailSenderAddress;

	@Autowired
	private MessagingClient messagingClient;

	/**
	 * Sends notification to configured recipient.
	 */
	public void sendNotification(String subject, String message) {

		if (!mailNotificationEnabled) {
			LOGGER.info(LOG_MAIL_NOTIFICATION_DISABLED);
			return;
		}

		// Send mail
		sendMail(Optional.ofNullable(subject).orElse("No subject") + " (" + (isBlank(applicationEnvironment) ? "default" : applicationEnvironment) + ")", message);
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

}
