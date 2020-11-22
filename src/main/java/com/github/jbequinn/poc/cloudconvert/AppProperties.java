package com.github.jbequinn.poc.cloudconvert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNullElse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppProperties {
	@Builder.Default
	private String inputFilePath = requireNonNullElse(
			System.getProperty("CLOUDCONVERT_INPUT_FILE"),
			"/tmp/test-file.docx"
	);
	@Builder.Default
	private String outputFilePath = requireNonNullElse(
			System.getProperty("CLOUDCONVERT_OUTPUT_FILE"),
			"/tmp/test-file.pdf"
	);
	@Builder.Default
	private String jobsUrl = "https://api.cloudconvert.com/v2/jobs";
	private String accessToken;
	private String whileListFilename;
}
