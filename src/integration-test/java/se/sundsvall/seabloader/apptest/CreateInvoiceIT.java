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

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/invoices";
	private static final String REQUEST_FILE = "invoice.xml";

	@Autowired
	private InvoiceRepository repository;

	@Test
	void test01_createInvoice() throws IOException, URISyntaxException {

		// Assert that invoice doesn't exist yet.
		assertThat(repository.existsByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "555")).isFalse();

		// Call.
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withContentType(APPLICATION_XML)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		// Assert that invoice exists with expected values.
		assertThat(repository.findByMunicipalityIdAndInvoiceId(MUNICIPALITY_ID, "555")).get()
			.extracting(InvoiceEntity::getContent, InvoiceEntity::getInvoiceId, InvoiceEntity::getStatus)
			.containsExactly(getResourceAsString("CreateInvoiceIT/__files/test01_createInvoice/invoice.xml"), "555", UNPROCESSED);
	}

	@Test
	void test02_createInvoiceThatWillFail() throws IOException, URISyntaxException {

		// Assert that we have one failed invoice (with null as invoiceId).
		assertThat(repository.findByStatusIn(IMPORT_FAILED))
			.extracting(InvoiceEntity::getId, InvoiceEntity::getInvoiceId, InvoiceEntity::getStatus)
			.containsExactlyInAnyOrder(
				tuple(2L, null, IMPORT_FAILED));

		// Call.
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withContentType(APPLICATION_XML)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		// Assert that we have two failed invoices (with null as invoiceId).
		assertThat(repository.findByStatusIn(IMPORT_FAILED))
			.extracting(InvoiceEntity::getId, InvoiceEntity::getContent, InvoiceEntity::getInvoiceId, InvoiceEntity::getStatus)
			.containsExactlyInAnyOrder(
				tuple(2L, "Lorem ipsum dolor sit amet", null, IMPORT_FAILED),
				tuple(4L, getResourceAsString("CreateInvoiceIT/__files/test02_createInvoiceThatWillFail/invoice.xml"), null, IMPORT_FAILED));
	}

	private String getResourceAsString(final String resourcePath) throws IOException, URISyntaxException {
		return Files.readString(Paths.get(getClass().getClassLoader().getResource(resourcePath).toURI()));
	}
}
