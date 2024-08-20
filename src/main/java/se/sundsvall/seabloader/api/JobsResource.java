package se.sundsvall.seabloader.api;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.seabloader.service.AsyncExecutorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@Tag(name = "Jobs", description = "Jobs resource")
@RequestMapping("/{municipalityId}/jobs")
public class JobsResource {

	private final AsyncExecutorService asyncExecutorService;

	public JobsResource(final AsyncExecutorService asyncExecutorService) {
		this.asyncExecutorService = asyncExecutorService;
	}

	@PostMapping(path = "/invoiceexporter", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers export invoices (to InvoiceCache) job.", description = "Triggers export invoices (to InvoiceCache) job.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> invoiceexporter(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId) {
		asyncExecutorService.invoiceExportExecute();
		return noContent().build();
	}

	@PostMapping(path = "/notifier", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers notification job.", description = "Triggers notification job.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> notifier(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId) {
		asyncExecutorService.notifierExecute();
		return noContent().build();
	}

	@PostMapping(path = "/dbcleaner", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers database cleaning job.", description = "Triggers database cleaning job.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> dbcleaner(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId) {
		asyncExecutorService.databaseCleanerExecute();
		return noContent().build();
	}
}
