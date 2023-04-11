package se.sundsvall.seabloader.scheduler.dbcleaner;

import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.db.model.enums.Status;
import se.sundsvall.seabloader.scheduler.AbstractScheduler;

@Service
@ConfigurationProperties("scheduler.dbcleaner.cron")
public class DatabaseCleanerSchedulerService extends AbstractScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerSchedulerService.class);
	private static final String LOG_CLEANING_STARTED = "Beginning removal of obsolete entities in the database";
	private static final String LOG_ENTITIES_REMOVAL = "Removing a total of {} obsolete entities having status {}";
	private static final String LOG_NOTHING_TO_REMOVE = "No entities found with status {}, hence no obsolete entities to remove";
	private static final String LOG_OPTIMIZE_RESULT = "Optimize result: {}";
	private static final String LOG_CLEANING_ENDED = "Cleaning of obsolete entities in database has ended";

	private static final int DELETE_CHUNK_SIZE = 10;
	private static final Status[] STATUS_FOR_ENTITIES_TO_REMOVE = { PROCESSED };

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Override
	@Scheduled(cron = "${scheduler.dbcleaner.cron.expression:-}")
	public void execute() {
		RequestId.init();

		LOGGER.info(LOG_CLEANING_STARTED);

		final var entitiesToRemove = invoiceRepository.countByStatusIn(STATUS_FOR_ENTITIES_TO_REMOVE);
		if (entitiesToRemove > 0) {
			LOGGER.info(LOG_ENTITIES_REMOVAL, entitiesToRemove, STATUS_FOR_ENTITIES_TO_REMOVE);
			Lists.partition(getIdsToRemove(), DELETE_CHUNK_SIZE).stream().forEach(invoiceRepository::deleteAllByIdInBatch);

			final var optimizeResult = invoiceRepository.optimizeTable();
			LOGGER.info(LOG_OPTIMIZE_RESULT, optimizeResult);
		} else {
			LOGGER.info(LOG_NOTHING_TO_REMOVE, (Object) STATUS_FOR_ENTITIES_TO_REMOVE);
		}

		LOGGER.info(LOG_CLEANING_ENDED);
	}

	private List<Long> getIdsToRemove() {
		return invoiceRepository.findIdsByStatusIn(STATUS_FOR_ENTITIES_TO_REMOVE)
			.stream()
			.map(InvoiceId::getId)
			.toList();
	}
}
