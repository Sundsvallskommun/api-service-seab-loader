package se.sundsvall.seabloader.integration.db.listener;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import org.junit.jupiter.api.Test;

import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

class InvoiceEntityListenerTest {

	@Test
	void prePerist() {

		// Setup
		final var listener = new InvoiceEntityListener();
		final var entity = new InvoiceEntity();

		// Call
		listener.prePersist(entity);

		// Assertions
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("id", "status", "created");
		assertThat(entity.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(entity.getStatus()).isEqualTo(UNPROCESSED);
	}

	@Test
	void prePeristWhenStatusIsAlreadySet() {

		// Setup
		final var listener = new InvoiceEntityListener();
		final var entity = new InvoiceEntity().withStatus(FAILED);

		// Call
		listener.prePersist(entity);

		// Assertions
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("id", "status", "created");
		assertThat(entity.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(entity.getStatus()).isEqualTo(FAILED);
	}

	@Test
	void preUpdate() {

		// Setup
		final var listener = new InvoiceEntityListener();
		final var entity = new InvoiceEntity();

		// Call
		listener.preUpdate(entity);

		// Assertions
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("id", "modified");
		assertThat(entity.getModified()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void preUpdateWhenStatusIsSetToProcessed() {

		// Setup
		final var listener = new InvoiceEntityListener();
		final var entity = new InvoiceEntity();

		// Update
		entity.setStatus(PROCESSED);

		// Call
		listener.preUpdate(entity);

		// Assertions
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("id", "modified", "status", "processed");
		assertThat(entity.getStatus()).isEqualTo(PROCESSED);
		assertThat(entity.getModified()).isCloseTo(now(), within(2, SECONDS));
		assertThat(entity.getProcessed()).isCloseTo(now(), within(2, SECONDS));
	}
}
