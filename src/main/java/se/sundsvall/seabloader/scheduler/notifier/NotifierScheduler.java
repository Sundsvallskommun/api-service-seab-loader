package se.sundsvall.seabloader.scheduler.notifier;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.NotifierService;

@Service
@ConfigurationProperties("scheduler.notifier.cron")
public class NotifierScheduler extends AbstractScheduler {

	private final NotifierService notifierService;

	public NotifierScheduler(final NotifierService notifierService) {
		this.notifierService = notifierService;
	}

	@Override
	@Dept44Scheduled(
		cron = "${scheduler.notifier.cron.expression}",
		name = "${scheduler.notifier.name}",
		lockAtMostFor = "${scheduler.notifier.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.notifier.maximum-execution-time}")
	public void execute() {
		notifierService.sendFailureNotification();
	}
}
