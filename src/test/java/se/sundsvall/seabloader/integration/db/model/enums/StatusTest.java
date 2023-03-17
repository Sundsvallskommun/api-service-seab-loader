package se.sundsvall.seabloader.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.EXPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import org.junit.jupiter.api.Test;

class StatusTest {

	@Test
	void enumValues() {
		assertThat(Status.values()).containsExactlyInAnyOrder(UNPROCESSED, PROCESSED, EXPORT_FAILED, IMPORT_FAILED);
	}

	@Test
	void enumToString() {
		assertThat(UNPROCESSED).hasToString("UNPROCESSED");
		assertThat(PROCESSED).hasToString("PROCESSED");
		assertThat(EXPORT_FAILED).hasToString("EXPORT_FAILED");
		assertThat(IMPORT_FAILED).hasToString("IMPORT_FAILED");
	}
}
