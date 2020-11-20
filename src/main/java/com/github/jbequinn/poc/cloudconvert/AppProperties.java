package com.github.jbequinn.poc.cloudconvert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppProperties {
	@Builder.Default
	private String inputFilePath = "/tmp/test-file.docx";
	@Builder.Default
	private String outputFilePath = "/tmp/test-file.pdf";
	@Builder.Default
	private String uploadUrl = "https://api.cloudconvert.com/v2/import/upload";
	private String accessToken;
}
