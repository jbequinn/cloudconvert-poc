package com.github.jbequinn.poc.cloudconvert.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobList {
	private List<Job> data;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Job {
		private String id;
	}
}
