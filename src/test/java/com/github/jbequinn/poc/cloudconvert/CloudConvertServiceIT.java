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

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

public class CloudConvertServiceIT {

	static OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(100, TimeUnit.SECONDS)
			.readTimeout(100, TimeUnit.SECONDS)
			.callTimeout(130, TimeUnit.SECONDS)
			.build();

	static ObjectMapper objectMapper = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.enable(SerializationFeature.WRAP_ROOT_VALUE);

	static AppProperties properties = AppProperties.builder()
			.jobsUrl("https://api.sandbox.cloudconvert.com/v2/jobs")
			.accessToken(System.getProperty("CLOUDCONVERT_ACCESS_TOKEN"))
			.whileListFilename("test-document.docx")
			.inputFilePath(CloudConvertServiceIT.class.getResource("/test-document.docx").getPath())
			.build();

	@BeforeAll
	static void beforeAll() throws Exception {
		properties.setOutputFilePath(File.createTempFile("test-file.pdf", null).getAbsolutePath());

		// GIVEN that there are no existing jobs
		// delete all existing tasks in the sandbox? if some other parallel development is taking place, this could affect it
		deleteExistingJobs(properties);
		assertThat(getExistingJobs(properties).getData())
				.isEmpty();

		// WHEN a new conversion job is created
		new CloudConvertService(properties, client, objectMapper).convert();

		// THEN a new job appears
		assertThat(getExistingJobs(properties).getData())
				.hasSize(1)
				.first()
				.extracting(JobList.Job::getId)
				// verify also that it has three tasks etc
				.isNotNull();
	}

	@Test
	void jobIsCreated() throws Exception {
		JobResponse job = getJobInfo(getExistingJobs(properties).getData().iterator().next().getId());

		// AND THEN this new job contains tasks for a conversion
		assertThat(job.getData().getTasks())
				.extracting(JobResponse.Task::getName)
				.containsExactlyInAnyOrder("import-test-file", "convert-test-file", "export-test-file");
	}

	@Test
	void fileIsUploaded() {
		// AND THEN the import task should be finished
		await()
				.atMost(60, TimeUnit.SECONDS)
				.pollDelay(10, TimeUnit.SECONDS)
				.untilAsserted(() -> {
					JobResponse job = getJobInfo(getExistingJobs(properties).getData().iterator().next().getId());

					assertThat(job.getData().getTasks())
							.filteredOn(task -> "import-test-file".equals(task.getName()))
							.extracting(JobResponse.Task::getStatus)
							.containsExactly("finished");
				});
	}

	@Test
	void fileIsConverted() {
		// AND THEN the conversion task should be finished
		await()
				.atMost(60, TimeUnit.SECONDS)
				.pollDelay(10, TimeUnit.SECONDS)
				.untilAsserted(() -> {
					JobResponse job = getJobInfo(getExistingJobs(properties).getData().iterator().next().getId());

					assertThat(job.getData().getTasks())
							.filteredOn(task -> "convert-test-file".equals(task.getName()))
							.extracting(JobResponse.Task::getStatus)
							.containsExactly("finished");
				});
	}

	@Test
	void fileIsExported() {
		// AND THEN the export task should be finished
		await()
				.atMost(60, TimeUnit.SECONDS)
				.pollDelay(10, TimeUnit.SECONDS)
				.untilAsserted(() -> {
					JobResponse job = getJobInfo(getExistingJobs(properties).getData().iterator().next().getId());

					assertThat(job.getData().getTasks())
							.filteredOn(task -> "export-test-file".equals(task.getName()))
							.extracting(JobResponse.Task::getStatus)
							.containsExactly("finished");
				});
	}

	@Test
	void fileIsDownloaded() {
		// AND THEN the file has been downloaded locally
	  assertThat(new File(properties.getOutputFilePath()))
				.exists()
				.canRead()
				// maybe compare with a pre-downloaded file? but the conversion code could change
				.isNotEmpty();
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

	// this is used also in production. it could be moved to a 'dao' class
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
