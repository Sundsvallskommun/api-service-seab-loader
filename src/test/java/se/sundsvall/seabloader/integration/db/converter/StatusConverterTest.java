package se.sundsvall.seabloader.integration.db.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class StatusConverterTest {

	private final StatusConverter statusConverter = new StatusConverter();

	@ParameterizedTest
	@EnumSource(value = Status.class)
	void testConvertToDatabaseColumn(Status status) {
		final var value = statusConverter.convertToDatabaseColumn(status);
		assertThat(value).isNotNull();
	}

	@Test
	void testConvertToDatabaseColumnWhenNullValue() {
		final var value = statusConverter.convertToDatabaseColumn(null);
		assertThat(value).isNull();
	}

	@ParameterizedTest
	@EnumSource(value = Status.class)
	void testConvertToEntityAttribute(Status status) {
		final var value = statusConverter.convertToEntityAttribute(status.name());
		assertThat(value).isNotNull();
	}

	@Test
	void testConvertToEntityAttributeWhenMissingValue() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> statusConverter.convertToEntityAttribute("noMatch"))
			.withMessage("No enum constant se.sundsvall.seabloader.integration.db.model.enums.Status.noMatch");
	}
}
