package com.github.jbequinn.poc.cloudconvert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jbequinn.poc.cloudconvert.pojo.JobList;
import com.github.jbequinn.poc.cloudconvert.pojo.JobResponse;
import com.github.jbequinn.poc.cloudconvert.service.CloudConvertService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class CloudConvertServiceIT {

	static OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(50, TimeUnit.SECONDS)
			.readTimeout(50, TimeUnit.SECONDS)
			.callTimeout(90, TimeUnit.SECONDS)
			.build();

	static ObjectMapper objectMapper = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.enable(SerializationFeature.WRAP_ROOT_VALUE);

	static AppProperties properties = AppProperties.builder()
			.uploadUrl("https://api.sandbox.cloudconvert.com/v2/import/upload")
			.jobsUrl("https://api.sandbox.cloudconvert.com/v2/jobs")
			.accessToken(System.getenv("CLOUDCONVERT_ACCESS_TOKEN"))
			.inputFilePath(CloudConvertServiceIT.class.getResource("/test-document.docx").getPath())
			.build();

	@BeforeAll
	static void beforeAll() throws Exception {
		// delete all existing tasks in the sandbox? if you have other git branches, it could affect them
		deleteExistingJobs(properties);

		// GIVEN that there are no existing jobs
		assertThat(getExistingJobs(properties).getData())
				.isEmpty();

		// WHEN a new job is created
		CloudConvertService service = new CloudConvertService(properties, client, objectMapper);
		service.convert();
	}

	@Test
	void jobIsCreated() throws Exception {
		JobList existingJobs = getExistingJobs(properties);
		// THEN a new job exists
		assertThat(existingJobs.getData())
				.hasSize(1)
				.first()
				.extracting(JobList.Job::getId)
				.isNotNull();

		JobResponse job = getJobInfo(existingJobs.getData().iterator().next().getId());

		// AND THEN this new job contains tasks for a conversion
		assertThat(job.getData().getTasks())
				.extracting(JobResponse.Task::getName)
				.containsExactlyInAnyOrder("import-test-file", "convert-test-file", "export-test-file");
	}

	@Test
	void fileIsUploaded() {
		// verify that a import/upload task is created
		fail();
	}

	@Test
	void fileIsConverted() {
		// verify that a convert task is created
		fail();
	}

	@Test
	void fileIsReadyToBeExported() {
		// verify that an export task is created
		fail();
	}

	@Test
	void fileIsExported() {
		// verify that the exported file exists. is it worthy comparing it with an expected result file?
	}

	private static void deleteExistingJobs(AppProperties properties) throws Exception {
		getExistingJobs(properties).getData().forEach(job -> {
			try {
				client.newCall(new Request.Builder()
						.url(properties.getJobsUrl() + "/" + job.getId())
						.header("Authorization", "Bearer " + properties.getAccessToken())
						.delete()
						.build())
						.execute();
			} catch (IOException e) {
				fail();
			}
		});
	}

	private static JobList getExistingJobs(AppProperties properties) throws Exception {
		// let's ignore for now that there can be more pages with jobs

		try (Response response = client.newCall(new Request.Builder()
				.url(properties.getJobsUrl())
				.header("Authorization", "Bearer " + properties.getAccessToken())
				.get()
				.build())
				.execute()) {

			return objectMapper.readValue(response.body().byteStream(), JobList.class);
		}
	}

	private static JobResponse getJobInfo(String jobId) throws IOException {
		try (Response response = client.newCall(new Request.Builder()
				.url(properties.getJobsUrl() + "/" + jobId)
				.header("Authorization", "Bearer " + properties.getAccessToken())
				.get()
				.build())
				.execute()) {

			if (!response.isSuccessful()) {
				throw new IllegalStateException("Error when getting the job with id: " + jobId + ". error code: " + response.code());
			}

			return objectMapper.readValue(
					Optional.ofNullable(response.body())
							.orElseThrow(IllegalStateException::new)
							.bytes(),
					JobResponse.class
			);
		}
	}
}


