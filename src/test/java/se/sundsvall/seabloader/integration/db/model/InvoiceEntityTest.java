package se.sundsvall.seabloader.integration.db.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.IN_EXCHANGE;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

class InvoiceEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(InvoiceEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void hasValidBuilderMethods() {

		final var created = now();
		final var content = "content";
		final var modified = now().plusDays(1);
		final var processed = now().plusDays(2);
		final var id = 1L;
		final var invoiceId = "invoiceId";
		final var status = UNPROCESSED;
		final var statusMessage = "statusMessage";
		final var source = IN_EXCHANGE;

		final var entity = InvoiceEntity.create()
			.withCreated(created)
			.withContent(content)
			.withModified(modified)
			.withProcessed(processed)
			.withId(id)
			.withInvoiceId(invoiceId)
			.withStatus(status)
			.withStatusMessage(statusMessage)
			.withSource(source);

		assertThat(entity).hasNoNullFieldsOrProperties();
		assertThat(entity.getCreated()).isEqualTo(created);
		assertThat(entity.getContent()).isEqualTo(content);
		assertThat(entity.getModified()).isEqualTo(modified);
		assertThat(entity.getProcessed()).isEqualTo(processed);
		assertThat(entity.getId()).isEqualTo(id);
		assertThat(entity.getInvoiceId()).isEqualTo(invoiceId);
		assertThat(entity.getStatus()).isEqualTo(status);
		assertThat(entity.getStatusMessage()).isEqualTo(statusMessage);
		assertThat(entity.getSource()).isEqualTo(source);
	}

	@Test
	void hasNoDirtOnCreatedBean() {
		assertThat(new InvoiceEntity()).hasAllNullFieldsOrPropertiesExcept("id");
		assertThat(InvoiceEntity.create()).hasAllNullFieldsOrPropertiesExcept("id");
	}
}
