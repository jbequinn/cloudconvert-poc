package com.github.jbequinn.poc.cloudconvert;

import com.github.jbequinn.poc.cloudconvert.service.CloudConvertService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class CloudConvertServiceIT {

	@TempDir
	static Path resultTemporaryDirectory;

	@BeforeAll
	static void beforeAll() {
		// delete all existing tasks in the sandbox? if you have other git branches, it could affect them

		CloudConvertService service = new CloudConvertService(AppProperties.builder()
				.uploadUrl("https://api.sandbox.cloudconvert.com/v2/import/upload")
				// .accessToken() - get it from the environment or somewhere
				.inputFilePath(CloudConvertServiceIT.class.getResource("/test-document.docx").getPath())
				.outputFilePath(resultTemporaryDirectory.resolve("output.pdf").toString())
				.build());

		service.convert();
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
}
