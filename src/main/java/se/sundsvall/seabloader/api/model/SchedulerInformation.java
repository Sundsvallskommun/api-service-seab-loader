package se.sundsvall.seabloader.api.model;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Scheduler information model")
public class SchedulerInformation {

	@Schema(description = "Scheduler name", example = "SchedulerService")
	private String name;

	@Schema(description = "Scheduler expression", example = "0 0 13 * * 7")
	private String expression;

	@Schema(description = "Scheduler human readable description", example = "At 13:00, only on Sunday")
	private String description;

	public static SchedulerInformation create() {
		return new SchedulerInformation();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public SchedulerInformation withName(final String name) {
		this.name = name;
		return this;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(final String expression) {
		this.expression = expression;
	}

	public SchedulerInformation withExpression(final String expression) {
		this.expression = expression;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public SchedulerInformation withDescription(final String description) {
		this.description = description;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, expression, name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (SchedulerInformation) obj;
		return Objects.equals(description, other.description) && Objects.equals(expression, other.expression) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("SchedulerInformation [name=").append(name).append(", expression=").append(expression).append(", description=").append(description).append("]");
		return builder.toString();
	}
}
