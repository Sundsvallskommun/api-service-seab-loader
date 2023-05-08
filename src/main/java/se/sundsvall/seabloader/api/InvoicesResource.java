package se.sundsvall.seabloader.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.seabloader.service.InvoiceService;

import jakarta.validation.constraints.NotNull;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Validated
@Tag(name = "Invoices", description = "Invoice resource")
@RequestMapping("/invoices")
public class InvoicesResource {

	@Autowired
	private InvoiceService invoiceService;

	@PostMapping(consumes = { APPLICATION_OCTET_STREAM_VALUE, APPLICATION_XML_VALUE }, produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Create invoice", description = "Receives and stores invoices in XML-format.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> createInvoice(@NotNull @RequestBody final byte[] body) {
		invoiceService.create(body);
		return noContent().build();
	}
}
