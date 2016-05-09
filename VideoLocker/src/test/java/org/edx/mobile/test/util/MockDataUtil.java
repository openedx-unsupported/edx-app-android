package org.edx.mobile.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by cleeedx on 4/14/16.
 */
public class MockDataUtil {

    private static final String MOCK_API_RESPONSES_PROPERTIES = "mock_api_responses.properties";

    public static String getMockResponse(String apiPropertyName) throws IOException {
        Properties responses = new Properties();
        InputStream in = MockDataUtil.class.getClassLoader()
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
}
