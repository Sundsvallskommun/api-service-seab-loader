package se.sundsvall.seabloader.integration.db.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

	@Test
	void enumValues() {
		assertThat(Status.values()).containsExactlyInAnyOrder(Status.UNPROCESSED, Status.PROCESSED, Status.EXPORT_FAILED, Status.FAILED);
	}

	@Test
	void enumToString() {
		assertThat(Status.UNPROCESSED).hasToString("UNPROCESSED");
		assertThat(Status.PROCESSED).hasToString("PROCESSED");
		assertThat(Status.EXPORT_FAILED).hasToString("EXPORT_FAILED");
		assertThat(Status.FAILED).hasToString("FAILED");
	}
}
