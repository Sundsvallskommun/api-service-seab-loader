package se.sundsvall.seabloader.integration.db.listener;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.isNull;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import java.time.ZoneId;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

public class InvoiceEntityListener {

	@PrePersist
	void prePersist(final InvoiceEntity entity) {
		entity.setCreated(now(ZoneId.systemDefault()).truncatedTo(MILLIS));

		if (isNull(entity.getStatus())) {
			entity.setStatus(UNPROCESSED);
		}
	}

	@PreUpdate
	void preUpdate(final InvoiceEntity entity) {
		final var now = now(ZoneId.systemDefault()).truncatedTo(MILLIS);

		entity.setModified(now);

		if (PROCESSED.equals(entity.getStatus()) && isNull(entity.getProcessed())) {
			entity.setProcessed(now);
			entity.setStatusMessage(null);
		}
	}
}
