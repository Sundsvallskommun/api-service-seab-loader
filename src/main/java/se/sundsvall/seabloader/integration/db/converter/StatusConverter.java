package se.sundsvall.seabloader.integration.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;
import se.sundsvall.seabloader.integration.db.model.enums.Status;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

	@Override
	public String convertToDatabaseColumn(Status attribute) {
		return Optional.ofNullable(attribute).map(Status::toString).orElse(null);
	}

	@Override
	public Status convertToEntityAttribute(String dbData) {
		return Status.valueOf(dbData);
	}
}
