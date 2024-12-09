package se.sundsvall.seabloader.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.Length;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.seabloader.integration.db.listener.InvoiceEntityListener;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

@Entity
@Table(name = "invoice",
	uniqueConstraints = {
		@UniqueConstraint(name = "invoice_unique_invoice_id_constraint", columnNames = {
			"invoice_id"
		})
	},
	indexes = {
		@Index(name = "invoice_invoice_id_index", columnList = "invoice_id"),
		@Index(name = "invoice_status_index", columnList = "status"),
		@Index(name = "invoice_municipality_id_index", columnList = "municipality_id")
	})
@EntityListeners(InvoiceEntityListener.class)
public class InvoiceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "municipality_id", length = 4)
	private String municipalityId;

	@Column(name = "invoice_id")
	private String invoiceId;

	@Column(name = "content", length = Length.LONG32)
	private String content;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "modified")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime modified;

	@Column(name = "processed")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime processed;

	@Column(name = "status", nullable = false)
	private Status status;

	@Column(name = "status_message", length = Length.LONG32)
	private String statusMessage;

	public static InvoiceEntity create() {
		return new InvoiceEntity();
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public InvoiceEntity withId(final long id) {
		this.id = id;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public InvoiceEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(final String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public InvoiceEntity withInvoiceId(final String invoiceId) {
		this.invoiceId = invoiceId;
		return this;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public InvoiceEntity withContent(final String content) {
		this.content = content;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	public InvoiceEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getModified() {
		return modified;
	}

	public void setModified(final OffsetDateTime modified) {
		this.modified = modified;
	}

	public InvoiceEntity withModified(final OffsetDateTime modified) {
		this.modified = modified;
		return this;
	}

	public OffsetDateTime getProcessed() {
		return processed;
	}

	public void setProcessed(final OffsetDateTime processed) {
		this.processed = processed;
	}

	public InvoiceEntity withProcessed(final OffsetDateTime processed) {
		this.processed = processed;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public InvoiceEntity withStatus(final Status status) {
		this.status = status;
		return this;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public InvoiceEntity withStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
		return this;
	}

	@Override
	public String toString() {
		return "InvoiceEntity{" +
			"id=" + id +
			", municipalityId='" + municipalityId + '\'' +
			", invoiceId='" + invoiceId + '\'' +
			", content='" + content + '\'' +
			", created=" + created +
			", modified=" + modified +
			", processed=" + processed +
			", status=" + status +
			", statusMessage='" + statusMessage + '\'' +
			'}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final InvoiceEntity that = (InvoiceEntity) o;
		return id == that.id && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(invoiceId, that.invoiceId) && Objects.equals(content, that.content) && Objects.equals(created, that.created) && Objects.equals(modified, that.modified)
			&& Objects.equals(processed, that.processed) && status == that.status && Objects.equals(statusMessage, that.statusMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, municipalityId, invoiceId, content, created, modified, processed, status, statusMessage);
	}
}
