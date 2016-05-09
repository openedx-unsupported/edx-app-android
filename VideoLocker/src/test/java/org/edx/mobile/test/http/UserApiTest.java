package org.edx.mobile.test.http;

import com.google.gson.Gson;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.PaginationData;
import org.edx.mobile.profiles.BadgeAssertion;
import org.edx.mobile.profiles.BadgeClass;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.user.UserAPI;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit.RestAdapter;

import static org.junit.Assert.assertEquals;

public class UserApiTest extends BaseTestCase {

    private BadgeAssertion getTestBadge() {
        return new BadgeAssertion("some user", "http://example.com/evidence", "http://example.com/image.jpg", new Date(),
                new BadgeClass(
                        "someslug", "some component", "A badge!", "A badge you get for stuff", "http://example.com/image.jpg", "somecourse"
                )
        );
    }

    private String getTestBadgeString() {
        Page<BadgeAssertion> response = new Page<>(new PaginationData(1, 1, null, null), Collections.singletonList(getTestBadge()));
        return new Gson().toJson(response);
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
        Page<BadgeAssertion> badges = api.getBadges("user", 1);

        assertEquals(badges.getResults().size(), 1);
    }
}
