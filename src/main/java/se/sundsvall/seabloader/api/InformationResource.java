package se.sundsvall.seabloader.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import se.sundsvall.seabloader.api.model.SchedulerInformation;
import se.sundsvall.seabloader.scheduler.SchedulerService;

@RestController
@Validated
@RequestMapping("/information")
@Tag(name = "Information", description = "Information resources")
public class InformationResource {

	@Autowired
	private List<SchedulerService> scheduleServiceList;

	@GetMapping(path = "/schedulers", produces = { APPLICATION_JSON_VALUE })
	@Operation(summary = "Scheduler information")
	@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SchedulerInformation.class))))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<List<SchedulerInformation>> getSchedulerInformation() {
		return ok(scheduleServiceList.stream()
			.map(SchedulerService::getScheduleInformation)
			.toList());
	}
}
