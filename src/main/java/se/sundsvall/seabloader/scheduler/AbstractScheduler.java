package se.sundsvall.seabloader.scheduler;

import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.StringUtils.length;

import it.burning.cron.CronExpressionDescriptor;
import it.burning.cron.CronExpressionParser.Options;
import java.util.Optional;
import se.sundsvall.seabloader.api.model.SchedulerInformation;

public abstract class AbstractScheduler implements SchedulerService {

	protected String expression;

	@Override
	public SchedulerInformation getScheduleInformation() {
		return SchedulerInformation.create()
			.withDescription(getDescription())
			.withName(this.getClass().getSimpleName())
			.withExpression(getExpression());
	}

	@Override
	public String getExpression() {
		return Optional.ofNullable(expression).orElse("");
	}

	public void setExpression(final String expression) {
		this.expression = expression;
	}

	private static Options options() {
		final var options = new Options();
		options.setVerbose(true);
		options.setUseJavaEeScheduleExpression(true);
		options.setLocale(ENGLISH);
		return options;
	}

	private String getDescription() {
		return Optional.ofNullable(expression)
			.filter(expr -> length(expr) > 10)
			.map(expr -> CronExpressionDescriptor.getDescription(expression, options()))
			.orElse("Expression invalid or undefined. Scheduler will not run.");
	}
}
