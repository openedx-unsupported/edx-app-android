package org.edx.main;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
/**
 * This file is required if user wants jar executable
 */
public class MavenRun {
	
	public static void main(String[] args) throws IOException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File("pom.xml"));
		if (args.length > 0) {
			if (args[0] != null && args[1] != null) {
				Properties projectProperties = new Properties();
				projectProperties.setProperty("deviceOS", args[0]);
				projectProperties.setProperty("appPath", args[1]);
				projectProperties.setProperty("osVersion", args[2]);
				projectProperties.setProperty("deviceName", args[3]);
				projectProperties.setProperty("udid", args[4]);
				request.setProperties(projectProperties);
			}
		}

		request.setGoals(Collections.singletonList("test"));
		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File(System.getenv("M2_HOME")));
		try {
			invoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
	}
}
