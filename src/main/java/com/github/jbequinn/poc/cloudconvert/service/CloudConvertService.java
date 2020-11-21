package com.github.jbequinn.poc.cloudconvert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jbequinn.poc.cloudconvert.AppProperties;
import com.github.jbequinn.poc.cloudconvert.pojo.JobResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class CloudConvertService {
	@NonNull private final AppProperties properties;
	@NonNull private final OkHttpClient client;
	@NonNull private final ObjectMapper objectMapper;

	public void convert() throws IOException {
		// 0. correctness - fail fast: verify that the file exists, the access token is present, etc
		if (properties.getAccessToken() == null) {
			throw new IllegalArgumentException("No access token present.");
		}
		if (!new File(properties.getInputFilePath()).exists()) {
			throw new IllegalArgumentException("The file: " + properties.getInputFilePath() + " does not exist or is not accessible");
		}

		// 1. create the job
		JobResponse job = createConversionJob();

		// the import-upload task contains the url to send the file to
		JobResponse.Result uploadTask = Arrays.stream(job.getData().getTasks())
				.filter(subtask -> "import-test-file".equals(subtask.getName()))
				.findAny()
				.map(JobResponse.Task::getResult)
				.orElseThrow(() -> new IllegalStateException("No import file task found"));

		// 2. upload the file - the process will not continue until the file is uploaded
		uploadFile(uploadTask);

		// 3. poll until ready
		await("job to be finished")
				.atMost(5, TimeUnit.MINUTES)
				.until(jobIsNotProcessing(job.getData().getId()));

		// 4. download the file
		JobResponse finalJobResponse = getJobInfo(job.getData().getId());
		JobResponse.FileItem exportedFile = Arrays.stream(finalJobResponse.getData().getTasks())
				.filter(subtask -> "export-test-file".equals(subtask.getName()))
				.findAny()
				.map(JobResponse.Task::getResult)
				.map(JobResponse.Result::getFiles)
				// there is supposed to be only one file
				.map(fileItems -> fileItems[0])
				.orElseThrow(() -> new IllegalStateException("No export file task found"));

		downloadFile(exportedFile);
	}

	// this service class does not need to know the specific details of how cloudconvert
	// is accessed. it could be moved to a 'dao' class
	private JobResponse createConversionJob() throws IOException {
		Request request = new Request.Builder()
				.url(properties.getJobsUrl())
				.header("Authorization", "Bearer " + properties.getAccessToken())
				.header("Content-Type", "application/json")
				.post(RequestBody.create(CloudConvertService.class.getResourceAsStream("/job-request.json").readAllBytes()))
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				log.error(new String(response.body().bytes()));
				throw new IllegalStateException("Error when creating the job. status code: " + response.code());
			}

			return objectMapper.readValue(
					Optional.ofNullable(response.body())
							.orElseThrow(IllegalStateException::new)
							.bytes(),
					JobResponse.class
			);
		}
	}

	private void uploadFile(JobResponse.Result uploadTask) throws IOException {
		// the filename placeholder associated to the 'key' key needs to be replaced with the 'real' filename,
		// or in case of sandbox tests, the whitelisted filename
		String filename = Optional.ofNullable(properties.getWhileListFilename()).orElse(properties.getInputFilePath());
		uploadTask.getForm().getParameters()
				.compute("key", (key, value) -> value.replace("${filename}", filename));

		MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
		multipartBodyBuilder.setType(MultipartBody.FORM);
		uploadTask.getForm().getParameters().forEach(multipartBodyBuilder::addFormDataPart);

		try (FileInputStream fileInputStream = new FileInputStream(properties.getInputFilePath())) {
			multipartBodyBuilder.addFormDataPart(
					"file",
					filename,
					RequestBody.create(fileInputStream.readAllBytes())
			);

			try (Response response = client.newCall(new Request.Builder()
					.url(uploadTask.getForm().getUrl())
					.post(multipartBodyBuilder.build())
					.build())
					.execute()) {

				if (!response.isSuccessful()) {
					log.error(new String(response.body().bytes()));
					throw new IllegalStateException("Error when uploading the file. status code: " + response.code());
				}

				// nothing to return - the body doesn't seem to have anything useful
			}
		}
	}

	private Callable<Boolean> jobIsNotProcessing(String jobId) {
		return () -> !"processing".equals(getJobInfo(jobId).getData().getStatus());
	}

	private JobResponse getJobInfo(String jobId) throws IOException {
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

	private void downloadFile(JobResponse.FileItem exportedFile) throws IOException {
		Request request = new Request.Builder()
				.url(exportedFile.getUrl())
				.get()
				.build();

		try (Response response = client.newCall(request).execute();
				 FileOutputStream fileOutputStream = new FileOutputStream(properties.getOutputFilePath())) {

			if (!response.isSuccessful()) {
				throw new IllegalStateException("Error when downloading the file. error code: " + response.code());
			}

			fileOutputStream.write(Optional.ofNullable(response.body())
					.orElseThrow(IllegalStateException::new)
					.bytes());
		}
	}
}
