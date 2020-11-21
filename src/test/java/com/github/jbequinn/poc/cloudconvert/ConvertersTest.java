package com.github.jbequinn.poc.cloudconvert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jbequinn.poc.cloudconvert.pojo.JobResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertersTest {

	@Test
	void jobRequestDeserializer() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.enable(SerializationFeature.WRAP_ROOT_VALUE);

		JobResponse response = objectMapper.readValue(
				ConvertersTest.class.getResourceAsStream("/job-response.json"),
				JobResponse.class);

		assertThat(response.getData().getId()).isEqualTo("some-job-id");

		assertThat(response.getData().getTasks())
				.filteredOn(task -> "import-test-file".equals(task.getName()))
				.isNotEmpty()
				.extracting(JobResponse.Task::getResult)
				.extracting(JobResponse.Result::getForm)
				.extracting(JobResponse.Form::getUrl)
				.containsExactly("https://some-url-upload");

		assertThat(response.getData().getTasks())
				.filteredOn(subtask -> "import-test-file".equals(subtask.getName()))
				.isNotEmpty()
				.extracting(JobResponse.Task::getResult)
				.extracting(JobResponse.Result::getForm)
				.extracting(JobResponse.Form::getParameters)
				.isNotEmpty()
				// verify that it contains some specific values
				.doesNotHaveDuplicates();
	}
}
