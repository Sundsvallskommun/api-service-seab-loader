package se.sundsvall.seabloader.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static se.sundsvall.seabloader.integration.db.model.enums.Status.PROCESSED;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.seabloader.Application;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;

/**
 * JobsIT tests.
 */
@WireMockAppTestSuite(files = "classpath:/JobsIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class JobsIT extends AbstractAppTest {

	@Autowired
	private InvoiceRepository repository;

	@Test
	void test01_notifier() {

		// Call
		setupCall()
			.withServicePath("/jobs/notifier")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_invoiceexporter() {

		// Call
		setupCall()
			.withServicePath("/jobs/invoiceexporter")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_dbcleaner() {

		// Assert that we have records with status PROCESSED.
		assertThat(repository.findByStatusIn(PROCESSED)).isNotEmpty();

		// Call
		setupCall()
			.withServicePath("/jobs/dbcleaner")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		// Assert that we don't have records with status PROCESSED.
		assertThat(repository.findByStatusIn(PROCESSED)).isEmpty();
	}
}
