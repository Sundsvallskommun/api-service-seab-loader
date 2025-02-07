package se.sundsvall.seabloader.scheduler.dbcleaner;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.DatabaseCleanerService;

@Service
@ConfigurationProperties("scheduler.dbcleaner.cron")
public class DatabaseCleanerScheduler extends AbstractScheduler {

	private final DatabaseCleanerService databaseCleanerService;

	public DatabaseCleanerScheduler(final DatabaseCleanerService databaseCleanerService) {
		this.databaseCleanerService = databaseCleanerService;
	}

	@Override
	@Dept44Scheduled(
		cron = "${scheduler.dbcleaner.cron.expression}",
		name = "${scheduler.dbcleaner.name}",
		lockAtMostFor = "${scheduler.dbcleaner.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.dbcleaner.maximum-execution-time}")
	public void execute() {
		databaseCleanerService.cleanDatabase();
	}
}
