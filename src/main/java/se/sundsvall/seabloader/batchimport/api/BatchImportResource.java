package se.sundsvall.seabloader.batchimport.api;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.accepted;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.seabloader.batchimport.service.BatchImportService;

@RestController
@Validated
@Tag(name = "BatchImport", description = "Batch import resource")
@RequestMapping("/batchimport")
public class BatchImportResource { // TODO: Remove after completion of Stralfors invoices import

	@Autowired
	private BatchImportService batchImportService;

	@Value(value = "${api.batchimportresource.rootpath:}")
	private String rootPath;

	@PostMapping(path = "/{subDirectory}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers batchImportService for sent in sub directory.")
	@ApiResponse(responseCode = "202", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<String> batchimport(
		@Parameter(name = "subDirectory", description = "Sub directory to import", example = "06_2020") @PathVariable(name = "subDirectory", required = true) @NotBlank String subDirectory) {
		
		if (StringUtils.isNotBlank(rootPath)) {
			batchImportService.execute(rootPath + (rootPath.endsWith(File.separator) ? EMPTY : File.separator) + subDirectory + File.separator);
			return accepted().build();
		}

		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "{Path to import root directory is not set in properties file}");
	}
}
