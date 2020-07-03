package com.myapp.source;

import java.io.BufferedWriter;
import java.io.IOException;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class EnvVars implements HttpFunction {

	// Returns the environment variable "foo" set during function deployment.
	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		BufferedWriter writer = response.getWriter();
		String foo = System.getenv("FOO");
		if (foo == null) {
			foo = "Specified environment variable is not set.";
		}
		writer.write(foo);
	}
}
