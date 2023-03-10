package se.sundsvall.seabloader.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

@CircuitBreaker(name = "InvoiceRepository")
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long>, JpaSpecificationExecutor<InvoiceEntity> {

	/**
	 * Find by invoiceId.
	 *
	 * @param invoiceId the invoiceId of the entity to find.
	 * @return An optional InvoiceEntity.
	 */
	Optional<InvoiceEntity> findByInvoiceId(String invoiceId);

	/**
	 * Returns whether an entity with the given invoiceId exists.
	 *
	 * @param invoiceId must not be {@literal null}.
	 * @return {@literal true} if an entity with the given invoiceId exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal invoiceId} is {@literal null}.
	 */
	boolean existsByInvoiceId(String invoiceId);

	/**
	 * Find by status list.
	 *
	 * @param statusList a List of statuses to filter on.
	 * @return A List of InvoiceEntity
	 */
	List<InvoiceEntity> findByStatusIn(Status... statusList);
}
