package com.github.jbequinn.poc.cloudconvert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jbequinn.poc.cloudconvert.AppProperties;
import com.github.jbequinn.poc.cloudconvert.pojo.JobResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class CloudConvertService {
	@NonNull private final AppProperties properties;
	@NonNull private final OkHttpClient client;
	@NonNull private final ObjectMapper objectMapper;

	public void convert() throws IOException {
		// 0. correctness: verify that the file exists, the access token is present, etc

		// 1. create the job
		JobResponse job = createConversionJob();

		// 2. call convert
		// 3. poll until ready
		// 4. download the file
	}

	private JobResponse createConversionJob() throws IOException {
		Request request = new Request.Builder()
				.url(properties.getJobsUrl())
				.header("Authorization", "Bearer " + properties.getAccessToken())
				.header("Content-Type", "application/json")
				.post(RequestBody.create(CloudConvertService.class.getResourceAsStream("/job-request.json").readAllBytes()))
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
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

	private void uploadFile() {
		// POST to import/upload to initiate the process

		// parse the response. get result.form url and parameters

		// POST the file contents
	}
}
