package com.github.jbequinn.poc.cloudconvert;

import com.github.jbequinn.poc.cloudconvert.service.CloudConvertService;

public class App {
	public static void main(String[] args) {
		new CloudConvertService(new AppProperties()).convert();
	}
}
