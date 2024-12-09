package se.sundsvall.seabloader.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
class SchemaVerificationTest {

	private static final String STORED_SCHEMA_FILE = "db/scripts/schema.sql";

	@Value("${spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target}")
	private String generatedSchemaFile;

	@Test
	void verifySchemaUpdates() throws IOException, URISyntaxException {

		final var storedSchema = getResourceString(STORED_SCHEMA_FILE);
		final var generatedSchema = Files.readString(Path.of(generatedSchemaFile));

		assertThat(generatedSchema)
			.as(String.format("Please reflect modifications to entities in file: %s", STORED_SCHEMA_FILE))
			.isEqualToNormalizingWhitespace(storedSchema);
	}

	private String getResourceString(final String fileName) throws IOException, URISyntaxException {
		return Files.readString(Paths.get(getClass().getClassLoader().getResource(fileName).toURI()));
	}
}
