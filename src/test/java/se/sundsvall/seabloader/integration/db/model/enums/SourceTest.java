package se.sundsvall.seabloader.integration.db.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.IN_EXCHANGE;
import static se.sundsvall.seabloader.integration.db.model.enums.Source.STRALFORS;

class SourceTest {

	@Test
	void enumValues() {
		assertThat(Source.values()).containsExactlyInAnyOrder(IN_EXCHANGE, STRALFORS);
	}

	@Test
	void enumToString() {
		assertThat(IN_EXCHANGE).hasToString("IN_EXCHANGE");
		assertThat(STRALFORS).hasToString("STRALFORS");
	}
}
