package org.edx.utils;

import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Optional;

public class PropertyLoader {

	private static final String PROP_FILE = "/config.properties";

	public static Optional<String> loadProperty(String name) {
		Properties props = new Properties();
		try {
			props.load(PropertyLoader.class.getResourceAsStream(PROP_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.of(props.getProperty(name));
	}
}
