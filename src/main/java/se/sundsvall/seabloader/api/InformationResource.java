package se.sundsvall.seabloader.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.seabloader.api.model.SchedulerInformation;
import se.sundsvall.seabloader.scheduler.SchedulerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/{municipalityId}/information")
@Tag(name = "Information", description = "Information resources")
public class InformationResource {

	private final List<SchedulerService> scheduleServiceList;

	public InformationResource(List<SchedulerService> scheduleServiceList) {
		this.scheduleServiceList = scheduleServiceList;
	}

	@GetMapping(path = "/schedulers", produces = {APPLICATION_JSON_VALUE})
	@Operation(summary = "Scheduler information")
	@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SchedulerInformation.class))))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<List<SchedulerInformation>> getSchedulerInformation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId) {
		return ok(scheduleServiceList.stream()
			.map(SchedulerService::getScheduleInformation)
			.toList());
	}
}
