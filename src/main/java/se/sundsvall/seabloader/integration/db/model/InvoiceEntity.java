package se.sundsvall.seabloader.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.Length;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.seabloader.integration.db.listener.InvoiceEntityListener;
import se.sundsvall.seabloader.integration.db.model.enums.Source;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

import java.time.OffsetDateTime;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Entity
@Table(name = "invoice",
	uniqueConstraints = {
		@UniqueConstraint(name = "invoice_unique_invoice_id_constraint", columnNames = { "invoice_id" })
	},
	indexes = {
		@Index(name = "invoice_invoice_id_index", columnList = "invoice_id"),
		@Index(name = "invoice_status_index", columnList = "status")
	})
@EntityListeners(InvoiceEntityListener.class)
public class InvoiceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

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
	@Enumerated(STRING)
	private Status status;

	@Column(name = "status_message", length = Length.LONG32)
	private String statusMessage;

	@Column(name = "source", nullable = false)
	@Enumerated(STRING)
	private Source source;

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

	public Source getSource() {
		return source;
	}

	public void setSource(final Source source) {
		this.source = source;
	}

	public InvoiceEntity withSource(final Source source) {
		this.source = source;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, created, id, invoiceId, modified, processed, status, statusMessage, source);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final InvoiceEntity other)) {
			return false;
		}
		return Objects.equals(content, other.content) && Objects.equals(created, other.created) && (id == other.id) && Objects.equals(invoiceId, other.invoiceId) && Objects.equals(modified, other.modified) && Objects.equals(processed, other.processed)
			&& (status == other.status) && Objects.equals(statusMessage, other.statusMessage) && Objects.equals(source, other.source);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("InvoiceEntity [id=").append(id).append(", invoiceId=").append(invoiceId).append(", content=").append(content).append(", created=").append(created).append(", modified=").append(modified).append(", processed=").append(processed)
			.append(", status=").append(status).append(", statusMessage=").append(statusMessage).append(", source=").append(source).append("]");
		return builder.toString();
	}
}
