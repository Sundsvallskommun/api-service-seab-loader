package se.sundsvall.seabloader.scheduler.dbcleaner;

import static java.time.Clock.systemUTC;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.seabloader.service.DatabaseCleanerService;

@SpringBootTest(properties = {
	"scheduler.dbcleaner.cron.expression=* * * * * *", // Setup to execute every second
	"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
	"spring.datasource.url=jdbc:tc:mariadb:10.6.4:////",
	"server.shutdown=immediate",
	"spring.lifecycle.timeout-per-shutdown-phase=0s"
})
@ActiveProfiles("junit")
class DatabaseCleanerSchedulerShedlockTest {

	private static LocalDateTime mockCalledTime;

	@Autowired
	private DatabaseCleanerService databaseCleanerService;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Test
	void verifyShedLockForDatabaseCleaner() {
		// Make sure scheduling occurs multiple times
		await().until(() -> (mockCalledTime != null) && LocalDateTime.now().isAfter(mockCalledTime.plusSeconds(2)));

		// Verify lock
		await().atMost(5, SECONDS)
			.untilAsserted(() -> assertThat(getLockedAt("dbcleaner"))
				.isCloseTo(LocalDateTime.now(systemUTC()), within(10, ChronoUnit.SECONDS)));

		verify(databaseCleanerService).cleanDatabase();
		verifyNoMoreInteractions(databaseCleanerService);
	}

	private LocalDateTime getLockedAt(final String name) {
		return jdbcTemplate.query(
			"SELECT locked_at FROM shedlock WHERE name = :name",
			Map.of("name", name),
			this::mapTimestamp);
	}

	private LocalDateTime mapTimestamp(final ResultSet rs) throws SQLException {
		if (rs.next()) {
			return rs.getTimestamp("locked_at").toLocalDateTime();
		}
		return null;
	}

	@TestConfiguration
	public static class ShedlockTestConfiguration {

		@Bean
		@Primary
		DatabaseCleanerService createMock() {

			final var mockBean = Mockito.mock(DatabaseCleanerService.class);

			// Let mock hang
			doAnswer(invocation -> {
				mockCalledTime = LocalDateTime.now();
				await().forever()
					.until(() -> false);
				return null;
			}).when(mockBean).cleanDatabase();

			return mockBean;
		}
	}
}
