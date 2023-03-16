package se.sundsvall.seabloader.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.IMPORT_FAILED;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.UNPROCESSED;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.seabloader.Application;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

/**
 * CreateInvoiceIT tests.
 */
@WireMockAppTestSuite(files = "classpath:/CreateInvoiceIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class CreateInvoiceIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "invoice.xml";

	@Autowired
	private InvoiceRepository repository;

	@Test
	void test01_createInvoice() throws IOException, URISyntaxException {

		// Assert that invoice doesn't exist yet.
		assertThat(repository.existsByInvoiceId("111")).isFalse();

		// Call.
		setupCall()
			.withServicePath("/invoices")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse(APPLICATION_XML);

		// Assert that invoice exists with expected values.
		assertThat(repository.findByInvoiceId("111")).get()
			.extracting(InvoiceEntity::getId, InvoiceEntity::getContent, InvoiceEntity::getInvoiceId, InvoiceEntity::getStatus)
			.containsExactly(2L, getResourceAsString("CreateInvoiceIT/__files/test01_createInvoice/invoice.xml"), "111", UNPROCESSED);
	}

	@Test
	void test02_createInvoiceThatWillFail() throws IOException, URISyntaxException {

		// Assert that we have one failed invoice (with null as invoiceId).
		assertThat(repository.findByStatusIn(IMPORT_FAILED))
			.extracting(InvoiceEntity::getId, InvoiceEntity::getInvoiceId, InvoiceEntity::getStatus)
			.containsExactlyInAnyOrder(
				tuple(1L, null, IMPORT_FAILED));

		// Call.
		setupCall()
			.withServicePath("/invoices")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse(APPLICATION_XML);

		// Assert that we have two failed invoices (with null as invoiceId).
		assertThat(repository.findByStatusIn(IMPORT_FAILED))
			.extracting(InvoiceEntity::getId, InvoiceEntity::getContent, InvoiceEntity::getInvoiceId, InvoiceEntity::getStatus)
			.containsExactlyInAnyOrder(
				tuple(1L, "Lorem ipsum dolor sit amet", null, IMPORT_FAILED),
				tuple(2L, getResourceAsString("CreateInvoiceIT/__files/test02_createInvoiceThatWillFail/invoice.xml"), null, IMPORT_FAILED));
	}

	private String getResourceAsString(final String resourcePath) throws IOException, URISyntaxException {
		return Files.readString(Paths.get(getClass().getClassLoader().getResource(resourcePath).toURI()));
	}
}
