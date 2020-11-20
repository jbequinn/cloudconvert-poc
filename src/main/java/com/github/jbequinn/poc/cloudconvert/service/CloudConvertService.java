package com.github.jbequinn.poc.cloudconvert.service;

import com.github.jbequinn.poc.cloudconvert.AppProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CloudConvertService {
	@NonNull private final AppProperties properties;

	public void convert() {
		// 0. correctness: verify that the file exists, the access token is present, etc

		// 1. upload the file
		uploadFile();

		// create the job
		// 2. call convert
		// 3. poll until ready
		// 4. download the file
	}

	private void uploadFile() {
		// POST to import/upload to initiate the process

		// parse the response. get result.form url and parameters

		// POST the file contents
	}
}
