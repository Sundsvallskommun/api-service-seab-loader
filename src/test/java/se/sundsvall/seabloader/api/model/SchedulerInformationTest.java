package se.sundsvall.seabloader.api.model;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class SchedulerInformationTest {

	@Test
	void testBean() {
		assertThat(SchedulerInformation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var name = "name";
		final var expression = "expression";
		final var description = "description";

		final var schedulerInformation = SchedulerInformation.create()
			.withDescription(description)
			.withName(name)
			.withExpression(expression);

		assertThat(schedulerInformation).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(schedulerInformation.getDescription()).isEqualTo(description);
		assertThat(schedulerInformation.getName()).isEqualTo(name);
		assertThat(schedulerInformation.getExpression()).isEqualTo(expression);
	}

	@ParameterizedTest
	@MethodSource("testNoDirtOnCreatedBeanArguments")
	void testNoDirtOnCreatedBean(final SchedulerInformation schedulerInformation) {
		assertThat(schedulerInformation).hasAllNullFieldsOrProperties();
	}

	private static Stream<Arguments> testNoDirtOnCreatedBeanArguments() {
		return Stream.of(
			Arguments.of(new SchedulerInformation()),
			Arguments.of(SchedulerInformation.create()));
	}
}
