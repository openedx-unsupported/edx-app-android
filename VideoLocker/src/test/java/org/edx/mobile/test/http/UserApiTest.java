package org.edx.mobile.test.http;

import com.google.gson.Gson;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.profiles.BadgeAssertion;
import org.edx.mobile.profiles.BadgeSpec;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.user.UserAPI;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit.RestAdapter;

public class UserApiTest extends BaseTestCase {

    private BadgeAssertion getTestBadge() {
        return new BadgeAssertion("some user", "http://example.com/evidence", "http://example.com/image.jpg", new Date(),
                new BadgeSpec(
                        "someslug", "some component", "A badge!", "A badge you get for stuff", "http://example.com/image.jpg", "somecourse"
                )
        );
    }

    private String getTestBadgeString() {
        return new Gson().toJson(Arrays.asList(getTestBadge()));
    }

    @Test
    public void testApiReturnsResult() throws RetroHttpException {
        MockWebServer server = new MockWebServer();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new Ok3Client(OkHttpUtil.getClient(context)))
                .setEndpoint(server.url("/").toString())
                .build();

        server.enqueue(new MockResponse().setBody(this.getTestBadgeString()));

        UserAPI api = new UserAPI(restAdapter);
        List<BadgeAssertion> badges = api.getBadges("user");

        assertEquals(badges.size(), 1);
    }
}
