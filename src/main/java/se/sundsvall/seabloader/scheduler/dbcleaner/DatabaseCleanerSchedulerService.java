package se.sundsvall.seabloader.scheduler.dbcleaner;

import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;

@Service
@ConfigurationProperties("scheduler.database.cleaner.cron")
public class DatabaseCleanerSchedulerService extends AbstractScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerSchedulerService.class);
	private static final String LOG_CLEANING_STARTED = "Beginning removal of obsolete entities in the database";
	private static final String LOG_ENTITIES_REMOVAL = "Removing a total of {} obsolete entities having status {}";
	private static final String LOG_NOTHING_TO_REMOVE = "No entities found with status {}, hence no obsolete entities to remove";
	private static final String LOG_CLEANING_ENDED = "Cleaning of obsolete entities in database has ended";

	private static final Status[] STATUSES_TO_REMOVE = { PROCESSED };

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Override
	@Scheduled(cron = "${scheduler.database.cleaner.cron.expression:-}")
	public void execute() {
		LOGGER.info(LOG_CLEANING_STARTED);

		final var entitiesToRemove = invoiceRepository.countByStatusIn(STATUSES_TO_REMOVE);
		if (entitiesToRemove > 0) {
			LOGGER.info(LOG_ENTITIES_REMOVAL, entitiesToRemove, STATUSES_TO_REMOVE);
			invoiceRepository.deleteAll(invoiceRepository.findByStatusIn(STATUSES_TO_REMOVE));
		} else {
			LOGGER.info(LOG_NOTHING_TO_REMOVE, (Object) STATUSES_TO_REMOVE);
		}

		LOGGER.info(LOG_CLEANING_ENDED);
	}
}