package se.sundsvall.seabloader.integration.db;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;
import se.sundsvall.seabloader.integration.db.model.InvoiceId;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Transactional(isolation = READ_COMMITTED)
@CircuitBreaker(name = "InvoiceRepository")
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long>, JpaSpecificationExecutor<InvoiceEntity> {

	/**
	 * Find by invoiceId.
	 *
	 * @param municipalityId the id of the municipality.
	 * @param invoiceId the invoiceId of the entity to find.
	 * @return An optional InvoiceEntity.
	 */
	Optional<InvoiceEntity> findByMunicipalityIdAndInvoiceId(final String municipalityId, final String invoiceId);


	/**
	 * Returns whether an entity with the given municipalityid and invoiceId exists.
	 *
	 * @param municipalityId id of the municipality.
	 * @param invoiceId must not be {@literal null}.
	 * @return {@literal true} if an entity with the given invoiceId exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal invoiceId} is {@literal null}.
	 */
	boolean existsByMunicipalityIdAndInvoiceId(final String municipalityId, final String invoiceId);

	/**
	 * Get ids of entities matching status in sent in status list.
	 *
	 * @param statusList a List of statuses to filter on.
	 * @return A List of InvoiceId:s
	 */
	List<InvoiceId> findIdsByStatusIn(Status... statusList);

	/**
	 * Find by status list.
	 *
	 * @param statusList a List of statuses to filter on.
	 * @return A List of InvoiceEntity
	 */
	List<InvoiceEntity> findByStatusIn(Status... statusList);

	/**
	 * Count occurences of entities with statuses equal to sent in statuses.
	 *
	 * @param statusList a List of statuses to filter on.
	 * @return amount of entities having matching the sent in statuses.
	 */
	long countByStatusIn(Status... statusList);

	/**
	 * Delete entities with statuses equal to sent in statuses.
	 *
	 * @param statusList a List of statuses to delete by.
	 */
	void deleteByStatusIn(Status... statusList);

	/**
	 * Reorganizes the physical storage of table data and associated index data,
	 * to reduce storage space and improve I/O efficiency when accessing the table.
	 * The exact changes made to each table depend on the storage engine used by that table.
	 *
	 * @return the result as a list of operation result strings.
	 */
	@Query(value = "OPTIMIZE TABLE invoice", nativeQuery = true)
	List<String> optimizeTable();


}
