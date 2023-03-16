package se.sundsvall.seabloader.scheduler;

import se.sundsvall.seabloader.api.model.SchedulerInformation;

public interface SchedulerService {

	void execute();

	SchedulerInformation getScheduleInformation();

	String getExpression();
}
