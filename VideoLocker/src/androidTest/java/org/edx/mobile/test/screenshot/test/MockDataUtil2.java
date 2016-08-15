package org.edx.mobile.test.screenshot.test;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MockDataUtil2 {

    private static final String MOCK_API_RESPONSES_PROPERTIES = "mock_data.properties";

    public static String getMockResponse(String apiPropertyName) throws IOException {
        Properties responses = new Properties();
        InputStream in = MockDataUtil2.class.getClassLoader()
                .getResourceAsStream(MOCK_API_RESPONSES_PROPERTIES);
        if (in == null) {
            throw new NullPointerException();
        }
        try {
            responses.load(in);
        } finally {
            in.close();
        }
        return responses.getProperty(apiPropertyName);
    }

    public static <T> T getMockResponse(@NonNull String apiPropertyName, @NonNull Class<T> cls) throws IOException {
        return new Gson().fromJson(getMockResponse(apiPropertyName), cls);
    }
}
