package se.sundsvall.seabloader.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.seabloader.Application;

/**
 * ReadInformationIT tests.
 */
@WireMockAppTestSuite(files = "classpath:/ReadInformationIT/", classes = Application.class)
class ReadInformationIT extends AbstractAppTest {

	@Test
	void test01_readSchedulerInformation() {

		// Call
		setupCall()
			.withServicePath("/information/schedulers")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
