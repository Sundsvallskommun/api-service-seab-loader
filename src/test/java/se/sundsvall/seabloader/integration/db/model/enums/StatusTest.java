package se.sundsvall.seabloader.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatusTest {

	@Test
	void enumValues() {
		assertThat(Status.values()).containsExactlyInAnyOrder(Status.UNPROCESSED, Status.PROCESSED, Status.FAILED);
	}

	@Test
	void enumToString() {
		assertThat(Status.UNPROCESSED).hasToString("UNPROCESSED");
		assertThat(Status.PROCESSED).hasToString("PROCESSED");
		assertThat(Status.FAILED).hasToString("FAILED");
	}
}
