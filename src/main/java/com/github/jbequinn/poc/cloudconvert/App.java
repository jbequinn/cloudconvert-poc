package com.github.jbequinn.poc.cloudconvert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jbequinn.poc.cloudconvert.service.CloudConvertService;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class App {
	public static void main(String[] args) throws IOException {
		AppProperties properties = AppProperties.builder()
				.accessToken(System.getProperty("CLOUDCONVERT_ACCESS_TOKEN"))
				.build();

		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(50, TimeUnit.SECONDS)
				.readTimeout(50, TimeUnit.SECONDS)
				.callTimeout(90, TimeUnit.SECONDS)
				.build();

		ObjectMapper objectMapper = new ObjectMapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.enable(SerializationFeature.WRAP_ROOT_VALUE);

		new CloudConvertService(properties, client, objectMapper).convert();
	}
}
