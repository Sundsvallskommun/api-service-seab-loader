package se.sundsvall.seabloader.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;

/**
 * InvoiceRepository tests
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class InvoiceRepositoryTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String INVOICE_ID = "123456";
	private static final String CONTENT = "<xml></xml>";

	@Autowired
	private InvoiceRepository repository;

	@Test
	void create() {

		final var entity = createInvoiceEntity();

		final var result = repository.save(entity);

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getId()).isPositive();
		assertThat(result.getStatus()).isEqualTo(UNPROCESSED);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getModified()).isNull();
		assertThat(result.getProcessed()).isNull();
	}

	@Test
	void findById() {

		// Setup
		final var id = 1L;
		final var invoiceId = "INV-2023-001";

		final var result = repository.findById(id).orElseThrow();

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getInvoiceId()).isEqualTo(invoiceId);
	}

	@Test
	void findByIdNotFound() {
		assertThat(repository.findById(123456789L)).isNotPresent();
	}

	@Test
	void findByMunicipalityIdAndInvoiceId() {

		// Setup
		final var id = 2L;
		final var invoiceId = "INV-2023-002";

		final var result = repository.findByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, invoiceId).orElseThrow();

		// Verification
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getInvoiceId()).isEqualTo(invoiceId);
	}

	@Test
	void findByMunicipalityIdAndInvoiceIdNotFound() {
		assertThat(repository.findByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "non-existing-flowInstanceId")).isNotPresent();
	}

	@Test
	void update() {

		// Setup
		final var id = 3L;

		// Fetch existing entity.
		final var accessCard = repository.findById(id).orElseThrow();
		assertThat(accessCard.getStatus()).isEqualTo(UNPROCESSED);
		assertThat(accessCard.getProcessed()).isNull();
		assertThat(accessCard.getModified()).isNull();

		// Update entity.
		repository.save(accessCard.withStatus(PROCESSED));
		repository.flush();

		// Verification
		final var result = repository.findById(id).orElseThrow();
		assertThat(result.getStatus()).isEqualTo(PROCESSED);
		assertThat(result.getProcessed()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getModified()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void deleteById() {

		// Setup
		final var id = 4L;

		assertThat(repository.findById(id)).isPresent();

		repository.deleteById(id);

		// Verification
		assertThat(repository.findById(id)).isNotPresent();
	}

	@Test
	void existsByMunicipalityIdAndInvoiceId() {
		assertThat(repository.existsByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "INV-2023-002")).isTrue();
	}

	@Test
	void existsByMunicipalityIdAndInvoiceIdNotFound() {
		assertThat(repository.existsByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "DOES-NOT-EXIST")).isFalse();
	}

	@Test
	void findByStatusIn() {

		// Call
		final var result = repository.findByStatusIn(IMPORT_FAILED, PROCESSED);

		// Verification
		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(InvoiceEntity::getId, InvoiceEntity::getStatus)
			.containsExactlyInAnyOrder(
				tuple(5L, PROCESSED),
				tuple(6L, IMPORT_FAILED));
	}

	@Test
	void countByStatusIn() {
		// Call
		assertThat(repository.countByStatusIn(IMPORT_FAILED, PROCESSED)).isEqualTo(2);
	}

	@Test
	void findIdsByStatusIn() {

		// Call
		final var result = repository.findIdsByStatusIn(IMPORT_FAILED, PROCESSED);

		// Verification
		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(InvoiceId::getId)
			.containsExactlyInAnyOrder(5L, 6L);
	}

	@Test
	void deleteByStatusIn() {

		assertThat(repository.count()).isEqualTo(6);
		assertThat(repository.countByStatusIn(PROCESSED)).isEqualTo(1);

		// Call
		repository.deleteByStatusIn(PROCESSED);

		// Verification
		assertThat(repository.count()).isEqualTo(5);
		assertThat(repository.countByStatusIn(PROCESSED)).isZero();
	}

	@Test
	void optimizeTable() {

		// Call
		final var result = repository.optimizeTable();
		assertThat(result).containsExactly(
			"test.invoice,optimize,note,Table does not support optimize, doing recreate + analyze instead",
			"test.invoice,optimize,status,OK");
	}

	private static InvoiceEntity createInvoiceEntity() {
		return new InvoiceEntity()
			.withContent(CONTENT)
			.withInvoiceId(INVOICE_ID);
	}
}
