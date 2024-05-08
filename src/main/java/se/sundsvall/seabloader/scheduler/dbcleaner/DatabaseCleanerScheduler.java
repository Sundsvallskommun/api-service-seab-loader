package se.sundsvall.seabloader.scheduler.dbcleaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;
import se.sundsvall.seabloader.service.DatabaseCleanerService;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service
@ConfigurationProperties("scheduler.dbcleaner.cron")
public class DatabaseCleanerScheduler extends AbstractScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerScheduler.class);
	private static final String LOG_CLEANING_STARTED = "Beginning removal of obsolete entities in the database";
	private static final String LOG_CLEANING_ENDED = "Cleaning of obsolete entities in database has ended";

	private final DatabaseCleanerService databaseCleanerService;

	public DatabaseCleanerScheduler(final DatabaseCleanerService databaseCleanerService) {
		this.databaseCleanerService = databaseCleanerService;
	}

	@Override
	@Scheduled(cron = "${scheduler.dbcleaner.cron.expression}")
	@SchedulerLock(name = "dbcleaner", lockAtMostFor = "${scheduler.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_CLEANING_STARTED);
		databaseCleanerService.cleanDatabase();
		LOGGER.info(LOG_CLEANING_ENDED);
	}

}
