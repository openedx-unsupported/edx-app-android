package org.edx.mobile.view;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class CourseUnitYoutubeVideoFragmentTest extends BaseCourseUnitVideoFragmentTest {

    private static final String YOUTUBE_IN_APP_PLAYER = "YOUTUBE_IN_APP_PLAYER";

    @Override
    protected JsonObject generateConfigProperties() throws IOException {
        // Add the mock youtube api key in the test config properties
        final JsonObject properties = super.generateConfigProperties();
        properties.add(YOUTUBE_IN_APP_PLAYER, getYoutubeMockConfig());
        return properties;
    }

    private JsonElement getYoutubeMockConfig() {
        final String serializedData = "{\"ENABLED\":\"True\", \"API_KEY\":\"TEST_YOUTUBE_API_KEY\"}";
        return new JsonParser().parse(serializedData);
    }

    @Override
    protected BaseCourseUnitVideoFragment getCourseUnitPlayerFragmentInstance() {
        return CourseUnitYoutubePlayerFragment.newInstance(getVideoUnit());
    }
}
