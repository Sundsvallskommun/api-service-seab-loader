package se.sundsvall.seabloader.scheduler.dbcleaner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.seabloader.service.DatabaseCleanerService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanerSchedulerTest {

	@Mock
	private DatabaseCleanerService databaseCleanerServiceMock;

	@InjectMocks
	private DatabaseCleanerScheduler scheduler;

	@Test
	void executeWithEntitiesToRemove() {

		// Act
		scheduler.execute();

		// Assert
		verify(databaseCleanerServiceMock).cleanDatabase();
		verifyNoMoreInteractions(databaseCleanerServiceMock);
	}
}
