package com.github.jbequinn.poc.cloudconvert.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobResponse {
	private JobData data;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class JobData {
		private String id;
		private String status;
		private Task[] tasks;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Task {
		private String name;
		private String status;
		private Result result;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Result {
		private Form form;
		private FileItem[] files;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Form {
		private String url;
		private Map<String, String> parameters;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class FileItem {
		private String filename;
		private String url;
	}
}
