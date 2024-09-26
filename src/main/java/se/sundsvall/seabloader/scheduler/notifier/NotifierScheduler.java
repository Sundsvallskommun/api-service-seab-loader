package se.sundsvall.seabloader.scheduler.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.NotifierService;

@Service
@ConfigurationProperties("scheduler.notifier.cron")
public class NotifierScheduler extends AbstractScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotifierScheduler.class);
	private static final String LOG_EXECUTE_STARTED = "Executing started";
	private static final String LOG_EXECUTE_ENDED = "Executing ended";

	private final NotifierService notifierService;

	public NotifierScheduler(final NotifierService notifierService) {
		this.notifierService = notifierService;
	}

	@Override
	@Scheduled(cron = "${scheduler.notifier.cron.expression}")
	@SchedulerLock(name = "sendnotifications", lockAtMostFor = "${scheduler.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_EXECUTE_STARTED);
		notifierService.sendFailureNotification();
		LOGGER.info(LOG_EXECUTE_ENDED);
	}
}
