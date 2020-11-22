package com.github.jbequinn.poc.cloudconvert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jbequinn.poc.cloudconvert.service.CloudConvertService;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class CloudServiceTest {
	@Test
	void failOnMissingAccessToken() {
		assertThatIllegalArgumentException()
				.isThrownBy(() ->
						new CloudConvertService(AppProperties.builder()
								// GIVEN there's no access token
								.accessToken(null)
								.inputFilePath(CloudConvertServiceIT.class.getResource("/test-document.docx").getPath())
								.build(),
								new OkHttpClient(),
								new ObjectMapper())
								// WHEN calling the conversion service
								.convert());

		// THEN an illegal argument exception is thrown
	}

	@Test
	void failOnMissingFile() {
		assertThatIllegalArgumentException()
				.isThrownBy(() ->
						new CloudConvertService(AppProperties.builder()
								.accessToken("totally-valid-token")
								// GIVEN a non valid path
								.inputFilePath("some-non-existing-path")
								.build(),
								new OkHttpClient(),
								new ObjectMapper())
								// WHEN calling the conversion service
								.convert());

		// THEN an illegal argument exception is thrown
	}

	@Test
	void formParametersAreParsedAndUsed() {
		// wiremock a response to import/upload with the json form element
		// then verify that the other POST arrives with those form parameters
	}
}
